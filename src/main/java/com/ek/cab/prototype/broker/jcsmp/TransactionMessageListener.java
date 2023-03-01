package com.ek.cab.prototype.broker.jcsmp;

import com.ek.cab.prototype.model.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TransactionMessageListener implements XMLMessageListener {

    private static final Logger log = LogManager.getLogger(TransactionMessageListener.class);

    @Override
    public void onReceive(BytesXMLMessage msg) {
        if (msg instanceof TextMessage) {
            String jsonObject = ((TextMessage) msg).getText();
            ObjectMapper mapper = new ObjectMapper();
            try {
                Event event = mapper.readValue(jsonObject, Event.class);
                log.info("event received:[" + event.toString() + "]");
                //ACK at the end after successfully processing required logic, in case of failure don't ACK !!!
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }

        } else {
            SDTMap map = msg.getProperties();
            log.info("Message received." + map.toString());
        }
        msg.ackMessage();
        /// log.debug("Message Dump:" + msg.dump());
        /// log.debug("TopicSequenceNumber:" + msg.getTopicSequenceNumber());
    }

    @Override
    public void onException(JCSMPException e) {
        log.error("### MessageListener's onException(): %s%n", e);

    }
}
