package com.ek.cab.prototype.broker.jcsmp;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionPublishEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {

    /*
    https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/JCSMPStreamingPublishCorrelatingEventHandler.html#handleError-java.lang.String-com.solacesystems.jcsmp.JCSMPException-long-
     */

    @Override
    public void responseReceivedEx(Object key) {
        log.info("Producer received response for msg: " + key.toString() + " msg has been ACKed");
    }

    @Override
    public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
        log.error("Producer received error for msg has been NACKed: %s@%s - %s%n", key.toString(), timestamp, cause);
    }
}
