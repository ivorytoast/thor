package com.titan.thor.model;

public class MawCancel {

    private long orderID;

    public MawCancel() {}

    public MawCancel(long orderID) {
        this.orderID = orderID;
    }

    public long getOrderID() {
        return orderID;
    }

    public void setOrderID(long orderID) {
        this.orderID = orderID;
    }

}
