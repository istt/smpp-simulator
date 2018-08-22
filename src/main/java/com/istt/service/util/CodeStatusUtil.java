/**
 * 
 */
package com.istt.service.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ducgiang8888
 *
 */
public class CodeStatusUtil {

  public static final Integer ADD_MESSAGE = 1;
  public static final Integer REFRESH_STATE = 2;
  public static final Integer SESSION_START_FALSE = 3;
  public static final Integer SESSION_START_SUCCESS = 4;
  public static final Integer SESSION_STOP = 5;
  public static final Integer MESSAGE_SUBMIT_FALSE = 6;
  private void load() {
    mapStatusMessage = new HashMap<Integer, String>();
//    mapStatusMessage.put(REFRESH_STATE, "");
  }
  
  private static Map<Integer, String> mapStatusMessage;
  private static CodeStatusUtil codeStatusUtil;

  private CodeStatusUtil() {
  }

  public static synchronized CodeStatusUtil getInstall() {
    if (codeStatusUtil == null) {
      codeStatusUtil = new CodeStatusUtil();
      codeStatusUtil.load();
    }
    return codeStatusUtil;
  }

  public String getMessage(Integer codeError) {
    if (codeError == null) {
      return "";
    }
    String message = mapStatusMessage.get(codeError);
    if (message == null) {
      message = "";
    };
    return message;
  }

  
}
