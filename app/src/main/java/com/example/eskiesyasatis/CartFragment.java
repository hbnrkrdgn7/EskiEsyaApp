package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private TextView tvCartTotal, tvCheckoutTotal, tvEmptyCart;
    private CardView cardCheckout;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        tvCartTotal = view.findViewById(R.id.tvCartTotal);
        tvCheckoutTotal = view.findViewById(R.id.tvCheckoutTotal);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);
        cardCheckout = view.findViewById(R.id.cardCheckout);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

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
                Toast.makeText(getContext(), "Sepetiniz boş!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Siparişiniz alındı! Toplam: " + calculateTotal(currentItems) + " TL", Toast.LENGTH_LONG).show();
            }
        });

        return view;
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
