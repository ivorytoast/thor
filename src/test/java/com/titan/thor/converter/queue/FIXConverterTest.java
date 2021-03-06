package com.titan.thor.converter.queue;

import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FIXConverterTest {

    String fixMessage = "8=FIX?1=ivorytoast?2=spx?3=78?4=67.30?5=buy";

    @Test
    public void fixToOrder() {
        Order order = FIXConverter.convertFixToOrder(fixMessage);
        Assertions.assertNull(order.getId());
        Assertions.assertEquals("ivorytoast", order.getUserID());
        Assertions.assertEquals("spx", order.getSymbol());
        Assertions.assertEquals(78L, order.getQuantity());
        Assertions.assertEquals(67.30, order.getPrice());
        Assertions.assertEquals("buy", order.getSide());
    }

    @Test
    public void fixToOrderRequest() {
        MawOrderRequest order = FIXConverter.convertFixToOrderRequest(fixMessage);
        Assertions.assertEquals("ivorytoast", order.getUserID());
        Assertions.assertEquals("spx", order.getSymbol());
        Assertions.assertEquals(78L, order.getQuantity());
        Assertions.assertEquals(67.30, order.getPrice());
        Assertions.assertEquals("buy", order.getSide());
    }

    @Test
    public void orderToFix() {
        Order order = FIXConverter.convertFixToOrder(fixMessage);
        String testFix = FIXConverter.convertOrderToFix(order);
    }

}
