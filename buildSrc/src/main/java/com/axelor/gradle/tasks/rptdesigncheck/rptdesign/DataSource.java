package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;
import java.util.Objects;

public class DataSource {
  @JacksonXmlProperty(localName = "oda-data-source")
  private OdaDataSource odaDataSource;

  public OdaDataSource getOdaDataSource() {
    if (!Objects.isNull(odaDataSource)) {
      return odaDataSource;
    }
    OdaDataSource fakeOdaDataSource = new OdaDataSource();
    fakeOdaDataSource.setProperties(new ArrayList<>());
    return fakeOdaDataSource;
  }
}
