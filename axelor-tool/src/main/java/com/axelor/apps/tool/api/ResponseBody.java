package com.axelor.apps.tool.api;

public class ResponseBody {

  private final int codeStatus;
  private final String messageStatus;
  private final ApiStructure object;

  public ResponseBody(int codeStatus, String messageStatus) {
    this.codeStatus = codeStatus;
    this.messageStatus = messageStatus;
    this.object = null;
  }

  public ResponseBody(int codeStatus, String messageStatus, ApiStructure object) {
    this.codeStatus = codeStatus;
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
