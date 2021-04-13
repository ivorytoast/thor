package com.titan.thor.database.converter;

import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConverterTest {

    @Test
    public void givenDao_convertToOrder() {
        Order order = new Order();
        OrderDAO testDAO = new OrderDAO();

        testDAO.setId(1L);
        testDAO.setUserID("id");
        testDAO.setQuantity(2L);
        testDAO.setSymbol("symbol");
        testDAO.setPrice(3.0);
        testDAO.setSide("buy");
        testDAO.setQuantityRemaining(4L);
        testDAO.setCancelled(false);

        Converter.createNewOrderFromDAO(order, testDAO);

        Assertions.assertEquals(testDAO.getId(), order.getId());
        Assertions.assertEquals(testDAO.getUserID(), order.getUserID());
        Assertions.assertEquals(testDAO.getQuantity(), order.getQuantity());
        Assertions.assertEquals(testDAO.getSymbol(), order.getSymbol());
        Assertions.assertEquals(testDAO.getPrice(), order.getPrice());
        Assertions.assertEquals(testDAO.getSide(), order.getSide());
        Assertions.assertEquals(testDAO.getQuantityRemaining(), order.getQuantityRemaining());
        Assertions.assertEquals(testDAO.isCancelled(), order.getCancelled());
    }

    @Test
    public void givenRequest_convertToDao() {
        OrderDAO testDAO = new OrderDAO();
        MawOrderRequest testRequest = new MawOrderRequest();

        testRequest.setUserID("id");
        testRequest.setSymbol("symbol");
        testRequest.setQuantity(1L);
        testRequest.setPrice(2.0);
        testRequest.setSide("sell");

        Converter.createNewDatabaseOrderFromMawRequest(testRequest, testDAO);

        Assertions.assertEquals(testRequest.getUserID(), testDAO.getUserID());
        Assertions.assertEquals(testRequest.getSymbol(), testDAO.getSymbol());
        Assertions.assertEquals(testRequest.getQuantity(), testDAO.getQuantity());
        Assertions.assertEquals(testRequest.getPrice(), testDAO.getPrice());
        Assertions.assertEquals(testDAO.getSide(), testDAO.getSide());
    }

}
