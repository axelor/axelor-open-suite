package com.axelor.apps.tool.api;

public class ResponseStructure {

  private final Integer version;

  public ResponseStructure(Integer version) {
    this.version = version;
  }

  public Integer getVersion() {
    return version;
  }
}
