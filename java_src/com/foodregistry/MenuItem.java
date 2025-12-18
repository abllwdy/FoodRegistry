package com.foodregistry;

public class MenuItem {
    private char code;
    private String name;
    private float price;

    public MenuItem(char c, String n, float p) {
        this.code = c;
        this.name = n;
        this.price = p;
    }

    public char getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }
}
