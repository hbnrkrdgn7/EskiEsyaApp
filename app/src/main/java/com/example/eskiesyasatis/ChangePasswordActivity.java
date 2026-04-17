package com.example.eskiesyasatis;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmNewPassword = etConfirmNewPassword.getText() != null ? etConfirmNewPassword.getText().toString().trim() : "";

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Yeni şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Yeni şifreler uyuşmuyor.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            Toast.makeText(this, "Yeni şifre eski şifre ile aynı olamaz.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Önce mevcut şifreyle yetkilendir, sonra şifreyi değiştir
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("İşleniyor...");

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Şifreniz başarıyla değiştirildi!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = updateTask.getException() != null ? updateTask.getException().getLocalizedMessage() : "Bilinmeyen hata";
                        Toast.makeText(this, "Şifre değiştirilemedi: " + errorMsg, Toast.LENGTH_LONG).show();
                        btnChangePassword.setEnabled(true);
                        btnChangePassword.setText("ŞİFREYİ DEĞİŞTİR");
                    }
                });
            } else {
                Toast.makeText(this, "Mevcut şifreniz yanlış.", Toast.LENGTH_SHORT).show();
                btnChangePassword.setEnabled(true);
                btnChangePassword.setText("ŞİFREYİ DEĞİŞTİR");
            }
        });
    }
}
