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

    public OrderTransactionController() {
    }

    @Bean
    public JCSMPStreamingPublishCorrelatingEventHandler getPublishEventHandler() {
        return new TransactionPublishEventHandler();
    }

    @PostMapping("/send")
    public void send(final @RequestBody Transaction transaction) throws JMSException, JCSMPException, JsonProcessingException {
        log.info("Sending a transaction.");

        final String topicName = environment.getProperty("solace.message.publisher.topic");

        log.debug("Attempting to provision the topic '%s' on the appliance.%n", topicName);
        XMLMessageProducer prod = session.getMessageProducer(getPublishEventHandler());

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
        // Delivery not yet confirmed. See ConfirmedPublish.java
        prod.send(msg, tutorialTopic);
    }
}
