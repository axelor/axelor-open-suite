package com.axelor.apps.intervention.utils;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CSVImportLoggerTool {
  private final Path logFile;

  public CSVImportLoggerTool(Path logFile) {
    this.logFile = logFile;
  }

  public void log(String message) {
    try {
      if (logFile != null) {
        Files.write(
            logFile,
            String.format("%n%s", message).getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }

  public void error(String message) {
    try {
      if (logFile != null) {
        Files.write(
            logFile,
            String.format("%n%s : %s", I18n.get("ERROR"), message).getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }

  public void warning(String message) {
    try {
      if (logFile != null) {
        Files.write(
            logFile,
            String.format("%n%s : %s", I18n.get("WARNING"), message)
                .getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }
}
