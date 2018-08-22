package com.istt.service.impl;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.smstpdu.DataCodingScheme;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharset;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetEncoder;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetEncodingData;
import org.mobicents.protocols.ss7.map.datacoding.Gsm7EncodingStyle;
import org.mobicents.protocols.ss7.map.smstpdu.DataCodingSchemeImpl;
import org.mobicents.smsc.library.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseSm;
import com.cloudhopper.smpp.pdu.DataSm;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitMulti;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.istt.service.SmppParametersService;
import com.istt.service.SmppTestingService;
import com.istt.service.dto.SmppMessage;
import com.istt.service.dto.SmppSimulatorParameters;
import com.istt.service.dto.SmppSimulatorParameters.EncodingType;
import com.istt.service.dto.SmppSimulatorParameters.SendingMessageType;
import com.istt.service.dto.SmppSimulatorParameters.SplittingType;
import com.istt.service.dto.SmppSimulatorParameters.ValidityType;
import com.istt.service.util.CodeStatusUtil;
import com.istt.service.util.StringUtils;
import com.istt.smpp.SmppPcapParser;
import com.istt.smpp.TestSmppClient;
import com.istt.smpp.TestSmppSession;
import com.istt.web.websocket.dto.ResReturnDTO;

@EnableScheduling
@Service("SmppTestingService")
public class SmppTestingServiceImpl implements SmppTestingService {

	private static Logger logger = Logger.getLogger(SmppTestingServiceImpl.class);

	private static SmppTestingServiceImpl smppTestingService;

	protected Timer[] timer;
	protected AtomicInteger messagesSent = new AtomicInteger();
	protected AtomicInteger segmentsSent = new AtomicInteger();
	protected AtomicInteger responsesRcvd = new AtomicInteger();
	protected AtomicInteger messagesRcvd = new AtomicInteger();

	private static Charset utf8Charset = Charset.forName("UTF-8");
	private static Charset ucs2Charset = Charset.forName("UTF-16BE");
	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Charset gsm7Charset = new GSMCharset("GSM", new String[] {});

	private AtomicLong msgIdGenerator;

	private ThreadPoolExecutor executor;
	private ScheduledThreadPoolExecutor monitorExecutor;
	private TestSmppClient clientBootstrap;
	private SmppSession session0;
	private DefaultSmppServer defaultSmppServer;


	@Autowired
	SimpMessageSendingOperations messagingTemplate;
	
	@Autowired
	private SmppParametersService smppParametersService;

	@PostConstruct
	private void load() {
		smppTestingService = this;
		Random rn = new Random();
		msgIdGenerator = new AtomicLong(rn.nextInt(100000000));
	}

	@Value("${server.port}")
	private String serverPort;

	public static SmppTestingServiceImpl getInstance() {
		return smppTestingService;
	}

	public void addMessage(String msg, String info) {
		addMessage(CodeStatusUtil.ADD_MESSAGE, msg, info);
	}

	public void addMessage(Integer codeStatus, String msg, String info) {
		try {
			SmppMessage sms = setSmppMessage(msg, info);
			ResReturnDTO returnDTO = new ResReturnDTO();
			returnDTO.setCodeStatus(codeStatus);
			returnDTO.setData(sms);
			messagingTemplate.convertAndSend("/topic/smpp-logger", returnDTO);
		} catch (Exception e) {
			logger.error("addMessage", e);
		}

	}

	private SmppMessage setSmppMessage(String msg, String info) throws ParseException {
		SmppMessage sms = new SmppMessage();
		String sDate = StringUtils.convertDateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		sms.setInfo(info);
		sms.setMsg(msg);
		sms.setTimeStamp(sDate);
		logger.debug("Add sms:\t" + sms);
		return sms;
	}

	private String getMessState(){
		return "messageSegmentsSent=" + this.segmentsSent.get() + ", submitMessagesSent="
				+ this.messagesSent.get() + ", submitResponsesRcvd=" + this.responsesRcvd.get() + ", messagesRcvd="
				+ this.messagesRcvd.get();
	}

	@Scheduled(initialDelay = 10000, fixedRate = 5000)
	public void scheduledAddMessage() {
		try {
//			addMessage(CodeStatusUtil.ADD_MESSAGE, "Message Segment", getMessState());
			addMessage(CodeStatusUtil.REFRESH_STATE, "Message Segment", getMessState());
		} catch (Exception e) {
			logger.error("addMessage", e);
		}

	}

	@Override
	public ResReturnDTO refreshState() {
		try{
			SmppMessage sms = setSmppMessage("Message Segment", getMessState());
			ResReturnDTO returnDTO = new ResReturnDTO();
			returnDTO.setCodeStatus(CodeStatusUtil.REFRESH_STATE);
			returnDTO.setData(sms);
			return returnDTO;
		}catch (Exception e) {
			logger.error("addMessage", e);
			return null;
		}

	}

	@Override
	public ResReturnDTO sendBadPacket() {
		doSendBadPacket();
		return null;
	}

	private void doSendBadPacket() {
		// TODO: ..............................
		SubmitSm submitSm = new SubmitSm();
		try {
			((TestSmppSession) this.session0).setMalformedPacket();
			this.session0.submit(submitSm, 1000);
		} catch (Exception e) {
			this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Have Exception. Can't send bad packet", e.getMessage());
			logger.error("submitSm", e);
		}
	}

	@Override
	public ResReturnDTO startASession() {
		start();
		return null;
	}

	public void start() {
		this.messagesSent = new AtomicInteger();
		this.segmentsSent = new AtomicInteger();
		this.responsesRcvd = new AtomicInteger();
		this.messagesRcvd = new AtomicInteger();

		this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Trying to start a new "
				+ smppParametersService.getCofGeneralParameters().getSmppSessionType() + " session", "");

		this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		this.monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
				return t;
			}
		});

		if (smppParametersService.getCofGeneralParameters().getSmppSessionType() == SmppSession.Type.CLIENT) {
			clientBootstrap = new TestSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);

			DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();

			SmppSessionConfiguration config0 = new SmppSessionConfiguration();
			config0.setWindowSize(smppParametersService.getCofGeneralParameters().getWindowSize());
			config0.setName("Tester.Session.0");
			config0.setType(smppParametersService.getCofGeneralParameters().getBindType());
			config0.setHost(smppParametersService.getCofGeneralParameters().getHost());
			config0.setPort(smppParametersService.getCofGeneralParameters().getPort());
			config0.setConnectTimeout(smppParametersService.getCofGeneralParameters().getConnectTimeout());
			config0.setSystemId(smppParametersService.getCofGeneralParameters().getSystemId());
			config0.setPassword(smppParametersService.getCofGeneralParameters().getPassword());
			config0.setAddressRange(
					new Address((byte) 1, (byte) 1, smppParametersService.getCofGeneralParameters().getAddressRange()));
			config0.getLoggingOptions().setLogBytes(true);
			// to enable monitoring (request expiration)
			config0.setRequestExpiryTimeout(smppParametersService.getCofGeneralParameters().getRequestExpiryTimeout());
			config0.setWindowMonitorInterval(
					smppParametersService.getCofGeneralParameters().getWindowMonitorInterval());
			config0.setCountersEnabled(true);

			try {
				session0 = clientBootstrap.bind(config0, sessionHandler);
			} catch (Exception e) {
				this.addMessage(CodeStatusUtil.SESSION_START_FALSE, "Failure to start a new session", e.toString());
				return;
			}

			// enableStart(false);
			// setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			this.addMessage(CodeStatusUtil.SESSION_START_SUCCESS, "Session has been successfully started", "");
		} else {

			SmppServerConfiguration configuration = new SmppServerConfiguration();
			configuration.setName("Test SMPP server");
			configuration.setPort(smppParametersService.getCofGeneralParameters().getPort());
			configuration.setBindTimeout(5000);
			configuration.setSystemId(smppParametersService.getCofGeneralParameters().getSystemId());
			configuration.setAutoNegotiateInterfaceVersion(true);
			configuration.setInterfaceVersion(SmppConstants.VERSION_3_4);
			configuration.setMaxConnectionSize(SmppConstants.DEFAULT_SERVER_MAX_CONNECTION_SIZE);
			configuration.setNonBlockingSocketsEnabled(true);

			configuration.setDefaultRequestExpiryTimeout(SmppConstants.DEFAULT_REQUEST_EXPIRY_TIMEOUT);
			configuration.setDefaultWindowMonitorInterval(SmppConstants.DEFAULT_WINDOW_MONITOR_INTERVAL);

			configuration.setDefaultWindowSize(SmppConstants.DEFAULT_WINDOW_SIZE);

			configuration.setDefaultWindowWaitTimeout(SmppConstants.DEFAULT_WINDOW_WAIT_TIMEOUT);
			configuration.setDefaultSessionCountersEnabled(true);

			configuration.setJmxEnabled(false);

			this.defaultSmppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler(), executor,
					monitorExecutor);
			try {
				this.defaultSmppServer.start();
			} catch (SmppChannelException e1) {
				this.addMessage(CodeStatusUtil.SESSION_START_FALSE, "Failure to start a defaultSmppServer",
						e1.toString());
				return;
			}

			// enableStart(false);
			// setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			this.addMessage(CodeStatusUtil.SESSION_START_SUCCESS, "SMPP Server has been successfully started", "");

		}
	}

	@Override
	public ResReturnDTO stopASession() {
		doStop();
		return null;
	}

	public void doStop() {
		try {
			if (this.session0 != null) {
				this.session0.unbind(5000);
				this.session0.destroy();
				this.session0 = null;
			}
			if (this.defaultSmppServer != null) {
				this.defaultSmppServer.stop();
				this.defaultSmppServer.destroy();
				this.defaultSmppServer = null;
			}

			if (clientBootstrap != null) {
				try {
					clientBootstrap.destroy();
					executor.shutdownNow();
					monitorExecutor.shutdownNow();
				} catch (Exception e) {

				}

				clientBootstrap = null;
				executor = null;
				monitorExecutor = null;
			}

			// enableStart(true);
			// setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			this.addMessage(CodeStatusUtil.SESSION_STOP, "Session has been stopped", "");
		} catch (Exception e) {
			this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Have Exception. Can't doStop", e.getMessage());
			logger.error("doStop", e);
		}

	}

	@Override
	public ResReturnDTO submitMessage() {
		doSubmitMessage(smppParametersService.getCofGeneralParameters().getEncodingType(),
				smppParametersService.getCofGeneralParameters().getMessageClass(),
				smppParametersService.getCofGeneralParameters().getMessageText(),
				smppParametersService.getCofGeneralParameters().getSplittingType(),
				smppParametersService.getCofGeneralParameters().getValidityType(),
				smppParametersService.getCofGeneralParameters().getDestAddress(),
				smppParametersService.getCofGeneralParameters().getMessagingMode(),
				smppParametersService.getCofGeneralParameters().getSpecifiedSegmentLength());
		return null;
	}

	private void doSubmitMessage(EncodingType encodingType, int messageClass, String messageText,
			SplittingType splittingType, ValidityType validityType, String destAddr,
			SmppSimulatorParameters.MessagingMode messagingMode, int specifiedSegmentLength) {
		if (session0 == null)
			return;

		try {
			int dcs = 0;
			ArrayList<byte[]> msgLst = new ArrayList<byte[]>();
			int msgRef = 0;

			switch (encodingType) {
			case GSM7_DCS_0:
				dcs = 0;
				break;
			case GSM8_DCS_4:
				dcs = 4;
				break;
			case UCS2_DCS_8:
				dcs = 8;
				break;
			}
			// if (messageClass) {
			// dcs += 16;
			// }
			int messageClassVal = 0;
			if (messageClass > 0) {
				messageClassVal = messageClass;
			}

			DataCodingScheme dataCodingScheme = new DataCodingSchemeImpl(dcs);
			int maxLen = MessageUtil.getMaxSolidMessageCharsLength(dataCodingScheme);
			int maxSplLen = MessageUtil.getMaxSegmentedMessageCharsLength(dataCodingScheme);
			if (splittingType == SplittingType.SplitWithParameters_SpecifiedSegmentLength
					|| splittingType == SplittingType.SplitWithUdh_SpecifiedSegmentLength) {
				maxLen = specifiedSegmentLength;
				maxSplLen = specifiedSegmentLength;
			}

			int segmCnt = 0;
			int esmClass = 0;
			boolean addSegmTlv = false;
			if (messageText.length() > maxLen) { // may be message splitting
				SplittingType st = splittingType;
				switch (st) {
				case DoNotSplit:
					// we do not split
					byte[] buf1 = encodeSegment(messageText, encodingType);
					byte[] buf2;
					if (encodingType == EncodingType.GSM8_DCS_4) {
						// 4-bytes length
						byte[] bf3 = new byte[7];
						bf3[0] = 6; // total UDH length
						bf3[1] = 5; // UDH id
						bf3[2] = 4; // UDH length
						bf3[3] = 0x3E;
						bf3[4] = (byte) 0x94;
						bf3[5] = 0;
						bf3[6] = 0;

						// 0-bytes length
						// bf3 = new byte[3];
						// bf3[0] = 2; // total UDH length
						// bf3[1] = 112; // UDH id
						// bf3[2] = 0; // UDH length

						buf2 = new byte[bf3.length + buf1.length];
						System.arraycopy(bf3, 0, buf2, 0, bf3.length);
						System.arraycopy(buf1, 0, buf2, bf3.length, buf1.length);
						esmClass = 0x40;
					} else {
						buf2 = buf1;
					}
					msgLst.add(buf2);
					ArrayList<String> r1 = this.splitStr(messageText, maxSplLen);
					segmCnt = r1.size();
					break;
				case SplitWithParameters_DefaultSegmentLength:
				case SplitWithParameters_SpecifiedSegmentLength:
					msgRef = getNextMsgRef();
					r1 = this.splitStr(messageText, maxSplLen);
					for (String bf : r1) {
						msgLst.add(encodeSegment(bf, encodingType));
					}
					segmCnt = msgLst.size();
					addSegmTlv = true;
					break;
				case SplitWithUdh_DefaultSegmentLength:
				case SplitWithUdh_SpecifiedSegmentLength:
					msgRef = getNextMsgRef();
					r1 = this.splitStr(messageText, maxSplLen);
					byte[] bf1 = new byte[6];
					bf1[0] = 5; // total UDH length
					bf1[1] = 0; // UDH id
					bf1[2] = 3; // UDH length
					bf1[3] = (byte) msgRef; // refNum
					bf1[4] = (byte) r1.size(); // segmCnt
					int i1 = 0;
					for (String bfStr : r1) {
						byte[] bf = encodeSegment(bfStr, encodingType);
						i1++;
						bf1[5] = (byte) i1; // segmNum
						byte[] bf2 = new byte[bf1.length + bf.length];
						System.arraycopy(bf1, 0, bf2, 0, bf1.length);
						System.arraycopy(bf, 0, bf2, bf1.length, bf.length);
						msgLst.add(bf2);
					}
					segmCnt = msgLst.size();
					esmClass = 0x40;
					break;
				}
			} else {
				byte[] buf = encodeSegment(messageText, encodingType);
				if (encodingType == EncodingType.GSM8_DCS_4) {
					byte[] bf1 = new byte[7];
					bf1[0] = 6; // total UDH length
					bf1[1] = 5; // UDH id
					bf1[2] = 4; // UDH length
					bf1[3] = 0x3e;
					bf1[4] = (byte) 0x94;
					bf1[5] = 0;
					bf1[6] = 0;

					// 0-bytes length
					// bf1 = new byte[3];
					// bf1[0] = 2; // total UDH length
					// bf1[1] = 112; // UDH id
					// bf1[2] = 0; // UDH length

					byte[] bf2 = new byte[bf1.length + buf.length];
					System.arraycopy(bf1, 0, bf2, 0, bf1.length);
					System.arraycopy(buf, 0, bf2, bf1.length, buf.length);
					msgLst.add(bf2);
					esmClass = 0x40;
				} else {
					msgLst.add(buf);
				}
				segmCnt = 1;
			}
			esmClass |= messagingMode.getCode();

			this.doSubmitMessage(dcs, msgLst, msgRef, addSegmTlv, esmClass, validityType, segmCnt, destAddr,
					messageClassVal);
		} catch (Exception e) {
			this.addMessage(CodeStatusUtil.MESSAGE_SUBMIT_FALSE, "Failure to submit message", e.toString());
			logger.error("SmppTestingServiceImpl", e);
			return;
		}
	}

	private int msgRef = 0;

	private int getNextMsgRef() {
		msgRef++;
		if (msgRef > 255)
			msgRef = 1;
		return msgRef;
	}

	private ArrayList<String> splitStr(String buf, int maxLen) {
		ArrayList<String> res = new ArrayList<String>();

		String prevBuf = buf;

		while (true) {
			if (prevBuf.length() <= maxLen) {
				res.add(prevBuf);
				break;
			}

			String segm = prevBuf.substring(0, maxLen);
			String newBuf = prevBuf.substring(maxLen, prevBuf.length());

			// String segm = new byte[maxLen];
			// String newBuf = new byte[prevBuf.length - maxLen];
			//
			// System.arraycopy(prevBuf, 0, segm, 0, maxLen);
			// System.arraycopy(prevBuf, maxLen, newBuf, 0, prevBuf.length -
			// maxLen);

			res.add(segm);
			prevBuf = newBuf;
		}

		return res;
	}

	@SuppressWarnings({ "rawtypes", "incomplete-switch" })
	private void doSubmitMessage(int dcs, ArrayList<byte[]> msgLst, int msgRef, boolean addSegmTlv, int esmClass,
			SmppSimulatorParameters.ValidityType validityType, int segmentCnt, String destAddr, int messageClassVal)
			throws Exception {
		int i1 = 0;
		for (byte[] buf : msgLst) {
			i1++;

			BaseSm pdu;
			switch (smppParametersService.getCofGeneralParameters().getSendingMessageType()) {
			case SubmitSm:
				SubmitSm submitPdu = new SubmitSm();
				pdu = submitPdu;
				break;
			case DataSm:
				DataSm dataPdu = new DataSm();
				pdu = dataPdu;
				break;
			case DeliverSm:
				DeliverSm deliverPdu = new DeliverSm();
				pdu = deliverPdu;
				break;
			case SubmitMulti:
				SubmitMulti submitMulti = new SubmitMulti();
				pdu = submitMulti;
				break;
			default:
				return;
			}

			pdu.setSourceAddress(
					new Address((byte) smppParametersService.getCofGeneralParameters().getSourceTON().getCode(),
							(byte) smppParametersService.getCofGeneralParameters().getSourceNPI().getCode(),
							smppParametersService.getCofGeneralParameters().getSourceAddress()));

			if (smppParametersService.getCofGeneralParameters()
					.getSendingMessageType() == SendingMessageType.SubmitMulti) {
				long daOrig = 1;
				try {
					daOrig = Long.parseLong(destAddr);
				} catch (Exception e) {

				}
				for (int i2 = 0; i2 < smppParametersService.getCofGeneralParameters()
						.getSubmitMultiMessageCnt(); i2++) {
					// this code can be used for testing of address rejections
					// if(i2 == 0){
					// ((SubmitMulti) pdu).addDestAddresses(new Address((byte)
					// 8, (byte)
					// smppParametersService.getCofGeneralParameters().getDestNPI().getCode(),
					// String
					// .valueOf(daOrig + i2)));
					// }else {
					// ((SubmitMulti) pdu).addDestAddresses(new Address((byte)
					// smppParametersService.getCofGeneralParameters().getDestTON().getCode(),
					// (byte)
					// smppParametersService.getCofGeneralParameters().getDestNPI().getCode(),
					// String
					// .valueOf(daOrig + i2)));
					// }

					((SubmitMulti) pdu).addDestAddresses(
							new Address((byte) smppParametersService.getCofGeneralParameters().getDestTON().getCode(),
									(byte) smppParametersService.getCofGeneralParameters().getDestNPI().getCode(),
									String.valueOf(daOrig + i2)));
				}
			} else {
				pdu.setDestAddress(new Address(
						(byte) smppParametersService.getCofGeneralParameters().getDestTON().getCode(),
						(byte) smppParametersService.getCofGeneralParameters().getDestNPI().getCode(), destAddr));
			}

			pdu.setEsmClass((byte) esmClass);

			switch (validityType) {
			case ValidityPeriod_5min:
				pdu.setValidityPeriod(MessageUtil.printSmppRelativeDate(0, 0, 0, 0, 5, 0));
				break;
			case ValidityPeriod_2hours:
				pdu.setValidityPeriod(MessageUtil.printSmppRelativeDate(0, 0, 0, 2, 0, 0));
				break;
			case ScheduleDeliveryTime_5min:
				pdu.setScheduleDeliveryTime(MessageUtil.printSmppRelativeDate(0, 0, 0, 0, 5, 0));
				break;
			}

			pdu.setDataCoding((byte) dcs);
			pdu.setRegisteredDelivery(
					(byte) smppParametersService.getCofGeneralParameters().getMcDeliveryReceipt().getCode());

			if (buf.length < 250 && smppParametersService.getCofGeneralParameters()
					.getSendingMessageType() != SmppSimulatorParameters.SendingMessageType.DataSm)
				pdu.setShortMessage(buf);
			else {
				Tlv tlv = new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, buf);
				pdu.addOptionalParameter(tlv);
			}

			if (addSegmTlv) {
				byte[] buf1 = new byte[2];
				buf1[0] = 0;
				buf1[1] = (byte) msgRef;
				Tlv tlv = new Tlv(SmppConstants.TAG_SAR_MSG_REF_NUM, buf1);
				pdu.addOptionalParameter(tlv);
				buf1 = new byte[1];
				buf1[0] = (byte) msgLst.size();
				tlv = new Tlv(SmppConstants.TAG_SAR_TOTAL_SEGMENTS, buf1);
				pdu.addOptionalParameter(tlv);
				buf1 = new byte[1];
				buf1[0] = (byte) i1;
				tlv = new Tlv(SmppConstants.TAG_SAR_SEGMENT_SEQNUM, buf1);
				pdu.addOptionalParameter(tlv);
			}
			if (messageClassVal > 0) {
				byte[] buf1 = new byte[1];
				buf1[0] = (byte) messageClassVal;
				Tlv tlv = new Tlv(SmppConstants.TAG_DEST_ADDR_SUBUNIT, buf1);
				pdu.addOptionalParameter(tlv);
			}

			@SuppressWarnings("unused")
			WindowFuture<Integer, PduRequest, PduResponse> future0 = session0.sendRequestPdu(pdu, 10000, false);

			this.messagesSent.incrementAndGet();
			if (this.timer == null) {
				this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Request=" + pdu.getName(), pdu.toString());
			}
		}

		this.segmentsSent.addAndGet(segmentCnt);
	}

	private byte[] encodeSegment(String msg, EncodingType encodingType) {
		if (encodingType == EncodingType.GSM8_DCS_4) {
			return msg.getBytes(isoCharset);
		} else {
			if (smppParametersService.getCofGeneralParameters().getSmppEncoding() == 0) {
				return msg.getBytes(utf8Charset);
			} else if (smppParametersService.getCofGeneralParameters().getSmppEncoding() == 1) {
				return msg.getBytes(ucs2Charset);
			} else {
				GSMCharsetEncoder encoder = (GSMCharsetEncoder) gsm7Charset.newEncoder();
				encoder.setGSMCharsetEncodingData(new GSMCharsetEncodingData(Gsm7EncodingStyle.bit8_smpp_style, null));
				ByteBuffer bb = null;
				try {
					bb = encoder.encode(CharBuffer.wrap(msg));
				} catch (CharacterCodingException e) {
					logger.error("SmppTestingServiceImpl", e);
					this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Have Exception. Can't doStop", e.getMessage());
				}
				byte[] data = new byte[bb.limit()];
				bb.get(data);
				return data;
			}
		}
	}

	private void doStopTimer() {
		if (this.timer != null) {
			for (Timer tm : this.timer) {
				tm.cancel();
			}
			this.timer = null;
		}
	}

	private boolean isStartBulk = false;
	private int threadCount = 10;

	@Override
	public ResReturnDTO stopBulkSending() {
		this.doStopTimer();
		isStartBulk = false;
		return null;
	}

	@Override
	public ResReturnDTO bulkSendingRandom() {
		this.doStopTimer();

		this.timer = new Timer[threadCount];
		for (int i1 = 0; i1 < threadCount; i1++) {
			this.timer[i1] = new Timer();
			this.timer[i1].scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					doSendRandomSmppMessages();
				}
			}, 1 * 1000, 1 * 1000);
		}
		isStartBulk = false;
		return null; // TODO
	}

	private String bigMessage = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
	private AtomicInteger messagesNum = new AtomicInteger();

	private void doSendRandomSmppMessages() {

		Random rand = new Random();

		for (int i1 = 0; i1 < smppParametersService.getCofGeneralParameters().getBulkMessagePerSecond()
				/ threadCount; i1++) {
			Long n = (smppParametersService.getCofGeneralParameters().getBulkDestAddressRangeEnd()
					- smppParametersService.getCofGeneralParameters().getBulkDestAddressRangeStart() + 1);
			if (n < 1L)
				n = 1L;
			int j1 = rand.nextInt(n.intValue());
			String destAddrS = smppParametersService.getCofGeneralParameters().getDestAddress();

			EncodingType encodingType = smppParametersService.getCofGeneralParameters().getEncodingType();
			SplittingType splittingType = smppParametersService.getCofGeneralParameters().getSplittingType();
			String msg = smppParametersService.getCofGeneralParameters().getMessageText();
			this.doSubmitMessage(encodingType, 0, msg, splittingType,
					smppParametersService.getCofGeneralParameters().getValidityType(), destAddrS,
					smppParametersService.getCofGeneralParameters().getMessagingMode(),
					smppParametersService.getCofGeneralParameters().getSpecifiedSegmentLength());
		}
	}

	@Override
	public ResReturnDTO bulkSendingFromPcapFile(final MultipartFile file, final int port) {
		isStartBulk = false;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				doParsePcapFile(file, port);
			}
		});
		t.start();

		return null;
	}

	private void doParsePcapFile(MultipartFile file, int port) {
		try {
			InputStream inputStream = file.getInputStream();

			SmppPcapParser smppPcapParser = new SmppPcapParser(this, inputStream, port);

			smppPcapParser.parse();
			inputStream.close();

		} catch (Exception e) {
			logger.error("SmppTestingServiceImpl", e);
			this.addMessage(CodeStatusUtil.ADD_MESSAGE, "Have Exception. Can't doStop", e.getMessage());
		} finally {
			this.isStartBulk = true;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onNewSmppRequest(BaseSm pdu) throws Exception {
		if (session0 != null) {
			@SuppressWarnings("unused")
			WindowFuture<Integer, PduRequest, PduResponse> future0 = session0.sendRequestPdu(pdu, 10000, false);

			this.messagesSent.incrementAndGet();
		}
	}

	@Override
	public boolean needContinue() {
		return this.isStartBulk;
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return this.monitorExecutor;
	}

	public void setSmppSession(SmppSession smppSession) {
		this.session0 = smppSession;
	}

	public SmppSession getSmppSession() {
		return this.session0;
	}

	public AtomicLong getMsgIdGenerator() {
		return msgIdGenerator;
	}

	private class MyHandler extends StompSessionHandlerAdapter {
		public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
			logger.info("Now connected");
		}
	}
}
