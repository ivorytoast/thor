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

## TODO
* Nginix Load Balancer in front of Loki
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
