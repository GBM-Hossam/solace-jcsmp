package com.ek.cab.prototype.broker;

import com.ek.cab.prototype.broker.jcsmp.TransactionMessageListener;
import com.solacesystems.jcsmp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.jms.ConnectionFactory;

@Configuration
@Slf4j
@PropertySource({"classpath:application.properties"})
public class SolaceConnection {

    /// private static final Logger log = LogManager.getLogger(TransactionMessageListener.class);
    @Autowired
    private TransactionMessageListener transactionListener;
    @Autowired
    private Environment environment;

    @Bean(destroyMethod = "closeSession")
    public JCSMPSession getSolaceSession() throws JCSMPException {
        log.debug("inside:getSolaceSession");
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, environment.getProperty("solace.java.host"));          // host:port
        properties.setProperty(JCSMPProperties.VPN_NAME, environment.getProperty("solace.java.msgVpn"));     // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, environment.getProperty("solace.java.clientUsername"));      // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, environment.getProperty("solace.java.clientPassword"));      // client-password
        properties.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);  // subscribe Direct subs after reconnect

        JCSMPChannelProperties channelProps = new JCSMPChannelProperties();
        channelProps.setReconnectRetries(Integer.parseInt(environment.getProperty("solace.java.reconnectRetries")));      // recommended settings?
        channelProps.setConnectRetriesPerHost(Integer.parseInt(environment.getProperty("solace.java.connectRetriesPerHost")));  // recommended settings?
        channelProps.setConnectRetries(Integer.parseInt(environment.getProperty("solace.java.connectRetries"))); // recommended settings?
        channelProps.setReconnectRetryWaitInMillis(Integer.parseInt(environment.getProperty("solace.java.reconnectRetryWaitInMillis")));// recommended settings?

        properties.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES, channelProps);
        final JCSMPSession session;
        session = JCSMPFactory.onlyInstance().createSession(properties, null, new SessionEventHandler() {
            @Override
            public void handleEvent(SessionEventArgs event) {  // could be reconnecting, connection lost, etc.
                log.info("### Received a Session event: %s%n", event);
            }
        });
        session.connect();  // connect to the broker
        return session;
    }

    @Bean(destroyMethod = "close")
    public FlowReceiver consumerConnection(final ConnectionFactory connectionFactory) throws JCSMPException {
        log.debug("inside:consumerConnection");

        JCSMPSession session = getSolaceSession();
        // Confirm the current session supports the capabilities required.
        if (session.isCapable(CapabilityType.PUB_GUARANTEED) &&
                session.isCapable(CapabilityType.SUB_FLOW_GUARANTEED) &&
                session.isCapable(CapabilityType.ENDPOINT_MANAGEMENT) &&
                session.isCapable(CapabilityType.QUEUE_SUBSCRIPTIONS)) {
            log.debug("All required capabilities supported!");
        } else {
            log.debug("Missing required capability.");
            log.debug("Capability - PUB_GUARANTEED: " + session.isCapable(CapabilityType.PUB_GUARANTEED));
            log.debug("Capability - SUB_FLOW_GUARANTEED: " + session.isCapable(CapabilityType.SUB_FLOW_GUARANTEED));
            log.debug("Capability - ENDPOINT_MANAGEMENT: " + session.isCapable(CapabilityType.ENDPOINT_MANAGEMENT));
            log.debug("Capability - QUEUE_SUBSCRIPTIONS: " + session.isCapable(CapabilityType.QUEUE_SUBSCRIPTIONS));
            System.exit(1);
        }


        final String queueName = environment.getProperty("solace.message.consumer.queue");
        log.debug("Attempting to provision the queue '%s' on the appliance.%n", queueName);
        final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // Actually provision it, and do not fail if it already exists
        session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        log.debug("Attempting to bind to the queue '%s' on the appliance.%n", queueName);
        // Create a Flow be able to bind to and consume messages from the Queue.
        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(queue);
        flow_prop.setAckMode(JCSMPProperties.MESSAGE_ACK_MODE);  //todo configure right ack mode

        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

        final FlowReceiver consumer = session.createFlow(transactionListener, flow_prop, endpoint_props);
        consumer.start();
        return consumer;
    }
}