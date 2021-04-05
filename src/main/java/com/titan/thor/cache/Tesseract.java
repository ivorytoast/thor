package com.titan.thor.cache;

import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;
import redis.clients.jedis.Jedis;

import java.util.*;

@Log
public class Tesseract implements InfinityStone {

    private final String underlier;
    private final String bookType;

    private final Jedis jedis;

    public Tesseract(BookType bookType, String underlier) {
        this.underlier = underlier;

        if (bookType == BookType.BID) {
            this.bookType = "bids";
        } else {
            this.bookType = "asks";
        }

        jedis = new Jedis("redis");
    }

    @Override
    public void add(Order order) {
        long id = order.getId();
        double price = order.getPrice();
        long quantity = order.getQuantity();

        if (doesOrderExistInCache(id)) {
            log.info("Order already exists in Cache. Do not use add -- use update");
            return;
        }

        if (doesOrderExistInOGP(id, price)) {
            log.info("Order already exists in OGP. Do not use add -- use update");
            return;
        }

        cacheAdd(order);
        sppIncr(price, quantity);
        ogpAdd(price, id);
        aaIncrBy(quantity);
    }

    @Override
    public Order removeMatchedOrder(long orderID) {
        if (!doesOrderExistInCache(orderID)) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

        Order order = find(orderID);

        double price = order.getPrice();
        long quantityToRemove = order.getQuantityRemaining();

        order.setQuantityRemaining(0L);

        cacheRemove(orderID);
        ogpRemove(price, orderID);
        sppDecr(price, quantityToRemove);
        aaDecrBy(quantityToRemove);
        coAdd(orderID);

        return order;
    }

    @Override
    public Order removeCancelledOrder(long orderID) {
        if (!doesOrderExistInCache(orderID)) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

        Order order = find(orderID);

        double price = order.getPrice();
        long quantityToRemove = order.getQuantityRemaining();

        cacheRemove(orderID);
        ogpRemove(price, orderID);
        sppDecr(price, quantityToRemove);
        aaDecrBy(quantityToRemove);
        coAdd(orderID);

        return order;
    }

    @Override
    public void update(long orderID, long quantityToRemove) {
        Order cacheOrder = find(orderID);

        double price = cacheOrder.getPrice();

        sppDecr(price, quantityToRemove);
        cacheUpdate(orderID, quantityToRemove);
        aaDecrBy(quantityToRemove);
    }

    @Override
    public Order find(long orderID) {
        if (!doesOrderExistInCache(orderID)) {
            log.info("Can't find something that does not exist!");
            return null;
        }

        Map<String, String> values = jedis.hgetAll(cacheKey(orderID));

        if (values.isEmpty()) {
            log.info("There were no values returned from redis during the find method");
            return null;
        }

        log.info("Finished finding the order in REDIS: " + orderID);

        return convertHashToOrder(values);
    }

    public LinkedList<Double> getPrices() {
        LinkedList<Double> pricesToReturn = new LinkedList<>();
        Set<String> prices;
        if (bookType.equals("bids")) {
            prices = jedis.zrevrange(pricesKey(), 0, -1);
        } else {
            prices = jedis.zrange(pricesKey(), 0, -1);
        }
        for (String orderID : prices) {
            pricesToReturn.add(Double.parseDouble(orderID));
        }
        return pricesToReturn;
    }
    public List<Order> getOGPOrdersForPrice(double price) {
        List<Order> ordersToReturn = new ArrayList<>();
        Set<String> orders = jedis.zrange(ogpKey(price), 0, -1);
        for (String orderID : orders) {
            ordersToReturn.add(find(Long.parseLong(orderID)));
        }
        return ordersToReturn;
    }
    public long getAmountAvailable() {
        String value = jedis.get(aaKey());
        return (jedis.get(aaKey()) == null) ? -1 : Long.parseLong(value);
    }

    private String aaKey() {
        return getUnderlier() + ":" + getBookType() + ":" + "aa";
    }
    private void aaIncrBy(long quantity) {
        jedis.incrBy(aaKey(), quantity);
    }
    private void aaDecrBy(long quantity) {
        jedis.decrBy(aaKey(), quantity);
    }

    private String pricesKey() {
        return getUnderlier() + ":" + getBookType() + ":prices";
    }
    private void addToPrices(double price) {
        jedis.zadd(pricesKey(), price, String.valueOf(price));
    }

    private String ogpKey(double price) {
        return getUnderlier() + ":" + getBookType() + ":ogp:" + price;
    }
    private long ogpAdd(double price, long orderID) {
        return jedis.zadd(ogpKey(price), orderID, String.valueOf(orderID));
    }
    private void ogpRemove(double price, long orderID) {
        log.info("Removing OGP (1 of 3). The price value is: " + price);
        log.info("Removing OGP (2 of 3). The key  is: " + ogpKey(price));
        log.info("Removing OGP (3 of 3). The String value of orderID is: " + String.valueOf(orderID));
        jedis.zrem(ogpKey(price), String.valueOf(orderID));
    }

    private String cacheKey(Long orderID) {
        return getUnderlier() + ":" + getBookType() + ":cache:" + orderID;
    }
    private void cacheAdd(Order order) {
        jedis.hmset(cacheKey(order.getId()), orderViewToMap(order));
    }
    private void cacheRemove(long orderID) {
        jedis.del(cacheKey(orderID));
    }
    private void cacheUpdate(long orderID, long quantityToRemove) {
        // Since there is no "hdecrBy" redis command, we need to increment by a negative number
        jedis.hincrBy(cacheKey(orderID), "quantityRemaining", quantityToRemove * -1L);
    }

    private String coKey() {
        return getUnderlier() + ":" + getBookType() + ":" + "co";
    }
    private void coAdd(long orderID) {
        jedis.rpush(coKey(), String.valueOf(orderID));
    }

    private String sppKey(double price) {
        return getUnderlier() + ":" + getBookType() + ":" + price;
    }
    private void sppIncr(double price, long quantity) {
        jedis.incrBy(sppKey(price), quantity);
        addToPrices(price);
    }
    private void sppDecr(double price, long quantity) {
        jedis.decrBy(sppKey(price), quantity);
        if (jedis.get(sppKey(price)).equals("0")) {
            jedis.del(sppKey(price));
            jedis.zrem(pricesKey(), String.valueOf(price));
        }
    }

    private Map<String, String> orderViewToMap(Order order) {
        Map<String, String> map = new HashMap<>();
        map.put("id", order.getId().toString());
        map.put("userID", order.getUserID());
        map.put("symbol", order.getSymbol());
        map.put("quantity", order.getQuantity().toString());
        map.put("price", order.getPrice().toString());
        map.put("side", order.getSide());
        map.put("quantityRemaining", order.getQuantityRemaining().toString());
        return map;
    }
    private boolean doesOrderExistInCache(long orderID) {
        log.info("Trying to find orderID: " + orderID + " in the cache");
        return jedis.exists(cacheKey(orderID));
    }
    private boolean doesOrderExistInOGP(long orderID, double price) {
        return jedis.zrank(ogpKey(price), String.valueOf(orderID)) != null;
    }

    private Order convertHashToOrder(Map<String, String> values) {
        long id = java.lang.Long.parseLong(values.get("id"));
        long quantity = java.lang.Long.parseLong(values.get("quantity"));
        double price = java.lang.Double.parseDouble(values.get("price"));
        long quantityRemaining = java.lang.Long.parseLong(values.get("quantityRemaining"));

        return new Order(id, values.get("userID"), values.get("symbol"), quantity, price, values.get("side"), quantityRemaining);
    }

    private String getUnderlier() {
        return underlier;
    }
    private String getBookType() {
        return bookType;
    }

}
