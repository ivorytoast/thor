package com.titan.thor.database;

import com.titan.thor.database.converter.Converter;
import com.titan.thor.database.repository.Pietro;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
public class Wanda {

    @Autowired
    private Pietro orderRepository;

    public Wanda() {}

    public boolean save(Order order) {
        OrderDAO orderDAO = new OrderDAO();
        try {
            Converter.viewToDataModelConverter(order, orderDAO);
            orderRepository.save(orderDAO);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
