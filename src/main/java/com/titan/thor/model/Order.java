package com.titan.thor.model;

import java.util.Objects;

public class Order {
    private Long id;
    private String userID;
    private String symbol;
    private Long quantity;
    private Double price;
    private String side;
    private Long quantityRemaining;

    public Long getId() {
        return id;
    }
    public String getUserID() { return userID; }
    public Long getQuantity() {
        return quantity;
    }
    public String getSymbol() {
        return symbol;
    }
    public Double getPrice() { return price; }
    public String getSide() { return side; }
    public Long getQuantityRemaining() { return quantityRemaining; }

    public void setId(Long id) {
        this.id = id;
    }
    public void setUserID(String userID) { this.userID = userID; }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public void setPrice(Double price) { this.price = price; }
    public void setSide(String side) { this.side = side; }
    public void setQuantityRemaining(Long quantityRemaining) { this.quantityRemaining = quantityRemaining; }

    public Order() {}

    public Order(Long id, String userID, String symbol, Long quantity, Double price, String side) {
        this.id = id;
        this.userID = userID;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.quantityRemaining = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order orderView = (Order) o;
        return Objects.equals(id, orderView.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderView [id=" + this.id + ", userID=" + this.userID + ", symbol=" + this.symbol +
                ", quantity=" + this.quantity + ", price=" + this.price +
                ", side=" + this.side + ", remaining quantity=" + this.quantityRemaining + "]";
    }

}
