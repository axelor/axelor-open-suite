package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Value {
  @JacksonXmlProperty(isAttribute = true, localName = "type")
  private String type;

  @JacksonXmlText private String value;

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }
}
