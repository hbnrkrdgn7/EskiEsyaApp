package com.example.eskiesyasatis;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener clickListener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener clickListener) {
        this.orderList = orderList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvOrderTotal.setText(order.getTotalAmount() + " TL");
        
        String itemsStr = String.join(", ", order.getItemNames());
        holder.tvOrderItems.setText(itemsStr);
        
        holder.tvOrderCard.setText("Kart: " + order.getCardLastFour());

        if (order.getFirstItemImageUrl() != null && !order.getFirstItemImageUrl().isEmpty()) {
            holder.ivOrderImage.setImageTintList(null);
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(order.getFirstItemImageUrl())
                .placeholder(R.drawable.ic_orders)
                .error(R.drawable.ic_orders)
                .centerCrop()
                .into(holder.ivOrderImage);
        } else {
            holder.ivOrderImage.setImageResource(R.drawable.ic_orders);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrderImage;
        TextView tvOrderDate, tvOrderTotal, tvOrderItems, tvOrderCard;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrderImage = itemView.findViewById(R.id.ivOrderImage);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderCard = itemView.findViewById(R.id.tvOrderCard);
        }
    }
}
