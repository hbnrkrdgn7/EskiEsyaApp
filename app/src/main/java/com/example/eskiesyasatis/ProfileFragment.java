package com.example.eskiesyasatis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ListenerRegistration unreadListener;
    private ImageView ivProfilePhoto;
    private ActivityResultLauncher<String> photoPickerLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        uploadProfilePhoto(resultUri);
                    }
                }
            }
        );

        // Fotoğraf seçici launcher'ını onCreate'de kaydet
        photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && getContext() != null) {
                    Uri destinationUri = Uri.fromFile(new java.io.File(getContext().getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
                    com.yalantis.ucrop.UCrop uCrop = com.yalantis.ucrop.UCrop.of(uri, destinationUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(500, 500);
                    cropLauncher.launch(uCrop.getIntent(getContext()));
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Kullanıcı bilgilerini göster
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        View viewUnreadProfileBadge = view.findViewById(R.id.viewUnreadProfileBadge);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        FrameLayout layoutProfilePhoto = view.findViewById(R.id.layoutProfilePhoto);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            } else {
                tvProfileName.setText("Kullanıcı");
            }
            tvProfileEmail.setText(currentUser.getEmail());

            // Profil fotoğrafını yükle
            loadProfilePhoto(currentUser.getUid());

            // Fotoğraf değiştirmek için tıkla
            layoutProfilePhoto.setOnClickListener(v -> {
                photoPickerLauncher.launch("image/*");
            });

            // Okunmamış mesajları dinle
            String currentUserId = currentUser.getUid();
            unreadListener = FirebaseFirestore.getInstance().collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    boolean hasUnread = false;
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Map<String, Long> unreadCounts = (Map<String, Long>) doc.get("unreadCounts");
                        if (unreadCounts != null && unreadCounts.containsKey(currentUserId)) {
                            Long count = unreadCounts.get(currentUserId);
                            if (count != null && count > 0) {
                                hasUnread = true;
                                break;
                            }
                        }
                    }
                    if (viewUnreadProfileBadge != null) {
                        viewUnreadProfileBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }
                });
        }

        // İlanlarım
        LinearLayout menuMyListings = view.findViewById(R.id.menuMyListings);
        menuMyListings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyListingsActivity.class);
            startActivity(intent);
        });

        // Sohbetlerim
        LinearLayout menuMyChats = view.findViewById(R.id.menuMyChats);
        menuMyChats.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyChatsActivity.class);
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

    private void loadProfilePhoto(String userId) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists() && doc.getString("profileImageUrl") != null) {
                    String url = doc.getString("profileImageUrl");
                    if (getContext() != null && ivProfilePhoto != null) {
                        ivProfilePhoto.setImageTintList(null); // Tint'i kaldır
                        Glide.with(getContext())
                            .load(url)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivProfilePhoto);
                    }
                }
            });
    }

    private void uploadProfilePhoto(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) return;

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Fotoğraf kaydediliyor...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        com.google.firebase.storage.StorageReference storageRef = storage.getReference().child("profile_images/" + user.getUid() + ".jpg");

        storageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String finalUrl = uri.toString();
                    
                    java.util.HashMap<String, Object> data = new java.util.HashMap<>();
                    data.put("profileImageUrl", finalUrl);
                    data.put("name", user.getDisplayName());
                    data.put("email", user.getEmail());
                    
                    FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUid())
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            if (getContext() != null) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Profil fotoğrafı başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
                                if (ivProfilePhoto != null) {
                                    ivProfilePhoto.setImageTintList(null);
                                    Glide.with(getContext())
                                        .load(finalUrl)
                                        .circleCrop()
                                        .into(ivProfilePhoto);
                                }
                            }
                        });
                });
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Fotoğraf yüklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            java.io.InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            java.io.File outputFile = new java.io.File(getContext().getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            java.io.OutputStream outputStream = new java.io.FileOutputStream(outputFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return Uri.fromFile(outputFile).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unreadListener != null) {
            unreadListener.remove();
        }
    }
}
