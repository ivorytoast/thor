package com.titan.thor;

import com.titan.thor.database.Wanda;
import com.titan.thor.model.Order;
import com.titan.thor.model.children.Symbol;
import lombok.extern.java.Log;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class Thor implements Runnable {

    private final Wanda wanda;
    private final Engine engine;

    private Map<String, Symbol> symbols;

    public Thor(Wanda wanda) {
        this.engine = new Engine();
        this.wanda = wanda;
        symbols = new HashMap<>();
        symbols.put("spx", new Symbol("spx"));
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

                // Convert FIX to Order POJO
//                Order order = engine.convertFixToOrder(fixMessageFromLoki);
                Order order = new Order(1L, "ivorytoast", "spx", 200L, 300.12, "buy");

                List<Order> ordersToUpdate = engine.acceptOrder(order);

                for (Order orderToUpdate : ordersToUpdate) {
                    log.info("Affected order: " + orderToUpdate.toString());
                    engine.updateOrder(orderToUpdate);
                }

                symbols.get("spx").bids.addOrder(order);
                if (wanda == null) {
                    log.info("Wanda is null");
                } else {
                    log.info("Wanda is NOT null");
                }
                this.wanda.save(order);

                responder.send("You sent me: " + fixMessageFromLoki);
            }
        }
    }

}
