package com.axelor.apps.intervention.utils;

public class CSVImportProcessParam {
  private final String name;
  private final String value;

  public CSVImportProcessParam(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
