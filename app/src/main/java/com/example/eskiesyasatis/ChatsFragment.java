package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatPreviewAdapter adapter;
    private TextView tvEmptyChats;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration chatsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerView = view.findViewById(R.id.recyclerViewChats);
        tvEmptyChats = view.findViewById(R.id.tvEmptyChats);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatPreviewAdapter(chatList,
            chat -> {
                // Sohbete git
                Intent intent = new Intent(getContext(), ChatActivity.class);
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
                new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Sohbeti Sil")
                    .setMessage("Bu sohbeti silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        db.collection("Chats").document(chat.getChatId()).delete();
                        Toast.makeText(getContext(), "Sohbet silindi.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                    .show();
            }
        );
        recyclerView.setAdapter(adapter);

        loadChats();

        return view;
    }

    private void loadChats() {
        if (currentUserId == null) return;

        chatsListener = db.collection("Chats")
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
}
