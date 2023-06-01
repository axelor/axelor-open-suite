package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "report")
public class Report {
  @JacksonXmlProperty(localName = "parameters")
  private Parameters parameters;

  @JacksonXmlProperty(localName = "data-sources")
  private DataSource dataSource;

  public Parameters getParameters() {
    return parameters;
  }

  public DataSource getDataSource() {
    return dataSource;
  }
}
