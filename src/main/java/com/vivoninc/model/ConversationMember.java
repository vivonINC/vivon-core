package com.vivoninc.model;

public class ConversationMember {
    private int ID;
    private int conversationID;
    private int userID;

    public enum Role{
        MEMBER,
        ADMIN,
        OWNER
    }
    private Role role;

    public ConversationMember(){}

    public ConversationMember(int ID, int conversationID, int userID, Role role){
        this.ID = ID;
        this.conversationID = conversationID;
        this.userID = userID;
        this.role = role;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public int getConversationID() {
        return conversationID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public int getID() {
        return ID;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }
}
