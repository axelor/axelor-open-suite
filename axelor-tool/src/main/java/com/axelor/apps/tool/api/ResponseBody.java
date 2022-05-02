package com.axelor.apps.tool.api;

import javax.ws.rs.core.Response;

public class ResponseBody {

  private final int codeStatus;
  private final String messageStatus;
  private final ApiStructure object;

  public ResponseBody(Response.Status codeStatus, String messageStatus) {
    this.codeStatus = codeStatus.getStatusCode();
    this.messageStatus = messageStatus;
    this.object = null;
  }

  public ResponseBody(Response.Status codeStatus, String messageStatus, ApiStructure object) {
    this.codeStatus = codeStatus.getStatusCode();
    this.messageStatus = messageStatus;
    this.object = object;
  }

  public int getCodeStatus() {
    return codeStatus;
  }

  public String getMessageStatus() {
    return messageStatus;
  }

  public ApiStructure getObject() {
    return object;
  }
}
