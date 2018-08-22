/**
 * 
 */
package com.istt.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.istt.service.SmppTestingService;
import com.istt.web.websocket.dto.ResReturnDTO;

/**
 * @author GiangDD
 *
 */
@RestController
@RequestMapping("/api/smpp-test/")
public class SmppTestingController {

	@Autowired
	private SmppTestingService smppTestingService;

	@RequestMapping(value = "start-session", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO startASession() {
		return smppTestingService.startASession();
	}
	
	@RequestMapping(value = "stop-session", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO stopASession() {
		return smppTestingService.stopASession();
	}

	@RequestMapping(value = "refresh-state", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO refreshState() {
		return smppTestingService.refreshState();
	}

	@RequestMapping(value = "send-bad-packet", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO sendBadPacket() {
		return smppTestingService.sendBadPacket();
	}

	@RequestMapping(value = "submit-message", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO submitMessage() {
		return smppTestingService.submitMessage();
	}

	@RequestMapping(value = "bulk-sending-random", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO bulkSendingRandom() {
		return smppTestingService.bulkSendingRandom();
	}

	@RequestMapping(value = "bulk-sending-from-pcap-file", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO bulkSendingFromPcapFile(@RequestParam("port") Integer port,
			@RequestParam("file") MultipartFile file) {
		return smppTestingService.bulkSendingFromPcapFile(file, port);
	}
	
	@RequestMapping(value = "stop-bulk-sending", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResReturnDTO stopBulkSending() {
		return smppTestingService.stopBulkSending();
	}
}
