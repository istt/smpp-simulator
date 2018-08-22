/**
 * 
 */
package com.istt.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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

	@RequestMapping(value = "/get-config", method = { RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public SmppSimulatorParameters getConfig() {

		return smppParametersService.getCofGeneralParameters();
	}

	@RequestMapping(value = "/save-config", method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public void createOrUpdateSmppSession(@RequestBody SmppSimulatorParameters smppParameters) {
		smppParametersService.saveConfSmmppParameters(smppParameters);
	}
}
