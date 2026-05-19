package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyCardsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCards;
    private LinearLayout layoutEmptyState;
    private CardAdapter cardAdapter;
    private List<Card> cardList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cards);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerViewCards = findViewById(R.id.recyclerViewCards);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        cardList = new ArrayList<>();
        cardAdapter = new CardAdapter(cardList, this::deleteCard);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCards.setAdapter(cardAdapter);

        loadCards();
    }

    private void loadCards() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).collection("cards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cardList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Card card = doc.toObject(Card.class);
                        if (card != null) {
                            card.setId(doc.getId());
                            cardList.add(card);
                        }
                    }
                    cardAdapter.updateList(cardList);
                    updateUI();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Kartlar yüklenemedi.", Toast.LENGTH_SHORT).show());
    }

    private void deleteCard(Card card) {
        if (mAuth.getCurrentUser() == null || card.getId() == null) return;
        
        new android.app.AlertDialog.Builder(this)
                .setTitle("Kartı Sil")
                .setMessage("Bu kartı silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    String userId = mAuth.getCurrentUser().getUid();
                    db.collection("Users").document(userId).collection("cards").document(card.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Kart silindi.", Toast.LENGTH_SHORT).show();
                                loadCards(); // Reload the list
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Silme başarısız.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void updateUI() {
        if (cardList.isEmpty()) {
            recyclerViewCards.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCards.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}
