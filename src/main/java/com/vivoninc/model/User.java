package com.vivoninc.model;

public class User {
    int Id;
    String userName;
    String password;
    Friend friends [];

    public User(){

    }

    public User(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }
}
