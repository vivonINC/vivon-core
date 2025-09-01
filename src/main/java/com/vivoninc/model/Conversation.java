package com.vivoninc.model;

public class Conversation {
    private int Id;
    private String name;
    public enum Type{
        GROUP,
        DIRECT
    }
    private Type type;

    public Conversation(){}

    public Conversation(int Id, String name, Type type){
        this.type = type;
        this.Id = Id;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
