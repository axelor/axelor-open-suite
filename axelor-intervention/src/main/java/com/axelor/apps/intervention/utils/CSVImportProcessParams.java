package com.axelor.apps.intervention.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CSVImportProcessParams {
  private final List<CSVImportProcessParam> params;
  private final long lineNbr;
  private final CSVImportLoggerTool loggerTool;

  public CSVImportProcessParams(
      List<CSVImportProcessParam> values, long lineNbr, CSVImportLoggerTool loggerTool) {
    this.params = values;
    this.lineNbr = lineNbr;
    this.loggerTool = loggerTool;
  }

  public List<String> getValues() {
    return params.stream().map(CSVImportProcessParam::getValue).collect(Collectors.toList());
  }

  public List<CSVImportProcessParam> getParams() {
    return params;
  }

  public String getParamValue(String name) {
    return params.stream()
        .filter(it -> Objects.equals(it.getName(), name))
        .map(CSVImportProcessParam::getValue)
        .findFirst()
        .orElse(null);
  }

  public CSVImportLoggerTool getLoggerTool() {
    return loggerTool;
  }

  public long getLineNbr() {
    return lineNbr;
  }
}
