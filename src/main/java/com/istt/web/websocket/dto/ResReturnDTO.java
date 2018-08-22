/**
 * 
 */
package com.istt.web.websocket.dto;

import java.io.Serializable;
import java.util.List;

import com.istt.service.util.CodeStatusUtil;


/**
 * @author ducgiang8888
 *
 */
public class ResReturnDTO implements Serializable {
  private static final long serialVersionUID = -2289394628497063904L;

  private Integer codeStatus;
  private String messageStatus;
  private List<Object> listData;
  private Object data;

  public ResReturnDTO() {
  }

  public ResReturnDTO(Integer codeStatus) {
    this.setCodeStatus(codeStatus);
  }
  
  public Integer getCodeStatus() {
    return codeStatus;
  }

  public void setCodeStatus(Integer codeStatus) {
    this.codeStatus = codeStatus;
    if (this.codeStatus!=null && this.getMessageStatus() == null) {
      this.setMessageStatus(CodeStatusUtil.getInstall().getMessage(codeStatus));
    }
  }

  public String getMessageStatus() {
    return messageStatus;
  }

  public void setMessageStatus(String messageStatus) {
    this.messageStatus = messageStatus;
  }

  public List<Object> getListData() {
    return listData;
  }

  public void setListData(List<Object> listData) {
    this.listData = listData;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

}
