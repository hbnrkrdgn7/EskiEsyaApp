package com.example.eskiesyasatis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private List<Listing> listingList;

    public ListingAdapter(List<Listing> listingList) {
        this.listingList = listingList;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listingList.get(position);
        Context context = holder.itemView.getContext();
        
        holder.tvListingTitle.setText(listing.getTitle());
        holder.tvListingPrice.setText(listing.getPrice() + " TL");

        if (listing.getImageUriString() != null) {
            try {
                holder.ivListingImage.setImageURI(Uri.parse(listing.getImageUriString()));
            } catch (Exception e) {
                holder.ivListingImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            // Fallback for mock items
            holder.ivListingImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        
        // Favorite Logic - Kullanıcıya özel
        boolean isFav = FavoritesRepository.getInstance().isFavorite(listing.getId());
        updateFavoriteIcon(holder.btnFavorite, isFav, context);
        
        holder.btnFavorite.setOnClickListener(v -> {
            boolean currentlyFav = FavoritesRepository.getInstance().isFavorite(listing.getId());
            if (currentlyFav) {
                FavoritesRepository.getInstance().removeFavorite(listing.getId());
                updateFavoriteIcon(holder.btnFavorite, false, context);
                Toast.makeText(context, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show();
            } else {
                FavoritesRepository.getInstance().addFavorite(listing);
                updateFavoriteIcon(holder.btnFavorite, true, context);
                Toast.makeText(context, "Favorilere eklendi", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Item Click Logic
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListingDetailActivity.class);
            intent.putExtra("LISTING_ID", listing.getId());
            context.startActivity(intent);
        });
    }

    private void updateFavoriteIcon(ImageButton btnFavorite, boolean isFavorite, Context context) {
        if (isFavorite) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.primaryColor));
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return listingList.size();
    }

    public void updateList(List<Listing> newList) {
        this.listingList = newList;
        notifyDataSetChanged();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivListingImage;
        TextView tvListingPrice;
        TextView tvListingTitle;
        ImageButton btnFavorite;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivListingImage = itemView.findViewById(R.id.ivListingImage);
            tvListingPrice = itemView.findViewById(R.id.tvListingPrice);
            tvListingTitle = itemView.findViewById(R.id.tvListingTitle);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
