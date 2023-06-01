package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Property {
  @JacksonXmlProperty(isAttribute = true, localName = "name")
  private String name;

  @JacksonXmlText private String value;

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
