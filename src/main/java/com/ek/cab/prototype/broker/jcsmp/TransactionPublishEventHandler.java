package com.ek.cab.prototype.broker.jcsmp;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionPublishEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {

    /// private static final Logger log = LogManager.getLogger(TransactionPublishEventHandler.class);


    @Override
    public void responseReceivedEx(Object key) {
        ///todo ack or nack !!
        log.debug("Producer received response for msg: " + key.toString());
    }

    @Override
    public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
        log.debug("Producer received error for msg: %s@%s - %s%n", key.toString(), timestamp, cause);
    }
}
