package com.axelor.gradle.tasks.csvcheck;

public class CsvLineException extends Exception {

  public CsvLineException(Throwable throwable) {
    super(throwable);
  }

  public CsvLineException(String message) {
    super(message);
  }
}
