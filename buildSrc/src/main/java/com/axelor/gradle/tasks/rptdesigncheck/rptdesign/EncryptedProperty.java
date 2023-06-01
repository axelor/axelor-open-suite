package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class EncryptedProperty {
  @JacksonXmlProperty(isAttribute = true, localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "encryptionID")
  private String encryptionID;

  @JacksonXmlText private String encryptedValue;

  public String getName() {
    return name;
  }

  public String getEncryptionID() {
    return encryptionID;
  }

  public String getEncryptedValue() {
    return encryptedValue;
  }
}
