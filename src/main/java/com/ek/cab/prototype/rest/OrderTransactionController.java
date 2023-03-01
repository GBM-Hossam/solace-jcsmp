package com.ek.cab.prototype.rest;


import com.ek.cab.prototype.broker.jcsmp.TransactionPublishEventHandler;
import com.ek.cab.prototype.model.Event;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.solacesystems.jcsmp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;

@RestController
@Configuration
@RequestMapping("/transaction")
public class OrderTransactionController {

    private static final Logger log = LogManager.getLogger(OrderTransactionController.class);

    @Autowired
    private JCSMPSession session;
    @Autowired
    private Environment environment;
    @Autowired
    private XMLMessageProducer producer;

    public OrderTransactionController() {
    }

    @Bean
    public JCSMPStreamingPublishCorrelatingEventHandler getPublishEventHandler() {
        return new TransactionPublishEventHandler();
    }

    @Bean(destroyMethod = "close")
    /*
     * XMLMessageProducer provides a session-dependent interface for applications to send messages to a Solace appliance.
     * An XMLMessageProducer instance is acquired from JCSMPSession. When acquired successfully, the instance of XMLMessageProducer
     * holds a channel (connection) opened to the appliance. In general, it is an expensive operation to acquire an XMLMessageProducer instance;
     * applications must cache this instance, and close it only when it is no longer required.
     */
    public XMLMessageProducer getXMLMessageProducer() throws JCSMPException {
        return session.getMessageProducer(getPublishEventHandler());
    }

    @PostMapping("/send")
    public void send(final @RequestBody Event event) throws JMSException, JCSMPException, JsonProcessingException {
        log.info("Sending a event.");

        final String topicName = environment.getProperty("solace.message.publisher.topic");

        log.debug("Attempting to provision the topic '%s' on the appliance.%n", topicName);
        ///log.debug("Recieved event.%n", event.toString());

        XMLMessageProducer prod = getXMLMessageProducer();

        Topic tutorialTopic = JCSMPFactory.onlyInstance().createTopic(topicName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(event);

        log.info("Publish object.[" + json + "]");

        final TextMessage msg = prod.createTextMessage();

        SDTMap map = prod.createMap();
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        map.putString("ek_ChannelName", event.getData().getEK_ChannelName());
        msg.setProperties(map);
        msg.setText(json);
        msg.setCorrelationKey(event);  // correlation key for receiving ACKs
        prod.send(msg, tutorialTopic);
    }
}
