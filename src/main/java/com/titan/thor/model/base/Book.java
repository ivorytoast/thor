package com.titan.thor.model.base;

import com.titan.thor.cache.Tesseract;
import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;

@Log
public abstract class Book {

    public BookType bookType;

    public Tesseract tesseract;

    public Book(BookType bookType, Tesseract cache) {
        this.bookType = bookType;
        this.tesseract = cache;
    }

    public void addOrder(Order order) {
        this.tesseract.add(order);
    }

    public Order removeOrder(long orderID) {
        return this.tesseract.remove(orderID);
    }

    public void updateOrder(Order order, long quantityToChange) {
        this.tesseract.update(order.getId(), quantityToChange);
    }

    public void cancelOrder(long orderID) {
        this.tesseract.cancel(orderID);
    }

    public Order findOrder(long orderID) {
        return this.tesseract.find(orderID);
    }

    public BookType getBookType() {
        return bookType;
    }

}
