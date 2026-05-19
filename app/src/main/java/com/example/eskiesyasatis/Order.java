package com.example.eskiesyasatis;

import java.util.List;

public class Order {
    private String orderId;
    private String orderDate;
    private double totalAmount;
    private List<String> itemNames;
    private String cardLastFour;
    private String firstItemImageUrl;

    public Order() {}

    public Order(String orderId, String orderDate, double totalAmount, List<String> itemNames, String cardLastFour, String firstItemImageUrl) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.itemNames = itemNames;
        this.cardLastFour = cardLastFour;
        this.firstItemImageUrl = firstItemImageUrl;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public List<String> getItemNames() { return itemNames; }
    public void setItemNames(List<String> itemNames) { this.itemNames = itemNames; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public String getFirstItemImageUrl() { return firstItemImageUrl; }
    public void setFirstItemImageUrl(String firstItemImageUrl) { this.firstItemImageUrl = firstItemImageUrl; }
}
