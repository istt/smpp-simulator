/**
 * 
 */
package com.istt.web.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.istt.web.websocket.dto.ResReturnDTO;

/**
 * @author GiangDD
 *
 */

@Controller
public class NotifSmppMessageController {
	
	@MessageMapping("/topic/smpp")
    @SendTo("/topic/smpp-logger")
	public ResReturnDTO sendNotifSmppMessage(ResReturnDTO sms) {
		return sms;
	}
}
