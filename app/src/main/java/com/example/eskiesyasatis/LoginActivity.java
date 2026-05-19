package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private android.widget.TextView tvGoToRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> validateAndLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen e-posta ve şifrenizi girin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Lütfen e-posta adresinize gönderilen linke tıklayarak hesabınızı doğrulayın.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Doğrulamamışsa çıkış yap
                        }
                    } else {
                        String errorMessage = "Giriş işlemi başarısız oldu.";
                        Exception e = task.getException();

                        if (e != null) {
                            String errorCode = "";
                            if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
                                errorCode = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();
                            }
                            
                            if (e instanceof FirebaseAuthInvalidUserException || "ERROR_USER_NOT_FOUND".equals(errorCode)) {
                                errorMessage = "Girdiğiniz e-posta adresine kayıtlı bir hesap bulunamadı.";
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Eğer e-posta formatı hatası değilse, şifre yanlış demektir
                                if ("ERROR_INVALID_EMAIL".equals(errorCode)) {
                                    errorMessage = "Lütfen geçerli bir e-posta adresi girin.";
                                } else {
                                    errorMessage = "Girdiğiniz şifre yanlış. Lütfen tekrar deneyin.";
                                }
                            } else if (e instanceof FirebaseNetworkException) {
                                errorMessage = "İnternet bağlantınız yok veya sunucuya erişilemiyor.";
                            } else if (e.getMessage() != null && e.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Sistem ayarları yapılandırılmamış (Firebase Auth etkin değil).";
                            } else {
                                errorMessage = "Giriş Hatası: " + e.getLocalizedMessage();
                            }
                        }
                        
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
