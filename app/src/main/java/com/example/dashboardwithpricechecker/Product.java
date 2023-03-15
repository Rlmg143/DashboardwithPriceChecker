package com.example.dashboardwithpricechecker;

public class Product {
    private String id;
    private String Name;
    private int image;

    private String Category;

    public Product(String id, String name, int image, String category) {
        this.id = id;
        Name = name;
        this.image = image;
        Category = category;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
