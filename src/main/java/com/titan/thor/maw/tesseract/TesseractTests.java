package com.titan.thor.maw.tesseract;

import com.titan.thor.cache.Tesseract;
import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log
public class TesseractTests {

    // TODO: Fix these tests next...aa/co/cache should all be the same. Just ogp/spp should be different

    private final Jedis jedis = new Jedis("redis");
    private final Tesseract tesseract = new Tesseract(BookType.BID, "spx");

    int numberOfKeysCounter = 1;
    int amountAvailableCounter = 0;
    HashMap<Double, Long> sppCounter = new HashMap<>();

    public TesseractTests() {}

    private void reset() {
        jedis.flushDB();
        numberOfKeysCounter = 1;
        amountAvailableCounter = 0;
        sppCounter = new HashMap<>();
    }

    public String quantity_Test() {
        reset();

        tesseract.add(TesseractObjects.orderOne);
        tesseract.add(TesseractObjects.orderTwo);
        tesseract.add(TesseractObjects.orderThree);
        tesseract.removeMatchedOrder(3L);
        tesseract.add(TesseractObjects.orderFour);
        tesseract.add(TesseractObjects.orderFive);
        tesseract.removeMatchedOrder(4L);
        tesseract.removeMatchedOrder(1L);
        tesseract.find(2L);
        tesseract.removeMatchedOrder(2L);
        tesseract.add(TesseractObjects.orderSix);
        tesseract.removeMatchedOrder(6L);
        tesseract.find(5L);
        tesseract.removeMatchedOrder(5L);

        Set<String> keys = jedis.keys("*");
        if (keys.size() != 2) return "There should only be 2 left in the cache. There are: " + keys.size();

        String aa = jedis.get("spx:bids:aa");
        if (aa == null) return "Amount Available key should still exist";
        if (!aa.equals(Integer.toString(0))) return "Amount Available should be 0. It is: " + aa;

        long coLengthTwo = jedis.llen("spx:bids:co");
        if (coLengthTwo != 6L) return "Not all the orders were added to the completed list. The length of completed orders is: " + coLengthTwo;

        return "(Quantity) All good!";
    }

    public String test_Add() {
        reset();

        tesseract.add(TesseractObjects.orderOne);

        String oneOutput = testAddValues(TesseractObjects.orderOne, "spx:bids:aa", "spx:bids:cache:1", "spx:bids:spp:1.1", "spx:bids:ogp:1.1:1");
        if (!oneOutput.equals("")) return oneOutput;

        tesseract.add(TesseractObjects.orderTwo);

        String twoOutput = testAddValues(TesseractObjects.orderTwo, "spx:bids:aa", "spx:bids:cache:2", "spx:bids:spp:2.2", "spx:bids:ogp:2.2:2");
        if (!twoOutput.equals("")) return twoOutput;

        tesseract.removeMatchedOrder(1);

        Set<String> keys = jedis.keys("*");
        if (keys.size() != 5) return "There should only be 5 keys in the cache. There are: " + keys.size();

        String aa = jedis.get("spx:bids:aa");
        if (aa == null) return "Amount Available key does not exist on REMOVE";
        amountAvailableCounter -= 1;
        if (!aa.equals(Integer.toString(amountAvailableCounter))) return "Amount Available does not equal " + amountAvailableCounter + " on ADD";

        Map<String, String> cache = jedis.hgetAll("spx:bids:cache:1");
        if (cache.size() > 0) return "The order from the cache was not removed. The key is: spx:bids:cache:1";

        String spp = jedis.get("spx:bids:spp:1.1");
        if (spp != null) {
            if (!spp.equals("0")) {
                return "spp was not removed. The key is: spx:bids:spp:1.1";
            }
        }

        Map<String, String> ogp = jedis.hgetAll("spx:bids:ogp:1.1:1");
        if (ogp.size() > 0) return "The order from the ogp was not removed. The key is: spx:bids:ogp:1.1:1";

        long coLength = jedis.llen("spx:bids:co");
        if (coLength != 1L) return "Order was not added to completed orders list. The length of completed orders is: " + coLength;

        tesseract.removeMatchedOrder(2);

        Set<String> keysTwo = jedis.keys("*");
        if (keysTwo.size() != 2) return "There should only be 2 keys in the cache. There are: " + keysTwo.size();

        String aaTwo = jedis.get("spx:bids:aa");
        if (aaTwo == null) return "Amount Available key does not exist on REMOVE";
        amountAvailableCounter -= 2;
        if (!aaTwo.equals(Integer.toString(amountAvailableCounter))) return "Amount Available does not equal " + amountAvailableCounter + " on REMOVE";

        Map<String, String> cacheTwo = jedis.hgetAll("spx:bids:cache:2");
        if (cacheTwo.size() > 0) return "The order from the cache was not removed. The key is: spx:bids:cache:2";

        String sppTwo = jedis.get("spx:bids:spp:2.2");
        if (sppTwo != null) {
            if (!sppTwo.equals("0")) {
                return "spp was not removed. The key is: spx:bids:spp:2.2";
            }
        }

        Map<String, String> ogpTwo = jedis.hgetAll("spx:bids:ogp:2.2:2");
        if (ogpTwo.size() > 0) return "The order from the ogp was not removed. The key is: spx:bids:ogp:2.2:2";

        long coLengthTwo = jedis.llen("spx:bids:co");
        if (coLengthTwo != 2L) return "Order was not added to completed orders list. The length of completed orders is: " + coLengthTwo;

        return "(Add) All Good!";
    }

    private String testAddValues(Order order, String aaKey, String cacheKey, String sppKey, String ogpKey) {
        numberOfKeysCounter += 3;
        amountAvailableCounter += order.getQuantityRemaining();

        if (sppCounter.containsKey(order.getPrice())) {
            sppCounter.put(order.getPrice(), sppCounter.get(order.getPrice()) + order.getQuantityRemaining());
        } else {
            sppCounter.put(order.getPrice(), order.getQuantityRemaining());
        }

        Set<String> keys = jedis.keys("*");
        if (keys.size() != numberOfKeysCounter) return "There should only be " + numberOfKeysCounter + " keys in the cache. There are: " + keys.size();

        String aa = jedis.get(aaKey);
        if (aa == null) return "Amount Available key does not exist on ADD";
        if (!aa.equals(Integer.toString(amountAvailableCounter))) return "Amount Available does not equal " + amountAvailableCounter + " on ADD";

        Map<String, String> cache = jedis.hgetAll(cacheKey);
        if (cache == null || cache.size() == 0) return "Cache key does not exist on ADD";
        String cacheTestOutput = testHashValues(cacheKey, order);
        if (!cacheTestOutput.equals("")) return cacheTestOutput;

        String spp = jedis.get(sppKey);
        if (spp == null) return "spp does not exist on ADD";
        if (!spp.equals(sppCounter.get(order.getPrice()).toString())) return "spp does not equal " + sppCounter.get(order.getPrice()) + " on ADD";

        Map<String, String> ogp = jedis.hgetAll(ogpKey);
        if (ogp == null) return "Cache key does not exist on ADD";
        String ogpTestOutput = testHashValues(ogpKey, order);
        if (!ogpTestOutput.equals("")) return ogpTestOutput;

        return "";
    }

    private String testHashValues(String key, Order order) {
        if (jedis.hget(key, "quantity") == null) return "Quantity does not exist on " + key;
        if (jedis.hget(key, "userID") == null) return "userID does not exist on " + key;
        if (jedis.hget(key, "symbol") == null) return "symbol does not exist on " + key;
        if (jedis.hget(key, "quantityRemaining") == null) return "quantityRemaining does not exist on " + key;
        if (jedis.hget(key, "side") == null) return "side does not exist on " + key;
        if (jedis.hget(key, "price") == null) return "price does not exist on " + key;

        if (!jedis.hget(key, "quantity").equals(order.getQuantity().toString())) return "Quantity does not match on " + key + ". It is: " + jedis.hget(key, "quantity");
        if (!jedis.hget(key, "userID").equals(order.getUserID())) return "userID does not match on " + key + ". It is: " + jedis.hget(key, "userID");
        if (!jedis.hget(key, "symbol").equals(order.getSymbol())) return "symbol does not match on " + key + ". It is: " + jedis.hget(key, "symbol");
        if (!jedis.hget(key, "quantityRemaining").equals(order.getQuantityRemaining().toString())) return "quantityRemaining does not match on " + key + ". It is: " + jedis.hget(key, "quantityRemaining");
        if (!jedis.hget(key, "side").equals(order.getSide())) return "side does not match on " + key + ". It is: " + jedis.hget(key, "side");
        if (!jedis.hget(key, "price").equals(order.getPrice().toString())) return "price does not match on " + key + ". It is: " + jedis.hget(key, "price");

        return "";
    }

}
