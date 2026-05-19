package com.example.eskiesyasatis;

import com.google.firebase.Timestamp;
import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private String otherUserName;
    private String otherUserId;
    private String listingId;
    private String listingTitle;
    private String listingImageUrl;
    private java.util.Map<String, Long> unreadCounts;

    public Chat() {}

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Timestamp lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getListingImageUrl() { return listingImageUrl; }
    public void setListingImageUrl(String listingImageUrl) { this.listingImageUrl = listingImageUrl; }

    public java.util.Map<String, Long> getUnreadCounts() { return unreadCounts; }
    public void setUnreadCounts(java.util.Map<String, Long> unreadCounts) { this.unreadCounts = unreadCounts; }
}
