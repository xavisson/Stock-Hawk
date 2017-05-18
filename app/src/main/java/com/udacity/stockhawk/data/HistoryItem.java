package com.udacity.stockhawk.data;

/**
 * Created by javidelpalacio on 18/5/17.
 */

public class HistoryItem {

    private String date;
    private float price;

    public HistoryItem(String date, float price) {
        this.date = date;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
