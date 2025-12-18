package com.foodregistry;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<Integer> quantities;
    private int itemCount;
    private float subtotal;
    private float sst;
    private float total;
    private final float SST_RATE = 0.10f;

    public Order(int count) {
        this.itemCount = count;
        this.quantities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            quantities.add(0);
        }
        this.subtotal = 0;
        this.sst = 0;
        this.total = 0;
    }

    // Copy constructor
    public Order(Order other) {
        this.itemCount = other.itemCount;
        this.quantities = new ArrayList<>(other.quantities);
        this.subtotal = other.subtotal;
        this.sst = other.sst;
        this.total = other.total;
    }

    public void addItem(int index, int quantity) {
        if (index >= 0 && index < itemCount) {
            int current = quantities.get(index);
            int newQty = current + quantity;
            if (newQty < 0) newQty = 0;
            quantities.set(index, newQty);
        }
    }

    public int getQuantity(int index) {
        return (index >= 0 && index < itemCount) ? quantities.get(index) : 0;
    }

    public void calculateTotals(List<MenuItem> menu) {
        subtotal = 0;
        for (int i = 0; i < itemCount; i++) {
            subtotal += menu.get(i).getPrice() * quantities.get(i);
        }
        sst = subtotal * SST_RATE;
        total = subtotal + sst;
    }

    public float getSubtotal() { return subtotal; }
    public float getSST() { return sst; }
    public float getTotal() { return total; }

    public boolean isEmpty() {
        for (int qty : quantities) {
            if (qty > 0) return false;
        }
        return true;
    }

    public void clear() {
        for (int i = 0; i < itemCount; i++) {
            quantities.set(i, 0);
        }
        subtotal = 0;
        sst = 0;
        total = 0;
    }
}
