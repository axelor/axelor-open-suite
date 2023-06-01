package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SimplePropertyList {
  @JacksonXmlProperty(isAttribute = true, localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "value")
  private Value value;

  public String getName() {
    return name;
  }

  public Value getValue() {
    return value;
  }
}
