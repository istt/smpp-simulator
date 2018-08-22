package com.istt.service.impl;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.istt.service.SmppParametersService;
import com.istt.service.dto.SmppSimulatorParameters;

@Service("SmppParametersService")
public class SmppParametersServiceImpl implements SmppParametersService {

	private static SmppSimulatorParameters data;
	private static SmppParametersServiceImpl smppParametersService;

	public static SmppParametersServiceImpl getInstance() {
		return smppParametersService;
	}
	
	public SmppParametersServiceImpl() {
		super();
	}



	@PostConstruct
	private void load() {
		data = new SmppSimulatorParameters();
		smppParametersService = this;
	}

	@Override
	public SmppSimulatorParameters getCofGeneralParameters() {
		return data;
	}

	@Override
	public void saveConfSmmppParameters(SmppSimulatorParameters smppParameters) {
		data = smppParameters;
	}

}
