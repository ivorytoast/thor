package com.titan.thor.model.dao;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "ORDERS")
public class OrderDAO {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @NotNull
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "USER_ID")
    private String userID;

    @NotNull
    @Column(name = "SYMBOL")
    private String symbol;

    @NotNull
    @Column(name = "QUANTITY")
    private Long quantity;

    @NotNull
    @Column(name = "PRICE")
    private Double price;

    @NotNull
    @Column(name = "SIDE")
    private String side;

    @NotNull
    @Column(name = "QUANTITY_REMAINING")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDAO orderData = (OrderDAO) o;
        return Objects.equals(id, orderData.id) && Objects.equals(userID, orderData.userID) && Objects.equals(symbol, orderData.symbol)
                && Objects.equals(quantity, orderData.quantity) && Objects.equals(price, orderData.price)
                && Objects.equals(side, orderData.side) && Objects.equals(quantityRemaining, orderData.quantityRemaining);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userID, symbol, quantity, price, side, quantityRemaining);
    }

    public OrderDAO() {}

    public OrderDAO(@NotNull String symbol, @NotNull String userID, @NotNull Long quantity, @NotNull Double price, @NotNull String side, @NotNull Long quantityRemaining) {
        this.symbol = symbol;
        this.userID = userID;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.quantityRemaining = quantityRemaining;
    }

    public OrderDAO(@NotNull Long id, @NotNull String userID, @NotNull String symbol, @NotNull Long quantity, @NotNull Double price, @NotNull String side, @NotNull Long quantityRemaining) {
        this.id = id;
        this.userID = userID;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.quantityRemaining = quantityRemaining;
    }

    @Override
    public String toString() {
        return "Order [id=" + this.id + ", userID=" + this.userID + ", symbol=" + this.symbol +
                ", quantity=" + this.quantity + ", price=" + this.price +
                ", side=" + this.side + ", quantity remaining=" + this.quantityRemaining + "]";
    }

}
