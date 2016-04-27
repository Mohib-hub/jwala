package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmStateMapMessageConverter converter;
    private final JvmService jvmService;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;
    private final JvmStateService jvmStateService;

    public JvmStateMessageListener(final JvmStateMapMessageConverter converter,
                                   final JvmService jvmService,
                                   final MessagingService messagingTemplate,
                                   final GroupStateNotificationService groupStateNotificationService,
                                   final JvmStateService jvmStateService) {
        this.converter = converter;
        this.jvmService = jvmService;
        this.messagingService = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
        this.jvmStateService = jvmStateService;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            LOGGER.debug("Received message : {}", message.getJMSMessageID());
            handleMessage(message);
        } catch (final JMSException | RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void handleMessage(final Message aMessage) throws JMSException {
        if (aMessage instanceof MapMessage) {
            processMessage((MapMessage) aMessage);
        } else {
            LOGGER.warn("Unable to process message {} of type {} ", aMessage.getJMSMessageID(), aMessage.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    protected void processMessage(final MapMessage aMapMessage) throws JMSException {
        final JvmStateMessage message = converter.convert(aMapMessage);
        LOGGER.debug("Processing message: {}", message);

        // We don't report the "JVM" stopped state since it does not mean that the service has also stopped running.
        // JvmControlServiceImpl is the one that knows if the service has stopped and thus has the responsibility of
        // notifying the client.
        if (!JvmState.JVM_STOPPED.toString().equalsIgnoreCase(message.getState())) {
            final SetStateRequest<Jvm, JvmState> setStateCommand = message.toCommand();
            final CurrentState<Jvm, JvmState> newState = setStateCommand.getNewState();
            jvmStateService.updateState(newState.getId(), newState.getState(), newState.getMessage());
        }
    }
}
