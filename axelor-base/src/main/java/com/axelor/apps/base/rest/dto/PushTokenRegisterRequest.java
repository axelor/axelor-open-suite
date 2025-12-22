package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotBlank;

public class PushTokenRegisterRequest extends RequestPostStructure {

  @NotBlank private String token;
  private String deviceId;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getDeviceId() {
    return deviceId == null || deviceId.isBlank() ? null : deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
}
