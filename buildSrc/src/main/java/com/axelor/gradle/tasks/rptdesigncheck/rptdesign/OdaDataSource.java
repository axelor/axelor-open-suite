package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdaDataSource {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "property")
  private List<Property> properties;

  @JacksonXmlProperty(localName = "encrypted-property")
  private EncryptedProperty encryptedProperty;

  public Map<String, String> getPropertyMap() {
    Map<String, String> propertyMap = new HashMap<>();

    properties.forEach(property -> propertyMap.put(property.getName(), property.getValue()));
    return propertyMap;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  public EncryptedProperty getEncryptedProperty() {
    return encryptedProperty;
  }

  public void setEncryptedProperty(EncryptedProperty encryptedProperty) {
    this.encryptedProperty = encryptedProperty;
  }
}
