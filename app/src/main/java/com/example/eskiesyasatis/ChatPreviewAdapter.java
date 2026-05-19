package com.example.eskiesyasatis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener clickListener;
    private OnChatLongClickListener longClickListener;
    private String currentUserId;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public interface OnChatLongClickListener {
        void onChatLongClick(Chat chat);
    }

    public ChatPreviewAdapter(List<Chat> chatList, OnChatClickListener clickListener, OnChatLongClickListener longClickListener) {
        this.chatList = chatList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        
        holder.tvListingTitle.setText(chat.getListingTitle() != null ? chat.getListingTitle() : "İlan: Belirtilmemiş");
        holder.tvChatName.setText(chat.getOtherUserName() != null ? chat.getOtherUserName() : "Kullanıcı");
        
        String lastMsg = chat.getLastMessage();
        if (lastMsg == null || lastMsg.isEmpty()) {
            holder.tvLastMessage.setText("Henüz mesaj yok.");
        } else {
            holder.tvLastMessage.setText(lastMsg);
        }

        if (chat.getLastMessageTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            holder.tvChatTime.setText(sdf.format(chat.getLastMessageTime().toDate()));
        } else {
            holder.tvChatTime.setText("");
        }

        if (chat.getListingImageUrl() != null && !chat.getListingImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(chat.getListingImageUrl())
                .placeholder(R.drawable.ic_orders)
                .error(R.drawable.ic_orders)
                .centerCrop()
                .into(holder.ivListingImage);
        } else {
            holder.ivListingImage.setImageResource(R.drawable.ic_orders); // Fallback
        }

        // Unread Badge Check
        if (currentUserId != null && chat.getUnreadCounts() != null) {
            Long unread = chat.getUnreadCounts().get(currentUserId);
            if (unread != null && unread > 0) {
                holder.viewUnreadBadge.setVisibility(View.VISIBLE);
                holder.tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
                holder.tvChatTime.setTextColor(holder.itemView.getContext().getColor(R.color.primaryColor));
            } else {
                holder.viewUnreadBadge.setVisibility(View.GONE);
                holder.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.tvChatTime.setTextColor(holder.itemView.getContext().getColor(R.color.textColorSecondary));
            }
        } else {
            holder.viewUnreadBadge.setVisibility(View.GONE);
            holder.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tvChatTime.setTextColor(holder.itemView.getContext().getColor(R.color.textColorSecondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onChatClick(chat);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onChatLongClick(chat);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateList(List<Chat> newList) {
        this.chatList = newList;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvListingTitle;
        TextView tvChatName;
        TextView tvLastMessage;
        TextView tvChatTime;
        ImageView ivListingImage;
        View viewUnreadBadge;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListingTitle = itemView.findViewById(R.id.tvListingTitle);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvChatTime = itemView.findViewById(R.id.tvChatTime);
            ivListingImage = itemView.findViewById(R.id.ivListingImage);
            viewUnreadBadge = itemView.findViewById(R.id.viewUnreadBadge);
        }
    }
}
