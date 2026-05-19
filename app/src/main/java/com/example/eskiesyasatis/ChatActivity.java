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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private android.widget.EditText etMessage;
    private List<ChatMessage> messages = new ArrayList<>();
    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String listingId;
    private String listingTitle;
    private String listingImageUrl;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        otherUserId = getIntent().getStringExtra("OTHER_USER_ID");
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        chatId = getIntent().getStringExtra("CHAT_ID");
        listingId = getIntent().getStringExtra("LISTING_ID");
        listingTitle = getIntent().getStringExtra("LISTING_TITLE");
        listingImageUrl = getIntent().getStringExtra("LISTING_IMAGE");

        if (currentUserId == null || otherUserId == null) {
            Toast.makeText(this, "Sohbet başlatılamadı.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView btnBack = findViewById(R.id.btnBackChat);
        btnBack.setOnClickListener(v -> finish());

        TextView tvChatUserName = findViewById(R.id.tvChatUserName);
        ImageView ivChatUserPhoto = findViewById(R.id.ivChatUserPhoto);
        
        String chatTitle = (otherUserName != null ? otherUserName : "Kullanıcı");
        if (listingTitle != null) {
            chatTitle += " - " + listingTitle;
        }
        tvChatUserName.setText(chatTitle);

        // Karşı tarafın profil fotoğrafını yükle
        db.collection("Users").document(otherUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getString("profileImageUrl") != null) {
                String url = doc.getString("profileImageUrl");
                ivChatUserPhoto.setImageTintList(null); // Varsayılan ikon rengini kaldır
                com.bumptech.glide.Glide.with(ChatActivity.this)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivChatUserPhoto);
            }
        });

        // Kullanıcı adına veya resme tıklayınca profil sayfası açılsın
        View.OnClickListener profileClickListener = v -> {
            Intent profileIntent = new Intent(this, UserProfileActivity.class);
            profileIntent.putExtra("USER_ID", otherUserId);
            profileIntent.putExtra("USER_NAME", otherUserName);
            startActivity(profileIntent);
        };
        tvChatUserName.setOnClickListener(profileClickListener);
        ivChatUserPhoto.setOnClickListener(profileClickListener);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter(messages, currentUserId);
        recyclerView.setAdapter(adapter);

        if (chatId != null) {
            listenToMessages();
        } else {
            findOrCreateChat();
        }

        // Quick Replies
        com.google.android.material.chip.Chip chipReply1 = findViewById(R.id.chipReply1);
        com.google.android.material.chip.Chip chipReply2 = findViewById(R.id.chipReply2);
        com.google.android.material.chip.Chip chipReply3 = findViewById(R.id.chipReply3);
        com.google.android.material.chip.Chip chipReply4 = findViewById(R.id.chipReply4);

        View.OnClickListener chipListener = v -> {
            String text = ((com.google.android.material.chip.Chip) v).getText().toString();
            etMessage.setText(text);
            sendMessage(); // Automatically send
        };

        if (chipReply1 != null) {
            chipReply1.setOnClickListener(chipListener);
            chipReply2.setOnClickListener(chipListener);
            chipReply3.setOnClickListener(chipListener);
            chipReply4.setOnClickListener(chipListener);
        }

        // Mark as Sold Button and Hide Quick Replies for Seller
        com.google.android.material.button.MaterialButton btnMarkAsSold = findViewById(R.id.btnMarkAsSold);
        View scrollQuickReplies = findViewById(R.id.scrollQuickReplies);
        
        if (listingId != null && currentUserId != null) {
            db.collection("Listings").document(listingId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String ownerId = doc.getString("userId");
                    if (currentUserId.equals(ownerId)) {
                        // User is the seller
                        if (btnMarkAsSold != null) btnMarkAsSold.setVisibility(View.VISIBLE);
                        if (btnMarkAsSold != null) btnMarkAsSold.setOnClickListener(v -> markAsSold());
                        if (scrollQuickReplies != null) scrollQuickReplies.setVisibility(View.GONE);
                    } else {
                        // User is the buyer
                        if (scrollQuickReplies != null) scrollQuickReplies.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void markAsSold() {
        if (listingId == null) return;
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Satıldı İşaretle")
            .setMessage("Bu ilanı satıldı olarak işaretlemek istediğinize emin misiniz?")
            .setPositiveButton("Evet", (dialog, which) -> {
                // İlanın durumunu güncelle
                db.collection("Listings").document(listingId).update("status", "sold").addOnSuccessListener(aVoid -> {
                    android.widget.Toast.makeText(ChatActivity.this, "İlan satıldı olarak işaretlendi.", android.widget.Toast.LENGTH_SHORT).show();
                    // Otomatik mesaj gönder
                    etMessage.setText("Bu ürün satılmıştır. İlginiz için teşekkürler!");
                    sendMessage();
                    com.google.android.material.button.MaterialButton btnMarkAsSold = findViewById(R.id.btnMarkAsSold);
                    if (btnMarkAsSold != null) btnMarkAsSold.setVisibility(View.GONE); // Artık satıldı, butonu gizle
                });
            })
            .setNegativeButton("İptal", null)
            .show();
    }

    private void findOrCreateChat() {
        if (listingId != null) {
            db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .whereEqualTo("listingId", listingId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(otherUserId)) {
                            chatId = doc.getId();
                            listenToMessages();
                            return;
                        }
                    }
                    createNewChat();
                });
        } else {
            // İlan ID'si yoksa eski yöntemle ara
            db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(otherUserId)) {
                            chatId = doc.getId();
                            listenToMessages();
                            return;
                        }
                    }
                    createNewChat();
                });
        }
    }

    private void createNewChat() {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", Arrays.asList(currentUserId, otherUserId));
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", Timestamp.now());
        chatData.put("otherUserName", otherUserName != null ? otherUserName : "Kullanıcı");
        
        if (listingId != null) chatData.put("listingId", listingId);
        if (listingTitle != null) chatData.put("listingTitle", listingTitle);
        if (listingImageUrl != null) chatData.put("listingImageUrl", listingImageUrl);

        Map<String, Long> unreadCounts = new HashMap<>();
        unreadCounts.put(currentUserId, 0L);
        unreadCounts.put(otherUserId, 0L);
        chatData.put("unreadCounts", unreadCounts);

        String myName = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String dName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (dName != null && !dName.isEmpty()) {
                myName = dName;
            } else {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (email != null && email.contains("@")) {
                    myName = email.substring(0, email.indexOf("@"));
                }
            }
        }
        chatData.put("creatorName", myName);

        db.collection("Chats")
            .add(chatData)
            .addOnSuccessListener(documentReference -> {
                chatId = documentReference.getId();
                listenToMessages();
            });
    }

    private void listenToMessages() {
        // Sohbeti açtığımızda okunmamış sayısını sıfırla
        resetUnreadCount();

        db.collection("Chats").document(chatId).collection("Messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) return;
                messages.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    ChatMessage msg = doc.toObject(ChatMessage.class);
                    if (msg != null) messages.add(msg);
                }
                adapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
                
                resetUnreadCount(); // Yeni mesaj geldiğinde de açıkken sıfırla
            });
    }

    private void resetUnreadCount() {
        if (chatId != null && currentUserId != null) {
            db.collection("Chats").document(chatId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Map<String, Long> unreadCounts = (Map<String, Long>) doc.get("unreadCounts");
                    if (unreadCounts != null && unreadCounts.containsKey(currentUserId)) {
                        Long count = unreadCounts.get(currentUserId);
                        if (count != null && count > 0) {
                            unreadCounts.put(currentUserId, 0L);
                            db.collection("Chats").document(chatId).update("unreadCounts", unreadCounts);
                        }
                    }
                }
            });
        }
    }

    private void sendMessage() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (text.isEmpty() || chatId == null) return;

        ChatMessage msg = new ChatMessage(currentUserId, text, Timestamp.now());
        db.collection("Chats").document(chatId).collection("Messages").add(msg);

        // Son mesajı güncelle ve karşı tarafın okunmamış sayısını 1 artır
        db.collection("Chats").document(chatId).get().addOnSuccessListener(doc -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMessage", text);
            updates.put("lastMessageTime", Timestamp.now());
            
            if (doc.exists()) {
                Map<String, Long> unreadCounts = (Map<String, Long>) doc.get("unreadCounts");
                if (unreadCounts == null) unreadCounts = new HashMap<>();
                
                Long currentCount = unreadCounts.get(otherUserId);
                unreadCounts.put(otherUserId, currentCount != null ? currentCount + 1L : 1L);
                updates.put("unreadCounts", unreadCounts);
            }
            db.collection("Chats").document(chatId).update(updates);
        });

        etMessage.setText("");
    }
}
