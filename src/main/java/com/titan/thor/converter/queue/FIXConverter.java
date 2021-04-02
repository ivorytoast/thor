package com.titan.thor.converter.queue;

import com.titan.thor.model.Order;

public class FIXConverter {

    public static Order convertFixToOrder(String fix) {
        String[] values = fix.split("\\?\\d");
        String userID = values[1].substring(1);
        String symbol = values[2].substring(1);
        String quantity = values[3].substring(1);
        String price = values[4].substring(1);
        String side = values[5].substring(1);
        return new Order(null, userID, symbol, Long.parseLong(quantity), Double.parseDouble(price), side);
    }

    public String convertOrderToFix(Order order) {
        /*
        TODO: All of it
         */
        return "";
    }

}
