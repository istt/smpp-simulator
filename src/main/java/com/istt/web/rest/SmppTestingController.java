/**
 * 
 */
package com.istt.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping("start-session")
	public ResponseEntity<ResReturnDTO> startASession() throws Exception {
		return ResponseEntity.accepted().body(smppTestingService.startASession());
	}
	
	@GetMapping(value = "stop-session")
	@ResponseBody
	public ResReturnDTO stopASession() throws Exception {
		return smppTestingService.stopASession();
	}

	@GetMapping(value = "refresh-state")
	@ResponseBody
	public ResReturnDTO refreshState() throws Exception {
		return smppTestingService.refreshState();
	}

	@GetMapping(value = "send-bad-packet")
	@ResponseBody
	public ResReturnDTO sendBadPacket() throws Exception {
		return smppTestingService.sendBadPacket();
	}

	@GetMapping(value = "submit-message")
	public ResponseEntity<ResReturnDTO> submitMessage() throws Exception {
		return ResponseEntity.accepted().body(smppTestingService.submitMessage());
	}

	@GetMapping(value = "bulk-sending-random")
	@ResponseBody
	public ResReturnDTO bulkSendingRandom() throws Exception {
		return smppTestingService.bulkSendingRandom();
	}

	@PostMapping(value = "bulk-sending-from-pcap-file")
	@ResponseBody
	public ResReturnDTO bulkSendingFromPcapFile(@RequestParam("port") Integer port,
			@RequestParam("file") MultipartFile file) throws Exception {
		return smppTestingService.bulkSendingFromPcapFile(file, port);
	}
	
	@GetMapping(value = "stop-bulk-sending")
	@ResponseBody
	public ResReturnDTO stopBulkSending() throws Exception {
		return smppTestingService.stopBulkSending();
	}
}
