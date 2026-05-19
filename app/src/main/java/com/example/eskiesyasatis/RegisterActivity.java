package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private android.widget.TextView tvGoToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView btnBackRegister = findViewById(R.id.btnBackRegister);
        btnBackRegister.setOnClickListener(v -> finish());

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> validateAndRegister());

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm boşlukları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Şifreniz en az 6 karakterden oluşmalıdır.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Girdiğiniz şifreler uyuşmuyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Eğer tüm kontrollerden geçerse Firebase'e kayıt işlemine başlar
        registerUser(name, email, password);
    }

    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Kayıt Başarılı! Lütfen e-postanızı doğrulayın.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Kayıt Başarılı! Doğrulama e-postası gönderilemedi.", Toast.LENGTH_LONG).show();
                                    }
                                    mAuth.signOut(); // Doğrulamadan girmesine izin verme
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                            });
                        }
                    } else {
                        // Eğer kayıt işlemi başarısız olursa Firebase'in hatasını Türkçeye çevirip kullanıcıya gösteriyoruz
                        String errorMessage = "Bilinmeyen bir hata oluştu. Lütfen tekrar deneyin.";
                        Exception e = task.getException();

                        if (e != null) {
                            if (e instanceof FirebaseAuthWeakPasswordException) {
                                errorMessage = "Şifreniz çok zayıf. Lütfen daha güçlü bir şifre belirleyin.";
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "E-posta adresinin formatı geçersiz.";
                            } else if (e instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Bu e-posta adresiyle kayıtlı bir hesap zaten var.";
                            } else if (e instanceof FirebaseNetworkException) {
                                errorMessage = "İnternet bağlantınız yok veya sunucuya ulaşılamıyor.";
                            } else if (e.getMessage() != null && e.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Sunucu Kayıt Ayarları henüz aktif edilmemiş! Lütfen Firebase Authentication'ı kontrol edin.";
                            } else {
                                errorMessage = "Kayıt Başarısız: " + e.getLocalizedMessage();
                            }
                        }

                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
