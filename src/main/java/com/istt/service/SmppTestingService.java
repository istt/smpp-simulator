package com.istt.service;

import org.springframework.web.multipart.MultipartFile;

import com.istt.web.websocket.dto.ResReturnDTO;

public interface SmppTestingService extends SmppAccepter {
	ResReturnDTO startASession() throws Exception;

	ResReturnDTO stopASession() throws Exception;

	ResReturnDTO refreshState() throws Exception;

	ResReturnDTO sendBadPacket() throws Exception;

	ResReturnDTO submitMessage() throws Exception;

	ResReturnDTO bulkSendingRandom() throws Exception;

	ResReturnDTO stopBulkSending() throws Exception;

	ResReturnDTO bulkSendingFromPcapFile(MultipartFile file, int port) throws Exception;
}
