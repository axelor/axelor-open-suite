package com.axelor.gradle.tasks.rptdesigncheck;

public class RptdesignLineException extends Exception {

  public RptdesignLineException(Throwable throwable) {
    super(throwable);
  }

  public RptdesignLineException(String message) {
    super(message);
  }
}
