package com.vivoninc.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private int ID;
    @JsonProperty("conversation_id")
    private int conversationID;
    private String content;
        @JsonProperty("sender_id")
    private String senderID;
    @JsonProperty("created_at")
    private java.sql.Timestamp dateSent;
    private java.sql.Timestamp dateDeleted;
    public enum MsgType{
        TEXT,
        IMAGE,
        SYSTEM
    }
    private MsgType type;

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public int getConversationID() {
        return conversationID;
    }
    public void setDateDeleted(java.sql.Timestamp dateDeleted) {
        this.dateDeleted = dateDeleted;
    }

    public java.sql.Timestamp getDateDeleted() {
        return dateDeleted;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public int getID() {
        return ID;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public MsgType getType() {
        return type;
    }
    
    public java.sql.Timestamp getDateSent() {
        return dateSent;
    }

    public void setDateSent(java.sql.Timestamp dateSent) {
        this.dateSent = dateSent;
    }

    public int getReceiverID() {
        return conversationID;
    }

    public void setReceiverID(int receiverID) {
        this.conversationID = receiverID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String text) {
        this.content = text;
    }

}
