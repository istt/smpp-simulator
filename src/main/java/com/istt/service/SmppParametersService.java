/**
 * 
 */
package com.istt.service;

import com.istt.service.dto.SmppSimulatorParameters;

/**
 * @author GiangDD
 *
 */
public interface SmppParametersService {

	SmppSimulatorParameters getCofGeneralParameters();
	
	SmppSimulatorParameters saveConfSmmppParameters(SmppSimulatorParameters smppParameters);
}
