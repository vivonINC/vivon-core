package com.vivoninc.model;

public class Message {
    private int ID;
    private int conversationID;
    private String content;
    private int senderID;
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

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String text) {
        this.content = text;
    }

}
