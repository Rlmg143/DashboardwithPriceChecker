package com.example.dashboardwithpricechecker;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String cost;
    private int quantity;
    private String tag;
    private String desc;
    private String unitSold;
    private String img;
    private String stockNo;

    public Item(String name, String cost, int quantity, String tag, String desc, String unitSold, String img, String stockNo) {
        this.name = name;
        this.cost = cost;
        this.quantity = quantity;
        this.tag = tag;
        this.desc = desc;
        this.unitSold = unitSold;
        this.img = img;
        this.stockNo = stockNo;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return cost;
    }

    public void setPrice(String cost) {
        this.cost = cost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnitSold() {
        return unitSold;
    }

    public String getImg() {
        return img;
    }

    public String getStockNo() {
        return stockNo;
    }

    @Override
    public String toString() {
        return name + "," + cost + "," + quantity + "," + tag + "," + desc + "," + unitSold + "," + img + "," + stockNo;
//        return "Item{" +
//                "name='" + name + '\'' +
//                ", price='" + cost + '\'' +
//                ", quantity=" + quantity +
//                ", tag='" + tag + '\'' +
//                ", desc='" + desc + '\'' +
//                '}';
    }
}
