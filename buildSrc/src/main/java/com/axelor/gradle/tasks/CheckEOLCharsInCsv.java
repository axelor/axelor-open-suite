package com.axelor.gradle.tasks;

import com.axelor.gradle.tasks.csvcheck.CsvLineException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public class CheckEOLCharsInCsv extends DefaultTask {

  @InputFiles @SkipWhenEmpty private FileTree files;

  private static final String ERROR_EOL_CR =
      "The CSV file %s has CR (Mac OS) used as line separator. It should be CR/LF.";
  private static final String ERROR_EOL_LF =
      "The CSV file %s has LF (Unix) used as line separator. It should be CR/LF.";

  private List<String> errorList = new ArrayList<>();

  public FileTree getFiles() {
    return files;
  }

  public void setFiles(FileTree files) {
    this.files = files;
  }

  @TaskAction
  public void check() throws CsvLineException {
    for (File file : getFiles()) {
      checkCsvFile(file);
    }
    if (!errorList.isEmpty()) {
      throw new CsvLineException(
          errorList.stream().collect(Collectors.joining(System.lineSeparator())));
    }
  }

  protected void checkCsvFile(File file) throws CsvLineException {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      int readCharInt;
      boolean checkNextCharIsLF = false;
      while ((readCharInt = bufferedReader.read()) != -1) {
        char readChar = (char) readCharInt;
        if (checkNextCharIsLF) {
          if (readChar != '\n') {
            errorList.add(String.format(ERROR_EOL_CR, file.getPath()));
            break;
          }
          checkNextCharIsLF = false;
        } else if (readChar == '\n') {
          errorList.add(String.format(ERROR_EOL_LF, file.getPath()));
          break;
        }
        if (readChar == '\r') {
          checkNextCharIsLF = true;
        }
      }
    } catch (Exception e) {
      throw new CsvLineException(e);
    }
  }
}
