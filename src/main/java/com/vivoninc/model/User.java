package com.vivoninc.model;

public class User {
    int Id;
    String email;
    String avatar; //Path
    String username;
    String password;
    Friend friends [];

    public User(){

    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public User(int Id, String email, String password){
        this.Id = Id;
        this.email = email;
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }
}
