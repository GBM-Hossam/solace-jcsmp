Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Soalce on Windows Docker](https://docs.solace.com/Solace-SW-Broker-Set-Up/Docker-Containers/Set-Up-Docker-Container-Windows.html)
* [Soalce Java JCSMP APIs](https://docs.solace.com/Solace-PubSub-Messaging-APIs/JCSMP-API/jcsmp-api-home.htm)
* [Non-Exclusive Queue – Persistent QoS Round-Robin](https://solace.com/blog/consumer-groups-consumer-scaling-solace/)

![image](https://user-images.githubusercontent.com/25661435/161483756-44710de6-f919-4dde-a83a-e6893ea29aa4.png)


### Steps

* Prototype contains both the publisher and consumer 
* Invoke API http://localhost:8090/transaction/send with payload {
  "from":"XX",
  "to":"YY",
  "id":1
  } or use CURL command "" 
API will trigger a message to be published on TOPIC, on other side consumer will consume the message from Queue 
and covert into Java Object ready for persistence !!
* All Solace broker connectivity and fine-tuning properties are in application.properties
