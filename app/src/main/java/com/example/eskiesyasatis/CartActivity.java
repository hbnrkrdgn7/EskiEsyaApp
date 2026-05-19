package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private TextView tvCartTotal, tvCheckoutTotal, tvEmptyCart;
    private CardView cardCheckout;
    private Button btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewCart);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        cardCheckout = findViewById(R.id.cardCheckout);
        btnCheckout = findViewById(R.id.btnCheckout);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Listing> cartItems = CartRepository.getInstance().getCartListings();
        listingAdapter = new ListingAdapter(cartItems);
        recyclerView.setAdapter(listingAdapter);

        updateUI(cartItems);

        CartRepository.getInstance().setOnCartChangedListener(() -> {
            List<Listing> updatedList = CartRepository.getInstance().getCartListings();
            listingAdapter.updateList(updatedList);
            updateUI(updatedList);
        });

        btnCheckout.setOnClickListener(v -> {
            List<Listing> currentItems = CartRepository.getInstance().getCartListings();
            if (currentItems.isEmpty()) {
                Toast.makeText(this, "Sepetiniz boş!", Toast.LENGTH_SHORT).show();
            } else {
                double total = calculateTotal(currentItems);
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("totalAmount", total);
                startActivity(intent);
            }
        });
    }

    private void updateUI(List<Listing> items) {
        String totalText = calculateTotal(items) + " TL";
        tvCartTotal.setText(totalText);
        tvCheckoutTotal.setText(totalText);

        if (items.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            cardCheckout.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            cardCheckout.setVisibility(View.VISIBLE);
        }
    }

    private double calculateTotal(List<Listing> items) {
        double total = 0;
        for (Listing item : items) {
            try {
                total += Double.parseDouble(item.getPrice());
            } catch (NumberFormatException e) {
                // Fiyat parse edilemezse atla
            }
        }
        return total;
    }
}
