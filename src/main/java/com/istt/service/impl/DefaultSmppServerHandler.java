package com.istt.service.impl;

import org.apache.log4j.Logger;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;

import com.cloudhopper.smpp.type.SmppProcessingException;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class DefaultSmppServerHandler implements SmppServerHandler {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ClientSmppSessionHandler.class);
	private SmppTestingServiceImpl smppTestingServiceImpl;

	public DefaultSmppServerHandler() {
        this.smppTestingServiceImpl = SmppTestingServiceImpl.getInstance();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, final BaseBind bindRequest) throws SmppProcessingException {
        if (this.smppTestingServiceImpl.getSmppSession() != null) {
            throw new SmppProcessingException(SmppConstants.STATUS_INVBNDSTS);
        }

//        sessionConfiguration.setAddressRange(bindRequestAddressRange);
//
//        sessionConfiguration.setCountersEnabled(esme.isCountersEnabled());

        sessionConfiguration.setName("Test SMPP session");
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
        if (this.smppTestingServiceImpl.getSmppSession() != null) {
            throw new SmppProcessingException(SmppConstants.STATUS_INVBNDSTS);
        }

        this.smppTestingServiceImpl.addMessage("Session created", session.getConfiguration().getSystemId());
        this.smppTestingServiceImpl.setSmppSession(session);
        session.serverReady(new ClientSmppSessionHandler());
    }

    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        this.smppTestingServiceImpl.addMessage("Session destroyed", session.getConfiguration().getSystemId());
        this.smppTestingServiceImpl.setSmppSession(null);

        session.destroy();
    }

}
