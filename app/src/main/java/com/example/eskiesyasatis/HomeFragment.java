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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHome);
        
        // Letgo tarzı 2 sütunlu ızgara görünümü
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        listingAdapter = new ListingAdapter(ListingRepository.getInstance().getListings());
        recyclerView.setAdapter(listingAdapter);

        ListingRepository.getInstance().setOnDataChangedListener(() -> {
            if (listingAdapter != null) {
                listingAdapter.updateList(ListingRepository.getInstance().getListings());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Anasayfaya dönüldüğünde (ilan eklendikten sonra) listeyi tazele
        if (listingAdapter != null) {
            listingAdapter.updateList(ListingRepository.getInstance().getListings());
        }
    }
}
