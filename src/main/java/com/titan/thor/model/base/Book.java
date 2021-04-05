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

    public Order removeMatchedOrder(long orderID) {
        return this.tesseract.removeMatchedOrder(orderID);
    }

    public void updateOrder(long orderID, long quantityToChange) {
        this.tesseract.update(orderID, quantityToChange);
    }

    public void cancelOrder(long orderID) {
        this.tesseract.removeCancelledOrder(orderID);
    }

    public Order findOrder(long orderID) {
        return this.tesseract.find(orderID);
    }

    public BookType getBookType() {
        return bookType;
    }

}
