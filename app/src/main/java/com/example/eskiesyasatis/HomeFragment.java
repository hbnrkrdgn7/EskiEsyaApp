package com.example.eskiesyasatis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ListingRepository.OnDataChangedListener dataChangedListener;
    private TextInputEditText etSearch;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerFrameLayout;
    private String currentSearchQuery = "";
    private ChipGroup chipGroupCategories;
    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHome);
        etSearch = view.findViewById(R.id.etSearch);
        shimmerFrameLayout = view.findViewById(R.id.shimmerViewContainer);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        
        setupCategoryChips();

        shimmerFrameLayout.startShimmer();
        
        // Letgo tarzı 2 sütunlu ızgara görünümü
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        listingAdapter = new ListingAdapter(new ArrayList<>());
        recyclerView.setAdapter(listingAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterListings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dataChangedListener = () -> {
            if (isAdded()) {
                filterListings();
            }
        };
        ListingRepository.getInstance().setOnDataChangedListener(dataChangedListener);
        
        filterListings();

        return view;
    }

    private void setupCategoryChips() {
        String[] categories = {"Tümü", "Elektronik", "Ev Eşyası", "Giyim", "Kitap & Hobi", "Araç İçi", "Diğer"};
        
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            
            if (category.equals("Tümü")) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (category.equals("Tümü")) {
                        selectedCategory = "";
                    } else {
                        selectedCategory = category;
                    }
                    filterListings();
                }
            });

            chipGroupCategories.addView(chip);
        }
    }

    private void filterListings() {
        if (listingAdapter == null) return;
        
        if (ListingRepository.getInstance().isLoaded()) {
            if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
        
        List<Listing> allActive = ListingRepository.getInstance().getActiveListings();
        
        if (currentSearchQuery.isEmpty() && selectedCategory.isEmpty()) {
            listingAdapter.updateList(allActive);
            return;
        }

        List<Listing> filteredList = new ArrayList<>();
        for (Listing listing : allActive) {
            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                boolean matchesTitle = listing.getTitle() != null && listing.getTitle().toLowerCase().contains(currentSearchQuery);
                boolean matchesDesc = listing.getDescription() != null && listing.getDescription().toLowerCase().contains(currentSearchQuery);
                matchesSearch = matchesTitle || matchesDesc;
            }

            boolean matchesCategory = true;
            if (!selectedCategory.isEmpty()) {
                matchesCategory = listing.getCategory() != null && listing.getCategory().equals(selectedCategory);
            }

            if (matchesSearch && matchesCategory) {
                filteredList.add(listing);
            }
        }
        listingAdapter.updateList(filteredList);
    }

    @Override
    public void onResume() {
        super.onResume();
        filterListings();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataChangedListener != null) {
            ListingRepository.getInstance().removeOnDataChangedListener(dataChangedListener);
        }
    }
}
