package com.titan.thor;

import com.titan.thor.converter.queue.FIXConverter;
import com.titan.thor.database.Wanda;
import com.titan.thor.database.converter.Converter;
import com.titan.thor.model.MawCancel;
import com.titan.thor.model.MawNew;
import com.titan.thor.model.Order;
import com.titan.thor.model.children.Symbol;
import com.titan.thor.model.dao.OrderDAO;
import lombok.extern.java.Log;
import org.aspectj.weaver.ast.Or;
import org.springframework.transaction.annotation.Transactional;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Transactional
public class Thor implements Runnable {

    private final Wanda wanda;
    private final Engine engine;

    private Map<String, Symbol> symbols;

    public Thor(Wanda wanda) {
        this.engine = new Engine(wanda);
        this.wanda = wanda;
        symbols = new HashMap<>();
        symbols.put("spx", new Symbol("spx"));
    }

    public void mawNew(MawNew mawNew) {
        String fixMessage = mawNew.getFixMessage();
        log.info("Received new order request from Maw: " + fixMessage);

        Order order = FIXConverter.convertFixToOrder(fixMessage);
        long createdID = this.wanda.addOrderToDatabase(order);
        order.setId(createdID);
        log.info("Order being sent down to the engine: " + order.toString());

        engine.addNewOrder(order);

        log.info("Order getting sent back to Maw: " + FIXConverter.convertOrderToFix(order));
    }

    public void mawCancel(MawCancel mawCancel) {
        long orderID = mawCancel.getOrderID();
        log.info("Received new cancel request from Maw for orderID: " + orderID);
        OrderDAO orderDAO = wanda.getOneOrderFromDatabase(orderID);
        Order order = new Order();
        Converter.createNewOrderFromDAO(order, orderDAO);

        wanda.cancelOrder(orderID);
        engine.cancelOrder(order);

        log.info("OrderID " + orderID + "cancelled");
    }

    @Override
    public void run() {
        log.info("Started Thor...");
        try (ZContext context = new ZContext()) {
            ZMQ.Socket responder = context.createSocket(SocketType.REP);

            boolean didConnect = responder.connect("tcp://bifrost:5560");
            log.info("(Thor) Connected to bifrost: " + didConnect);

            while (!Thread.currentThread().isInterrupted()) {
                String fixMessageFromLoki = responder.recvStr(0);
                System.out.printf("Received request: [%s]\n", fixMessageFromLoki);

                // Convert from FIX to POJO, save to database, and enrich with new ID
                Order order = FIXConverter.convertFixToOrder(fixMessageFromLoki);
                long createdID = this.wanda.addOrderToDatabase(order);
                order.setId(createdID);
                log.info("Order being sent down to the engine: " + order.toString());

                List<Order> ordersToUpdate = engine.acceptOrder(order);

                for (Order orderToUpdate : ordersToUpdate) {
                    log.info("Affected order: " + orderToUpdate.toString());
                    engine.updateOrder(orderToUpdate);
                }

                log.info("Order Thor is going to send back to Loki: " + order.toString());
                responder.send("From Thor -> " + fixMessageFromLoki);
            }
        }
    }

}
