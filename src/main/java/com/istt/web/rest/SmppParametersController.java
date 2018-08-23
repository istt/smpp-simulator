/**
 * 
 */
package com.istt.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istt.service.SmppParametersService;
import com.istt.service.dto.SmppSimulatorParameters;

/**
 * @author GiangDD
 *
 */
@RestController
@RequestMapping("/api/smpp-paramaters")
public class SmppParametersController {

	@Autowired
	private SmppParametersService smppParametersService;

	@GetMapping(value = "/get-config")
	public ResponseEntity<SmppSimulatorParameters> getConfig() {
		return ResponseEntity.ok().body(smppParametersService.getCofGeneralParameters());
	}

	@PostMapping(value = "/save-config")
	public ResponseEntity<SmppSimulatorParameters> createOrUpdateSmppSession(@RequestBody SmppSimulatorParameters smppParameters) {
		return ResponseEntity.accepted().body(smppParametersService.saveConfSmmppParameters(smppParameters));
	}
}
