package com.example.project_game;

import java.io.Serializable;

public class Item implements Serializable {
    protected String name;
    protected String state;

    public Item(String name, String state){
        this.name=name;
        this.state=state;
    }
}
