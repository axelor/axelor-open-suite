package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotBlank;

public class PushTokenDeactivationRequest extends RequestPostStructure {

  @NotBlank private String token;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
