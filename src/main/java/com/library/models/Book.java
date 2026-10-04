package com.library.models;

public class Book {

    private int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int quantity;
    private int availableQty;

    public Book(int id, String title, String author, String isbn,
                String category, int quantity, int availableQty) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.quantity = quantity;
        this.availableQty = availableQty;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    @Override
    public String toString() {
        return title + " - " + author + " (" + availableQty + " үлдсэн)";
    }
}