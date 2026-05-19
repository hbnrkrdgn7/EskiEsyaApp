package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private TextView tvEmptyMessage;
    private ListingRepository.OnDataChangedListener dataChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        recyclerView = findViewById(R.id.recyclerViewMyListings);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        String currentUserId = FirebaseAuth.getInstance().getUid();
        List<Listing> myListings = ListingRepository.getInstance().getListingsByOwnerId(currentUserId);

        listingAdapter = new ListingAdapter(myListings);
        recyclerView.setAdapter(listingAdapter);

        updateEmptyState(myListings);

        dataChangedListener = () -> {
            List<Listing> updatedList = ListingRepository.getInstance().getListingsByOwnerId(currentUserId);
            listingAdapter.updateList(updatedList);
            updateEmptyState(updatedList);
        };
        ListingRepository.getInstance().setOnDataChangedListener(dataChangedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataChangedListener != null) {
            ListingRepository.getInstance().removeOnDataChangedListener(dataChangedListener);
        }
    }

    private void updateEmptyState(List<Listing> listings) {
        if (listings.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
