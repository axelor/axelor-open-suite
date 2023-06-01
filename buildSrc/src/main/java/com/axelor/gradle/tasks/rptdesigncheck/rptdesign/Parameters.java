package com.axelor.gradle.tasks.rptdesigncheck.rptdesign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Parameters {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "scalar-parameter")
  private List<ScalarParameter> parameters;

  public Map<String, String> getParameterMap() {
    Map<String, String> parameterMap = new HashMap<>();

    parameters.forEach(
        parameter -> {
          if (!Objects.isNull(parameter.getSimplePropertyList())
              && !Objects.isNull(parameter.getSimplePropertyList().getValue())
              && !Objects.isNull(parameter.getSimplePropertyList().getValue().getValue())) {
            parameterMap.put(
                parameter.getName(), parameter.getSimplePropertyList().getValue().getValue());
          }
        });
    return parameterMap;
  }
}
