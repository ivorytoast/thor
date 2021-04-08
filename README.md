# Interact With Titan
https://svelte-8jol273uz-ivorytoast.vercel.app/

# Architecture Diagram
![Titan_Exchange_Diagram](https://user-images.githubusercontent.com/8243054/113965695-dcaf6600-97fb-11eb-9b2e-8365930a4d97.png)

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

## FIX Protocol
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
