Getting Started

### Reference Documentation

Protoype based on Soalce Java JCSMP APIs on Spring boot App. For further reference, please consider the following sections:

* [Soalce on Windows Docker](https://docs.solace.com/Solace-SW-Broker-Set-Up/Docker-Containers/Set-Up-Docker-Container-Windows.htm)
* [Soalce Java JCSMP APIs](https://docs.solace.com/Solace-PubSub-Messaging-APIs/JCSMP-API/jcsmp-api-home.htm)
* [Non-Exclusive Queue – Persistent QoS Round-Robin](https://solace.com/blog/consumer-groups-consumer-scaling-solace/)
* [Topic Subscription on Queues](https://solace.com/blog/topic-subscription-queues/)
* All required administrative objects must be created on Solace before try out this prototype

![image](https://user-images.githubusercontent.com/25661435/161483756-44710de6-f919-4dde-a83a-e6893ea29aa4.png)


### Steps

* Prototype contains both the publisher and consumer 
* Invoke API `http://localhost:8090/transaction/send with payload {
  "from":"XX",
  "to":"YY",
  "id":1
  } `or use CURL command <br>

`curl http://localhost:8090/transaction/send -X POST -H "Content-type:application/json" -d '{"from":"XX","to":"YY","id": 1}'
`<br>or<br>
`curl http://localhost:8090/transaction/send -X POST -H "Content-type:application/json" -d '{"\"from"\":"\"XX"\","\"to"\":"\"YY"\","\"id"\":1}'" 
`
<br>API will trigger a message to be published on a topic(T/OrderTransaction), on other side consumer will consume the message from a queue (OrderTransactionQueue)
and covert into Java Object ready for persistence !!
* All Solace broker connectivity and fine-[tuning properties](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/constant-values.html) are in application.properties
