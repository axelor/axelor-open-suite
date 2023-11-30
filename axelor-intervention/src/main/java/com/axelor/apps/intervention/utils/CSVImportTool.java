package com.axelor.apps.intervention.utils;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVImportTool implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final CSVReader csvReader;
  private final Stream<String> lines;
  private final long totalLines;
  private final CSVImportLoggerTool loggerTool;
  private int totalRecord;
  private int successRecord;
  private int anomaly;

  public CSVImportTool(Path path, Path logFile) throws IOException {
    this.csvReader = new CSVReader(new FileReader(path.toFile()), ';');
    this.lines = Files.lines(path);
    this.totalLines = lines.count();
    this.loggerTool = new CSVImportLoggerTool(logFile);
    this.totalRecord = 0;
    this.successRecord = 0;
    this.anomaly = 0;
  }

  public static <T> T processField(
      String value,
      T defaultValue,
      boolean required,
      String noValueMsg,
      String nullParsedValueMsg,
      CSVImportLoggerTool loggerTool,
      Function<String, T> parser) {
    if (StringUtils.isBlank(value)) {
      return getDefaultValue(defaultValue, required, noValueMsg, loggerTool);
    }
    try {
      T t = parser.apply(value);
      if (t == null) {
        return getDefaultValue(defaultValue, required, nullParsedValueMsg, loggerTool);
      }
      return t;
    } catch (Exception e) {
      return getDefaultValue(defaultValue, required, e, loggerTool);
    }
  }

  private static <T> T getDefaultValue(
      T defaultValue, boolean required, String message, CSVImportLoggerTool loggerTool) {
    if (required) {
      throw new IllegalArgumentException(message);
    } else {
      if (StringUtils.notBlank(message)) {
        loggerTool.warning(message);
      }
    }
    return defaultValue;
  }

  private static <T> T getDefaultValue(
      T defaultValue, boolean required, Throwable e, CSVImportLoggerTool loggerTool) {
    if (required) {
      throw new IllegalArgumentException(e);
    } else {
      loggerTool.warning(e.getLocalizedMessage());
    }
    return defaultValue;
  }

  /**
   * Parse all CSV lines and execute the given function for each one.
   *
   * @param isFirstLineHeader to indicate if the first line has to be treated as a header line.
   * @param transactionalBlockSize to configure the size of a transactional block. Pass null or 0 to
   *     not execute JPA clear.
   * @param function to execute on each CSV lines.
   * @param <T> the function's return type.
   * @return the list of parsed type T records.
   * @throws IOException in case of issues while reading CSV lines or while writing in the log file.
   */
  public <T> List<T> parseAllLines(
      boolean isFirstLineHeader,
      Integer transactionalBlockSize,
      Function<CSVImportProcessParams, T> function)
      throws IOException {
    boolean jpaClear = transactionalBlockSize != null && transactionalBlockSize > 0;
    if (!jpaClear) {
      transactionalBlockSize = 20;
    }
    String[] names = null;
    String[] values;
    List<T> list = new ArrayList<>();
    if (isFirstLineHeader) {
      names = csvReader.readNext();
    }
    while ((values = csvReader.readNext()) != null) {
      try {
        List<CSVImportProcessParam> params = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
          params.add(
              new CSVImportProcessParam(
                  names != null && i < names.length ? names[i] : null, values[i]));
        }
        T t =
            function.apply(
                new CSVImportProcessParams(params, csvReader.getLinesRead(), loggerTool));
        if (t != null) {
          list.add(t);
          successRecord++;
        } else {
          anomaly++;
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
        LOG.error(
            "There was an exception while parsing a line : {} {}", values, e.getLocalizedMessage());
        loggerTool.error(getStackTrace(e));
        anomaly++;
      } finally {
        if (totalRecord++ % transactionalBlockSize == 0) {
          LOG.info(
              "Progress :: {}% completed.",
              (long)
                  (((double) totalRecord)
                      / ((double) (isFirstLineHeader ? this.totalLines - 1 : this.totalLines))
                      * 100));
          if (jpaClear) {
            JPA.clear();
          }
        }
      }
    }
    String summary =
        String.format(
            "%n%n%s %s %s %s%n%s %s",
            I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_1),
            totalRecord,
            I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_2),
            successRecord,
            I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_3),
            anomaly);
    loggerTool.log(summary);
    return list;
  }

  private String getStackTrace(Throwable e) {
    String message = "";
    if (StringUtils.notBlank(e.getLocalizedMessage())) {
      message = String.format("%s", e.getLocalizedMessage());
    }
    if (e.getCause() != null) {
      message += String.format("%n%s", getStackTrace(e.getCause()));
    }
    return message;
  }

  @Override
  public void close() throws IOException {
    csvReader.close();
    lines.close();
  }
}
