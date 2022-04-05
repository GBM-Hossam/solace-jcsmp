package com.ek.cab.prototype.rest;


import com.ek.cab.prototype.broker.jcsmp.TransactionPublishEventHandler;
import com.ek.cab.prototype.model.Transaction;
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
    public void send(final @RequestBody Transaction transaction) throws JMSException, JCSMPException, JsonProcessingException {
        log.info("Sending a transaction.");

        final String topicName = environment.getProperty("solace.message.publisher.topic");

        log.debug("Attempting to provision the topic '%s' on the appliance.%n", topicName);
        XMLMessageProducer prod = getXMLMessageProducer();

        Topic tutorialTopic = JCSMPFactory.onlyInstance().createTopic(topicName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

        String json = ow.writeValueAsString(transaction);

        log.info("Publish object.[" + json + "]");

        final TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        msg.setText(json);
        msg.setCorrelationKey(msg);
        /*
        When publishing direct messages, XMLMessageProducer operates in non-blocking mode, also known as streaming publish mode.
        In other words, the send() does not block while waiting for the appliance to acknowledge delivery.
        Applications receive notifications regarding error conditions of delivery through the callback handler JCSMPStreamingPublishEventHandler.
        In this mode, the API returns control from the send operation as soon as the message has been written to the network socket's buffer.
        When publishing persistent or non-persistent messages, XMLMessageProducer will block when the publisher window, or the network socket's buffer is full
         */
        prod.send(msg, tutorialTopic);
    }
}
