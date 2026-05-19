package com.example.eskiesyasatis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cardList;
    private OnCardDeleteListener deleteListener;

    public interface OnCardDeleteListener {
        void onDeleteClick(Card card);
    }

    public CardAdapter(List<Card> cardList, OnCardDeleteListener deleteListener) {
        this.cardList = cardList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.tvCardNumber.setText(card.getMaskedNumber());
        holder.tvCardHolder.setText(card.getHolderName());
        holder.tvCardExpiry.setText(card.getExpiryDate());

        holder.btnDeleteCard.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList != null ? cardList.size() : 0;
    }

    public void updateList(List<Card> newList) {
        this.cardList = newList;
        notifyDataSetChanged();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber, tvCardHolder, tvCardExpiry;
        ImageView btnDeleteCard;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvCardHolder = itemView.findViewById(R.id.tvCardHolder);
            tvCardExpiry = itemView.findViewById(R.id.tvCardExpiry);
            btnDeleteCard = itemView.findViewById(R.id.btnDeleteCard);
        }
    }
}
