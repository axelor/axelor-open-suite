package com.axelor.apps.sale.rest.dto;

public class UnitResponse {
  protected String name;
  protected String labelToPrinting;

  public UnitResponse(String name, String labelToPrinting) {
    this.name = name;
    this.labelToPrinting = labelToPrinting;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabelToPrinting() {
    return labelToPrinting;
  }

  public void setLabelToPrinting(String labelToPrinting) {
    this.labelToPrinting = labelToPrinting;
  }
}
