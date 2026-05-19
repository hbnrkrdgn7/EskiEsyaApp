package com.example.eskiesyasatis;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private LinearLayout layoutEmptyState;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this::showOrderDetails);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).collection("orders")
                // optional: .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        }
                    }
                    orderAdapter.updateList(orderList);
                    updateUI();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Siparişler yüklenemedi.", Toast.LENGTH_SHORT).show());
    }

    private void updateUI() {
        if (orderList.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showOrderDetails(Order order) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_order_detail, null);

        android.widget.TextView tvSheetOrderDate = view.findViewById(R.id.tvSheetOrderDate);
        android.widget.TextView tvSheetOrderCard = view.findViewById(R.id.tvSheetOrderCard);
        android.widget.TextView tvSheetOrderTotal = view.findViewById(R.id.tvSheetOrderTotal);
        LinearLayout layoutSheetItems = view.findViewById(R.id.layoutSheetItems);
        com.google.android.material.button.MaterialButton btnSheetClose = view.findViewById(R.id.btnSheetClose);
        
        // Ürün resmini yükle
        ImageView ivSheetOrderImage = view.findViewById(R.id.ivSheetOrderImage);
        androidx.cardview.widget.CardView cardSheetImage = view.findViewById(R.id.cardSheetImage);
        
        if (order.getFirstItemImageUrl() != null && !order.getFirstItemImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(order.getFirstItemImageUrl())
                .centerCrop()
                .into(ivSheetOrderImage);
            cardSheetImage.setVisibility(View.VISIBLE);
        } else {
            cardSheetImage.setVisibility(View.GONE);
        }

        tvSheetOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "");
        tvSheetOrderCard.setText(order.getCardLastFour() != null ? order.getCardLastFour() : "");
        tvSheetOrderTotal.setText(order.getTotalAmount() + " TL");

        if (order.getItemNames() != null) {
            for (String itemName : order.getItemNames()) {
                View itemView = getLayoutInflater().inflate(R.layout.item_order_detail_row, layoutSheetItems, false);
                android.widget.TextView tvItemName = itemView.findViewById(R.id.tvItemName);
                tvItemName.setText(itemName);
                layoutSheetItems.addView(itemView);
            }
        }

        btnSheetClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }
}
