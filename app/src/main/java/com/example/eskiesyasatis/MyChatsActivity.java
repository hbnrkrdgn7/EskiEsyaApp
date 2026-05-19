package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatPreviewAdapter adapter;
    private TextView tvEmptyChats;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewChats);
        tvEmptyChats = findViewById(R.id.tvEmptyChats);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatPreviewAdapter(chatList,
            chat -> {
                // Sohbete git
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("CHAT_ID", chat.getChatId());
                intent.putExtra("OTHER_USER_ID", chat.getOtherUserId());
                intent.putExtra("OTHER_USER_NAME", chat.getOtherUserName());
                intent.putExtra("LISTING_ID", chat.getListingId());
                intent.putExtra("LISTING_TITLE", chat.getListingTitle());
                intent.putExtra("LISTING_IMAGE_URL", chat.getListingImageUrl());
                startActivity(intent);
            },
            chat -> {
                // Uzun basınca sil
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Sohbeti Sil")
                    .setMessage("Bu sohbeti silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        db.collection("Chats").document(chat.getChatId()).delete();
                        Toast.makeText(this, "Sohbet silindi.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                    .show();
            }
        );
        recyclerView.setAdapter(adapter);

        loadChats();
    }

    private void loadChats() {
        if (currentUserId == null) return;

        db.collection("Chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) return;
                chatList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Chat chat = new Chat();
                    chat.setChatId(doc.getId());
                    chat.setParticipants((List<String>) doc.get("participants"));
                    chat.setLastMessage(doc.getString("lastMessage"));
                    chat.setLastMessageTime(doc.getTimestamp("lastMessageTime"));

                    // Karşı tarafın bilgilerini bul
                    List<String> participants = chat.getParticipants();
                    if (participants != null) {
                        for (String pId : participants) {
                            if (!pId.equals(currentUserId)) {
                                chat.setOtherUserId(pId);
                                break;
                            }
                        }
                    }

                    // İsim belirleme
                    String creatorName = doc.getString("creatorName");
                    String otherName = doc.getString("otherUserName");
                    
                    if (chat.getOtherUserId() != null) {
                        // Eğer ben oluşturduysam, otherUserName karşı tarafın adı
                        // Eğer karşı taraf oluşturmuşsa, creatorName karşı tarafın adı
                        List<String> parts = chat.getParticipants();
                        if (parts != null && parts.size() >= 2) {
                            String firstParticipant = parts.get(0);
                            if (firstParticipant.equals(currentUserId)) {
                                chat.setOtherUserName(otherName != null ? otherName : "Kullanıcı");
                            } else {
                                chat.setOtherUserName(creatorName != null ? creatorName : "Kullanıcı");
                            }
                        }
                    }

                    chat.setListingId(doc.getString("listingId"));
                    chat.setListingTitle(doc.getString("listingTitle"));
                    chat.setListingImageUrl(doc.getString("listingImageUrl"));
                    chat.setUnreadCounts((java.util.Map<String, Long>) doc.get("unreadCounts"));

                    chatList.add(chat);
                }

                adapter.updateList(chatList);
                updateEmptyState();
            });
    }

    private void updateEmptyState() {
        if (chatList.isEmpty()) {
            tvEmptyChats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyChats.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
