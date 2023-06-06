package com.axelor.gradle.tasks;

import com.axelor.gradle.tasks.rptdesigncheck.RptdesignLineException;
import com.axelor.gradle.tasks.rptdesigncheck.rptdesign.EncryptedProperty;
import com.axelor.gradle.tasks.rptdesigncheck.rptdesign.Report;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public class CheckCredentialsInRptdesign extends DefaultTask {

  @InputFiles @SkipWhenEmpty private FileTree files;

  private static final String STANDARD_DB_URL = "jdbc:postgresql://localhost:5432/";
  private static final String ERROR_DEFAULT_DBNAME =
      "\nThe BIRT file %s contains wrong Default DBName.";
  private static final String ERROR_DEFAULT_USERNAME =
      "\nThe BIRT file %s contains Default UserName credential.";
  private static final String ERROR_DEFAULT_PASSWORD =
      "\nThe BIRT file %s contains ODA Password credential.";
  private static final String ERROR_ODA_URL = "\nThe BIRT file %s contains wrong ODA URL.";
  private static final String ERROR_ODA_USER = "\nThe BIRT file %s contains ODA User credential.";
  private static final String ERROR_ODA_PASSWORD =
      "\nThe BIRT file %s contains ODA Password credential.";

  private List<String> errorList = new ArrayList<>();

  public FileTree getFiles() {
    return files;
  }

  public void setFiles(FileTree files) {
    this.files = files;
  }

  @TaskAction
  public void check() throws RptdesignLineException {
    for (File file : getFiles()) {
      checkRptdesignFile(file);
    }
    if (!errorList.isEmpty()) {
      throw new RptdesignLineException(String.join("", errorList));
    }
  }

  protected void checkRptdesignFile(File file) throws RptdesignLineException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      Report report = xmlMapper.readValue(file, Report.class);
      Map<String, String> parameters = report.getParameters().getParameterMap();
      Map<String, String> properties = report.getDataSource().getOdaDataSource().getPropertyMap();
      EncryptedProperty password = report.getDataSource().getOdaDataSource().getEncryptedProperty();

      if (!Objects.isNull(parameters.get("DBName"))
          && parameters.get("DBName").compareTo(STANDARD_DB_URL) != 0) {
        errorList.add(String.format(ERROR_DEFAULT_DBNAME, file.getPath()));
      }
      if (!Objects.isNull(parameters.get("UserName"))) {
        errorList.add(String.format(ERROR_DEFAULT_USERNAME, file.getPath()));
      }
      if (!Objects.isNull(parameters.get("Password"))) {
        errorList.add(String.format(ERROR_DEFAULT_PASSWORD, file.getPath()));
      }

      if (!Objects.isNull(properties.get("odaURL"))
          && properties.get("odaURL").compareTo(STANDARD_DB_URL) != 0) {
        errorList.add(String.format(ERROR_ODA_URL, file.getPath()));
      }
      if (!Objects.isNull(properties.get("odaUser"))) {
        errorList.add(String.format(ERROR_ODA_USER, file.getPath()));
      }
      if (!Objects.isNull(password) && !Objects.isNull(password.getEncryptedValue())) {
        errorList.add(String.format(ERROR_ODA_PASSWORD, file.getPath()));
      }
    } catch (Exception e) {
      throw new RptdesignLineException(e);
    }
  }
}
