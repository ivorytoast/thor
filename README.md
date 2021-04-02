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