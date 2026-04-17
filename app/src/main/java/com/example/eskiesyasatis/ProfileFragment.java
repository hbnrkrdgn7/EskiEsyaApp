package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Kullanıcı bilgilerini göster
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            } else {
                tvProfileName.setText("Kullanıcı");
            }
            tvProfileEmail.setText(currentUser.getEmail());
        }

        // İlanlarım
        LinearLayout menuMyListings = view.findViewById(R.id.menuMyListings);
        menuMyListings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyListingsActivity.class);
            startActivity(intent);
        });

        // Siparişlerim
        LinearLayout menuMyOrders = view.findViewById(R.id.menuMyOrders);
        menuMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyOrdersActivity.class);
            startActivity(intent);
        });

        // Kartlarım
        LinearLayout menuMyCards = view.findViewById(R.id.menuMyCards);
        menuMyCards.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyCardsActivity.class);
            startActivity(intent);
        });

        // Şifre Değiştir
        LinearLayout menuChangePassword = view.findViewById(R.id.menuChangePassword);
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Çıkış Yap
        LinearLayout menuLogout = view.findViewById(R.id.menuLogout);
        menuLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            CartRepository.resetInstance();
            FavoritesRepository.resetInstance();
            Toast.makeText(getContext(), "Çıkış yapıldı.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Hesabı Sil
        LinearLayout menuDeleteAccount = view.findViewById(R.id.menuDeleteAccount);
        menuDeleteAccount.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Hesabı Sil")
                .setMessage("Hesabınızı ve tüm ilanlarınızı kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz!")
                .setPositiveButton("Eminim, Sil", (dialog, which) -> {
                    FirebaseUser userToDelete = FirebaseAuth.getInstance().getCurrentUser();
                    if (userToDelete != null) {
                        String uid = userToDelete.getUid();
                        // Önce ilanları sil
                        ListingRepository.getInstance().deleteListingsByOwnerId(uid);
                        
                        // Sonra hesabı sil
                        userToDelete.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Hesabınız başarıyla silindi.", Toast.LENGTH_LONG).show();
                                CartRepository.resetInstance();
                                FavoritesRepository.resetInstance();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Hesap silinirken hata oluştu. Lütfen tekrar giriş yapıp deneyin.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                .show();
        });

        return view;
    }
}
