package com.example.eskiesyasatis;

import com.google.firebase.Timestamp;
import java.util.List;

public class Card {
    private String id;
    private String cardNumber;
    private String maskedNumber;
    private String holderName;
    private String expiryDate;

    public Card() {}

    public Card(String id, String cardNumber, String holderName, String expiryDate) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.maskedNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        this.holderName = holderName;
        this.expiryDate = expiryDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getMaskedNumber() { return maskedNumber; }
    public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
