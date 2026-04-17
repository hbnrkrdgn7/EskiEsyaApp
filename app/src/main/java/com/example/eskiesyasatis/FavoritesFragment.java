package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyFavorites;
    private ListingAdapter listingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites);
        
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        List<Listing> favorites = FavoritesRepository.getInstance().getFavoriteListings();
        listingAdapter = new ListingAdapter(favorites);
        recyclerView.setAdapter(listingAdapter);

        updateEmptyState();

        // Firestore değişikliklerini dinle
        FavoritesRepository.getInstance().setOnFavoritesChangedListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    listingAdapter.updateList(FavoritesRepository.getInstance().getFavoriteListings());
                    updateEmptyState();
                });
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listingAdapter != null) {
            listingAdapter.updateList(FavoritesRepository.getInstance().getFavoriteListings());
            updateEmptyState();
        }
    }
    
    private void updateEmptyState() {
        if(listingAdapter.getItemCount() == 0) {
            tvEmptyFavorites.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyFavorites.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
