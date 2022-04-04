package com.ek.cab.prototype.broker.jcsmp;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionPublishEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {

    private static final Logger log = LogManager.getLogger(TransactionPublishEventHandler.class);


    @Override
    public void responseReceivedEx(Object key) {
        log.debug("Producer received response for msg: " + key.toString());
    }

    @Override
    public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
        log.debug("Producer received error for msg: %s@%s - %s%n", key.toString(), timestamp, cause);
    }
}
