package com.titan.thor;

import lombok.extern.java.Log;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@Log
public class Thor implements Runnable {

    public Thor() {}

    @Override
    public void run() {
        log.info("Started Thor...");
        try (ZContext context = new ZContext()) {
            ZMQ.Socket responder = context.createSocket(SocketType.REP);
            responder.connect("tcp://0.0.0.0:5560");

            while (!Thread.currentThread().isInterrupted()) {
                //  Wait for next request from client
                String string = responder.recvStr(0);
                System.out.printf("Received request: [%s]\n", string);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //  Send reply back to client
                responder.send("You sent me: " + string);
            }
        }
    }

}
