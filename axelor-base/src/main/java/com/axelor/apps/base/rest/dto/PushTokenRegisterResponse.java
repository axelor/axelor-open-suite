package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.db.PushToken;
import com.axelor.utils.api.ResponseStructure;

public class PushTokenRegisterResponse extends ResponseStructure {

  private final long id;
  private final String token;
  private final String deviceId;
  private final boolean active;

  public PushTokenRegisterResponse(PushToken pushToken) {
    super(pushToken.getVersion());
    this.id = pushToken.getId();
    this.token = pushToken.getToken();
    this.deviceId = pushToken.getDeviceId();
    this.active = pushToken.getIsActive() != null ? pushToken.getIsActive() : false;
  }

  public long getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public boolean isActive() {
    return active;
  }
}
