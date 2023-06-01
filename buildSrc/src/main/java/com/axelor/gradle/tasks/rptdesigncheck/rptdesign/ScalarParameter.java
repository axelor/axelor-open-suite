package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ScalarParameter {
  @JacksonXmlProperty(isAttribute = true, localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "simple-property-list")
  private SimplePropertyList simplePropertyList;

  public String getName() {
    return name;
  }

  public SimplePropertyList getSimplePropertyList() {
    return simplePropertyList;
  }
}
