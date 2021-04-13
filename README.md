# Interact With Titan
https://svelte-8jol273uz-ivorytoast.vercel.app/

# Architecture Diagram
![Titan_Exchange_Diagram](https://user-images.githubusercontent.com/8243054/113966258-c229bc80-97fc-11eb-953b-449b9d8d2615.png)

## Current State
* Loki is built (Own docker image)
* Thor is built (Own docker image)
* Bifrost is built (Own docker image)
* Redis images are connected
* MySQL images are connected
* Sample client is created (vercel repository -- link above "Interact with Titan")
* Bifrost runs ZeroMQ
* Backend is running on Vultr cloud hosting
* UI is running on Vercel cloud hosting
* Backend has a self-signed certificate to allow "https"
* Nginx Load Balancer in front of Loki and Thor

## TODO
* Creating the Redis replication instances in docker compose file
* Database needs to have replicas
* Wanda is coupled with Thor. Therefore, Loki and Thor have repeating models to interact with Wanda. Wanda needs to have its own image and provide a client for Loki, Thor and any future service wanting to interact with the MySQL database
* Heimdall created to provide a pub/sub service for all orders and related information

## Titan's FIX Protocol
   * 8=FIX
   * 1=USER_ID
   * 2=SYMBOL
   * 3=QUANTITY
   * 4=PRICE
   * 5=SIDE

## Docker
* mvn clean install -DskipTests
* docker build -t ivorytoast3853/thor .
* docker push ivorytoast3853/thor
* docker run ivorytoast3853/thor

* docker-compose -f docker-compose.yml up --build -d
* docker-compose down

## Useful Info
https://github.com/docker-library/mysql/issues/275

# Thor
Responsibility: Engine for Titan
Outward Connections: Redis, Database (Wanda)
Receiving Connections: JSON, ZeroMQ Socker

### Thor's Unique Matching Criteria
1. Called a "Full fill or wait"
  * This means that when an order is received, if the order can be fully matched, it will be fully matched. If it cannot be fully matched, NO matching is done and the order is simply added to the corresponding symbol's bids/asks

### Thor's Cache (Tesseract)
Handles the following responsibilites:
1. Add
2. Remove Matched Order
3. Remove Cancelled Order
4. Update
5. Find

Tesseract _solely_ deals with Redis. It does not involve itself with the database. Therefore, when the Tesseract is trying to find, update, add or delete orders, it is using Redis as its sole datasource. This makes sure Thor does not keep internal state, which allows multiple instances to be running and receiving connections at the same time. Therefore, ZeroMQ was used to send all requests from Loki to Thor and handle the request/response pattern.

### Workflow of the engine (Accepting a new order)
1. Order is accepted
2. The underlier of the order is used to create a Symbol object
  * Symbol objects contain the books (bids & asks)
3. Depending if the client is buying or selling, the correct book is used (buys -> bids, sells -> asks)
4. Gets the order quantity. If there are not enough shares to fill the order, the matching session ends and the order gets added to the correct book
5. If their are enough shares, the best prices are returned (for a buy order, the lowest asks are retrieved (or) for a sell order, the highest bids are retrieved)
6. Order goes through each price matching shares. The process ends only when the incoming order's quantity is zero
7. During the matching process, all affected orders (including the incoming order) is saved
8. These saved orders are then cycled through and the order is updated in the database as well
