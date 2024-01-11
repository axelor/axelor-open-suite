/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.db.DataBackup;
import com.axelor.apps.base.db.repo.AnonymizerLineRepository;
import com.axelor.apps.base.db.repo.DataBackupRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.common.csv.CSVFile;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.internal.DBHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.App;
import com.axelor.utils.date.DateTool;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.naming.NamingException;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupCreateService {

  protected static final char SEPARATOR = ',';
  protected static final char REFERENCE_FIELD_SEPARATOR = '|';
  protected static final int BUFFER_SIZE = 1000;

  protected boolean notNullReferenceFlag;
  protected boolean referenceFlag;
  protected boolean byteArrFieldFlag = false;
  protected List<String> fileNameList;
  protected static Set<String> exceptColumnNameList =
      ImmutableSet.of(
          "importOrigin",
          "importId",
          "updatedBy",
          "createdBy",
          "updatedOn",
          "createdOn",
          "archived",
          "version",
          "attrs");

  StringBuilder sb = new StringBuilder();

  protected static Map<Object, Object> AutoImportModelMap =
      ImmutableMap.builder()
          .put("com.axelor.studio.db.App", "self.code = :code")
          .put("com.axelor.auth.db.Role", "self.name = :name")
          .put("com.axelor.auth.db.User", "self.code = :code")
          .put("com.axelor.auth.db.Permission", "self.name = :name")
          .put("com.axelor.auth.db.Group", "self.code = :code")
          .put("com.axelor.apps.base.db.Language", "self.code = :code")
          .put("com.axelor.apps.base.db.BirtTemplate", "self.name = :name")
          .put("com.axelor.apps.base.db.BirtTemplateParameter", "self.name = :name")
          .put("com.axelor.apps.crm.db.EventCategory", "self.code = :code")
          .put("com.axelor.apps.account.db.AccountChart", "self.code = :code")
          .put("com.axelor.apps.bankpayment.db.BankOrderFileFormat", "self.name = :name")
          .put("com.axelor.apps.bankpayment.db.BankStatementFileFormat", "self.name = :name")
          .build();

  protected DataBackupRepository dataBackupRepository;
  protected MetaModelRepository metaModelRepo;
  protected MetaFiles metaFiles;
  protected AnonymizeService anonymizeService;
  protected AnonymizerLineRepository anonymizerLineRepository;
  protected Logger LOG = LoggerFactory.getLogger(getClass());
  protected DataBackupAnonymizeService dataBackupAnonymizeService;

  @Inject
  public DataBackupCreateService(
      DataBackupRepository dataBackupRepository,
      MetaModelRepository metaModelRepo,
      MetaFiles metaFiles,
      AnonymizeService anonymizeService,
      AnonymizerLineRepository anonymizerLineRepository,
      DataBackupAnonymizeService dataBackupAnonymizeService) {
    this.dataBackupRepository = dataBackupRepository;
    this.metaModelRepo = metaModelRepo;
    this.metaFiles = metaFiles;
    this.anonymizeService = anonymizeService;
    this.anonymizerLineRepository = anonymizerLineRepository;
    this.dataBackupAnonymizeService = dataBackupAnonymizeService;
  }

  /* Generate csv Files for each individual MetaModel and single config file */
  public DataBackup create(DataBackup dataBackup) throws IOException {
    File tempDir = Files.createTempDirectory(null).toFile();
    String tempDirectoryPath = tempDir.getAbsolutePath();
    int fetchLimit = dataBackup.getFetchLimit();
    int errorsCount = 0;
    byte[] salt = null;

    fileNameList = new ArrayList<>();
    List<MetaModel> metaModelList = getMetaModels(dataBackup.getAnonymizer() != null);

    LinkedList<CSVInput> simpleCsvs = new LinkedList<>();
    LinkedList<CSVInput> refernceCsvs = new LinkedList<>();
    LinkedList<CSVInput> notNullReferenceCsvs = new LinkedList<>();
    Map<String, List<String>> subClassesMap = getSubClassesMap(dataBackup.getAnonymizer() != null);

    if (dataBackup.getCheckAllErrorFirst()) {
      dataBackup.setFetchLimit(1);

      errorsCount = checkErrors(dataBackup, metaModelList, tempDirectoryPath, subClassesMap);

      dataBackup.setFetchLimit(fetchLimit);
      fileNameList.clear();
    }

    if (dataBackup.getAnonymizer() != null) {
      salt = anonymizeService.getSalt();
    }

    if (errorsCount == 0) {
      for (MetaModel metaModel : metaModelList) {

        try {
          List<String> subClasses = subClassesMap.get(metaModel.getFullName());
          long totalRecord = getMetaModelDataCount(metaModel, subClasses);
          if (!dataBackup.getIsProcessEmptyTable() && totalRecord < 1) {
            continue;
          }

          LOG.debug("Exporting Model : " + metaModel.getFullName());
          notNullReferenceFlag = false;
          referenceFlag = false;

          File templateFile = new File(tempDirectoryPath, metaModel.getName() + ".csv");
          CSVFile csvFormat =
              CSVFile.DEFAULT.withDelimiter(SEPARATOR).withQuoteAll().withFirstRecordAsHeader();
          CSVPrinter printer = csvFormat.write(templateFile);
          CSVInput csvInput =
              writeCSVData(
                  metaModel, printer, dataBackup, totalRecord, subClasses, tempDirectoryPath, salt);
          printer.close();

          if (notNullReferenceFlag) {
            notNullReferenceCsvs.add(csvInput);
          } else if (referenceFlag) {
            refernceCsvs.add(csvInput);
            CSVInput temcsv = new CSVInput();
            temcsv.setFileName(csvInput.getFileName());
            temcsv.setTypeName(csvInput.getTypeName());

            if (dataBackup.getIsRelativeDate()) {
              temcsv.setBindings(new ArrayList<>());
              getCsvInputForDateorDateTime(metaModel, temcsv);
            }
            if (AutoImportModelMap.containsKey(csvInput.getTypeName())) {
              temcsv.setSearch(AutoImportModelMap.get(csvInput.getTypeName()).toString());
            }
            if (Class.forName(metaModel.getFullName()).getSuperclass() == App.class) {
              temcsv.setSearch("self.code = :code");
            }
            if (!AutoImportModelMap.containsKey(csvInput.getTypeName())
                && !((Class.forName(metaModel.getFullName()).getSuperclass()).equals(App.class))) {
              temcsv.setSearch("self.importId = :importId");
            }
            simpleCsvs.add(temcsv);
          } else {
            simpleCsvs.add(csvInput);
          }

          fileNameList.add(metaModel.getName() + ".csv");
        } catch (ClassNotFoundException | IOException e) {
          TraceBackService.trace(e, DataBackupService.class.getName());
        } catch (Exception e) {
          JPA.em().getTransaction().rollback();
          if (!dataBackup.getCheckAllErrorFirst()) {
            sb.append("\nError occured while processing model : " + metaModel.getFullName() + "\n");
            sb.append(e.getMessage() + "\n");
          }
          JPA.em().getTransaction().begin();
          dataBackup = dataBackupRepository.find(dataBackup.getId());
          errorsCount++;
        }
      }

      CSVConfig csvConfig = new CSVConfig();
      csvConfig.setInputs(simpleCsvs);
      csvConfig.getInputs().addAll(notNullReferenceCsvs);
      csvConfig.getInputs().addAll(refernceCsvs);
      csvConfig.getInputs().addAll(notNullReferenceCsvs);
      generateConfig(tempDirectoryPath, csvConfig);

      fileNameList.add(DataBackupServiceImpl.CONFIG_FILE_NAME);
    }

    try {
      if (!Strings.isNullOrEmpty(sb.toString())) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmSS");
        String logFileName = "DataBackupLog_" + LocalDateTime.now().format(formatter) + ".log";

        File file = new File(tempDir.getAbsolutePath(), logFileName);
        PrintWriter pw = new PrintWriter(file);

        pw.write(sb.toString());
        pw.close();

        if (file != null) {
          dataBackup.setLogMetaFile(metaFiles.upload(file));
        }
      }

      if (errorsCount == 0) {
        File zippedFile = generateZIP(tempDirectoryPath, fileNameList);
        dataBackup.setBackupMetaFile(metaFiles.upload(zippedFile));
      } else {
        dataBackup.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_ERROR);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return dataBackup;
  }

  protected void getCsvInputForDateorDateTime(MetaModel metaModel, CSVInput csvInput) {
    try {
      Mapper metaModelMapper = Mapper.of(Class.forName(metaModel.getFullName()));
      Property[] properties = metaModelMapper.getProperties();
      for (Property property : properties) {
        String propertyType = property.getType().toString();
        if ((propertyType.equals("DATE") || propertyType.equals("DATETIME"))
            && !property.getName().equals("createdOn")
            && !property.getName().equals("updatedOn")) {
          getDateOrDateTimeHeader(property, csvInput);
        }
      }
    } catch (ClassNotFoundException e) {
      TraceBackService.trace(e);
    }
  }

  /* Get All MetaModels */
  protected List<MetaModel> getMetaModels(boolean anonymizeData) {
    String filterStr = "";
    if (anonymizeData) {
      filterStr =
          "self.packageName NOT LIKE '%meta%' AND self.packageName !='com.axelor.studio.db' AND self.name NOT IN ('DataBackup','MailMessage') AND self.tableName IS NOT NULL";
    } else {
      filterStr =
          "self.packageName NOT LIKE '%meta%' AND self.packageName !='com.axelor.studio.db' AND self.name!='DataBackup' AND self.tableName IS NOT NULL";
    }

    List<MetaModel> metaModels = metaModelRepo.all().filter(filterStr).order("fullName").fetch();
    metaModels.add(metaModelRepo.findByName(MetaFile.class.getSimpleName()));
    metaModels.add(metaModelRepo.findByName(MetaJsonField.class.getSimpleName()));
    return metaModels;
  }

  protected Map<String, List<String>> getSubClassesMap(boolean anonymizeData) {
    List<MetaModel> metaModels = getMetaModels(anonymizeData);
    List<String> subClasses;
    Map<String, List<String>> subClassMap = new HashMap<>();
    for (MetaModel metaModel : metaModels) {
      try {
        subClasses = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Class<AuditableModel> superClass =
            (Class<AuditableModel>) Class.forName(metaModel.getFullName()).getSuperclass();
        if (superClass != AuditableModel.class) {
          if (!subClassMap.isEmpty() && subClassMap.containsKey(superClass.getName())) {
            subClasses = subClassMap.get(superClass.getName());
          }
          subClasses.add(metaModel.getName());
          subClassMap.put(superClass.getName(), subClasses);
        }
      } catch (ClassNotFoundException e) {
        String strError = "Class not found issue: ";
        sb.append(strError)
            .append(e.getLocalizedMessage() + "-----------------------------------------\n");
        e.printStackTrace();
      }
    }
    return subClassMap;
  }

  /* Get All Data of Specific MetaModel */
  protected List<Model> getMetaModelDataList(
      MetaModel metaModel, int start, Integer fetchLimit, List<String> subClasses)
      throws ClassNotFoundException {

    Query<Model> query = getQuery(metaModel, subClasses);

    if (query != null) {
      return query.fetch(fetchLimit, start);
    }

    return null;
  }

  protected long getMetaModelDataCount(MetaModel metaModel, List<String> subClasses)
      throws ClassNotFoundException {
    Query<Model> query = getQuery(metaModel, subClasses);
    long count = 0;
    if (query != null) {
      count = query.count();
    }
    return count;
  }

  protected Query<Model> getQuery(MetaModel metaModel, List<String> subClasses)
      throws ClassNotFoundException {
    StringBuilder whereStr = new StringBuilder();
    if (subClasses != null && !subClasses.isEmpty()) {
      for (String subClassName : subClasses) {
        whereStr.append(whereStr.length() > 0 ? " AND " : "");
        whereStr.append("id NOT IN (select id from ").append(subClassName).append(")");
      }
    }
    @SuppressWarnings("unchecked")
    Class<Model> klass = (Class<Model>) Class.forName(metaModel.getFullName());
    JpaRepository<Model> model = null;
    Query<Model> query = null;
    try {
      model = JpaRepository.of(klass);
    } catch (Exception e) {
      TraceBackService.trace(e, DataBackupService.class.getName());
    }
    if (model != null) {
      query = JpaRepository.of(klass).all();
      if (StringUtils.notEmpty(whereStr.toString())) {
        query.filter(whereStr.toString());
      }

      try {
        Connection connection = DBHelper.getConnection();
        DatabaseMetaData dbmd = connection.getMetaData();

        List<Property> properties = model.fields();
        for (Property property : properties) {
          if (property.isRequired()) {
            ResultSet res =
                dbmd.getColumns(
                    null,
                    null,
                    metaModel.getTableName().toLowerCase(),
                    property.getName().replaceAll("([^_A-Z])([A-Z])", "$1_$2").toLowerCase());

            while (res.next()) {
              if (res.getInt("NULLABLE") == 1) {
                String strErr = "Required field issue in table: ";
                sb.append(strErr)
                    .append(
                        "Model: "
                            + metaModel.getName()
                            + ", Field: "
                            + property.getName()
                            + "-----------------------------------------\n");
                query = null;
              }
            }
          }
        }
        connection.close();
      } catch (SQLException | NamingException e) {
        e.printStackTrace();
      }
    }
    return query;
  }

  protected CSVInput writeCSVData(
      MetaModel metaModel,
      CSVPrinter printer,
      DataBackup dataBackup,
      long totalRecord,
      List<String> subClasses,
      String dirPath,
      byte[] salt)
      throws AxelorException, IOException {

    CSVInput csvInput = new CSVInput();
    boolean headerFlag = true;
    List<String> dataArr;
    List<String> headerArr = new ArrayList<>();
    List<Model> dataList;

    try {
      Mapper metaModelMapper = Mapper.of(Class.forName(metaModel.getFullName()));
      Property[] pro = metaModelMapper.getProperties();
      Integer fetchLimit = dataBackup.getFetchLimit();
      boolean isRelativeDate = dataBackup.getIsRelativeDate();
      boolean updateImportId = dataBackup.getUpdateImportId();

      csvInput.setFileName(metaModel.getName() + ".csv");
      csvInput.setTypeName(metaModel.getFullName());
      csvInput.setBindings(new ArrayList<>());

      if (totalRecord > 0) {
        for (int i = 0; i < totalRecord; i = i + fetchLimit) {

          dataList = getMetaModelDataList(metaModel, i, fetchLimit, subClasses);

          if (dataList != null && !dataList.isEmpty()) {
            dataBackup = dataBackupRepository.find(dataBackup.getId());
            for (Object dataObject : dataList) {
              dataArr = new ArrayList<>();

              for (Property property : pro) {
                if (isPropertyExportable(property)) {
                  if (headerFlag) {
                    String headerStr = getMetaModelHeader(property, csvInput, isRelativeDate);
                    headerArr.add(headerStr);
                  }
                  dataArr.add(
                      getMetaModelData(
                          metaModel.getName(),
                          metaModelMapper,
                          property,
                          dataObject,
                          dirPath,
                          isRelativeDate,
                          updateImportId,
                          dataBackup));
                }
              }
              if (headerFlag) {
                if (byteArrFieldFlag) {
                  csvInput.setCallable(
                      "com.axelor.apps.base.service.DataBackupRestoreService:importObjectWithByteArray");
                  byteArrFieldFlag = false;
                }
                printer.printRecord(headerArr);
                headerFlag = false;
              }

              if ("Partner".equals(metaModel.getName()) && dataBackup.getAnonymizer() != null) {
                dataArr =
                    dataBackupAnonymizeService.csvComputeAnonymizedFullname(dataArr, headerArr);
              }

              if (dataBackup.getAnonymizer() != null) {
                dataBackupAnonymizeService.csvAnonymizeImportId(dataArr, headerArr, salt);
              }

              printer.printRecord(dataArr);
            }
          }
          JPA.clear();
        }
      } else {
        for (Property property : pro) {
          if (isPropertyExportable(property)) {
            String headerStr = getMetaModelHeader(property, csvInput, isRelativeDate);
            headerArr.add(headerStr);
          }
        }
        if (byteArrFieldFlag) {
          csvInput.setCallable(
              "com.axelor.apps.base.service.DataBackupRestoreService:importObjectWithByteArray");
          byteArrFieldFlag = false;
        }
        printer.printRecord(headerArr);
      }

      if (AutoImportModelMap.containsKey(csvInput.getTypeName())) {
        csvInput.setSearch(AutoImportModelMap.get(csvInput.getTypeName()).toString());
      } else if (Class.forName(metaModel.getFullName()).getSuperclass() == App.class) {
        csvInput.setSearch("self.code = :code");
      } else {
        csvInput.setSearch("self.importId = :importId");
      }
    } catch (ClassNotFoundException e) {
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    return csvInput;
  }

  protected boolean isPropertyExportable(Property property) {
    return !exceptColumnNameList.contains(property.getName())
        && ((StringUtils.isEmpty(property.getMappedBy()))
            || (!StringUtils.isEmpty(property.getMappedBy())
                && (property.getTarget() != null
                    && property
                        .getTarget()
                        .getPackage()
                        .equals(Package.getPackage("com.axelor.meta.db"))
                    && !property.getTarget().isAssignableFrom(MetaFile.class)
                    && !property.getTarget().isAssignableFrom(MetaJsonField.class))))
        && !property.isTransient();
  }

  /* Get Header For csv File */
  protected String getMetaModelHeader(
      Property property, CSVInput csvInput, boolean isRelativeDate) {
    String propertyTypeStr = property.getType().toString();
    String propertyName = property.getName();
    switch (propertyTypeStr) {
      case "DATE":
      case "DATETIME":
        if (isRelativeDate) {
          return getDateOrDateTimeHeader(property, csvInput);
        }
        return propertyName;
      case "LONG":
        return propertyName.equalsIgnoreCase("id") ? "importId" : propertyName;
      case "BINARY":
        byteArrFieldFlag = true;
        return "byte_" + propertyName;
      case "ONE_TO_ONE":
      case "MANY_TO_ONE":
        return getRelationalFieldHeader(property, csvInput, "ONE");
      case "ONE_TO_MANY":
      case "MANY_TO_MANY":
        return getRelationalFieldHeader(property, csvInput, "MANY");
      default:
        return propertyName;
    }
  }

  protected String getDateOrDateTimeHeader(Property property, CSVInput csvInput) {
    String propertyName = property.getName();
    CSVBind csvBind = new CSVBind();
    csvBind.setField(propertyName);
    csvBind.setColumn(propertyName);
    if (property.getType().toString().equals("DATE")) {
      csvBind.setExpression(
          "call:com.axelor.csv.script.ImportDateTime:importDate(" + propertyName + ")");

    } else {
      csvBind.setExpression(
          "call:com.axelor.csv.script.ImportDateTime:importDateTime(" + propertyName + ")");
    }
    csvInput.getBindings().add(csvBind);
    return propertyName;
  }

  protected String getRelationalFieldHeader(
      Property property, CSVInput csvInput, String relationship) {
    csvInput.setSearch("self.importId = :importId");
    CSVBind csvBind = new CSVBind();
    String columnName = property.getName() + "_importId";
    String search =
        relationship.equalsIgnoreCase("ONE")
            ? "self.importId = :" + columnName
            : "self.importId in :" + columnName;
    if (property.getTarget() != null
        && property.getTarget().getPackage().equals(Package.getPackage("com.axelor.meta.db"))
        && !property.getTarget().getTypeName().equals("com.axelor.meta.db.MetaFile")) {
      columnName = property.getName() + "_name";
      search =
          relationship.equalsIgnoreCase("ONE")
              ? "self.name = :" + columnName
              : "self.name in :" + columnName;
    }
    csvBind.setColumn(columnName);
    csvBind.setField(property.getName());
    csvBind.setSearch(search);
    csvBind.setUpdate(true);
    if (relationship.equalsIgnoreCase("MANY")) {
      csvBind.setExpression(columnName + ".split('\\\\|') as List");
    }
    csvInput.getBindings().add(csvBind);
    referenceFlag = true;
    if (property.isRequired()) {
      notNullReferenceFlag = true;
    }
    return columnName;
  }

  /* Get Data For csv File */
  protected String getMetaModelData(
      String metaModelName,
      Mapper metaModelMapper,
      Property property,
      Object dataObject,
      String dirPath,
      boolean isRelativeDate,
      boolean updateImportId,
      DataBackup dataBackup)
      throws AxelorException {
    String id = metaModelMapper.get(dataObject, "id").toString();
    Object value = metaModelMapper.get(dataObject, property.getName());
    List<AnonymizerLine> anonymizerLines;

    if (value == null) {
      return "";
    }
    String propertyTypeStr = property.getType().toString();

    if (dataBackup.getAnonymizer() != null) {
      anonymizerLines =
          dataBackupAnonymizeService.searchAnonymizerLines(dataBackup, property, metaModelName);
      if (anonymizerLines != null && !anonymizerLines.isEmpty()) {
        return dataBackupAnonymizeService.anonymizeMetaModelData(
            property, metaModelName, value, anonymizerLines);
      }
    }

    switch (propertyTypeStr) {
      case "LONG":
        if (updateImportId) {
          return ((Model) dataObject).getImportId();
        }
        return value.toString();
      case "DATE":
        if (isRelativeDate) {
          return createRelativeDate((LocalDate) value);
        }
        return value.toString();

      case "DATETIME":
        if (isRelativeDate) {
          if (property.getJavaType() == ZonedDateTime.class) {
            return createRelativeDateTime(((ZonedDateTime) value).toLocalDateTime());
          }
          return createRelativeDateTime((LocalDateTime) value);
        }
        return property.getJavaType() == ZonedDateTime.class
            ? ((ZonedDateTime) value).toLocalDateTime().toString()
            : value.toString();

      case "BINARY":
        String fileName = metaModelName + "_" + property.getName() + "_" + id + ".png";

        try {
          org.apache.commons.io.FileUtils.writeByteArrayToFile(
              new File(dirPath, fileName), (byte[]) value);
          fileNameList.add(fileName);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return fileName;
      case "ONE_TO_ONE":
      case "MANY_TO_ONE":
        return getRelationalFieldValue(property, value, updateImportId);
      case "ONE_TO_MANY":
      case "MANY_TO_MANY":
        return getRelationalFieldData(property, value, updateImportId);
      default:
        return value.toString();
    }
  }

  public String createRelativeDateTime(LocalDateTime dateT) {
    LocalDateTime currentDateTime = LocalDateTime.now();

    long years = currentDateTime.until(dateT, ChronoUnit.YEARS);
    currentDateTime = currentDateTime.plusYears(years);

    long months = currentDateTime.until(dateT, ChronoUnit.MONTHS);
    currentDateTime = currentDateTime.plusMonths(months);

    long days = currentDateTime.until(dateT, ChronoUnit.DAYS);
    currentDateTime = currentDateTime.plusDays(days);

    long hours = currentDateTime.until(dateT, ChronoUnit.HOURS);
    currentDateTime = currentDateTime.plusHours(hours);

    long minutes = currentDateTime.until(dateT, ChronoUnit.MINUTES);
    currentDateTime = currentDateTime.plusMinutes(minutes);

    long seconds = currentDateTime.until(dateT, ChronoUnit.SECONDS);
    if (seconds < 0 || minutes < 0 || hours < 0 || days < 0 || months < 0 || years < 0) {
      return "NOW["
          + ((years == 0) ? "" : (years + "y"))
          + ((months == 0) ? "" : (months + "M"))
          + ((days == 0) ? "" : (days + "d"))
          + ((hours == 0) ? "" : (hours + "H"))
          + ((minutes == 0) ? "" : (minutes + "m"))
          + ((seconds == 0) ? "" : (seconds + "s"))
          + "]";
    }
    return "NOW["
        + ((years == 0) ? "" : ("+" + years + "y"))
        + ((months == 0) ? "" : ("+" + months + "M"))
        + ((days == 0) ? "" : ("+" + days + "d"))
        + ((hours == 0) ? "" : ("+" + hours + "H"))
        + ((minutes == 0) ? "" : ("+" + minutes + "m"))
        + ((seconds == 0) ? "" : ("+" + seconds + "s"))
        + "]";
  }

  public String createRelativeDate(LocalDate date) {
    LocalDate currentDate = DateTool.getTodayDate(null);
    long years = currentDate.until(date, ChronoUnit.YEARS);
    currentDate = currentDate.plusYears(years);

    long months = currentDate.until(date, ChronoUnit.MONTHS);
    currentDate = currentDate.plusMonths(months);

    long days = currentDate.until(date, ChronoUnit.DAYS);

    if (days < 0 || months < 0 || years < 0) {
      return "TODAY["
          + ((years == 0) ? "" : years + "y")
          + ((months == 0) ? "" : months + "M")
          + ((days == 0) ? "" : days + "d")
          + "]";
    } else if (days == 0 && months == 0 && years == 0) {
      return "TODAY";
    }
    return "TODAY["
        + ((years == 0) ? "" : ("+" + years + "y"))
        + ((months == 0) ? "" : ("+" + months + "M"))
        + ((days == 0) ? "" : ("+" + days + "d"))
        + "]";
  }

  public String getRelationalFieldData(Property property, Object value, boolean updateImportId) {
    StringBuilder idStringBuilder = new StringBuilder();
    Collection<?> valueList = (Collection<?>) value;
    String referenceData;
    for (Object val : valueList) {
      referenceData = getRelationalFieldValue(property, val, updateImportId);
      if (StringUtils.notBlank(referenceData)) {
        idStringBuilder.append(referenceData).append(REFERENCE_FIELD_SEPARATOR);
      }
    }

    if (StringUtils.notBlank(idStringBuilder)) {
      idStringBuilder.setLength(idStringBuilder.length() - 1);
    }
    return idStringBuilder.toString();
  }

  protected String getRelationalFieldValue(Property property, Object val, boolean updateImportId) {
    if (property.getTarget() != null
        && property.getTarget().getPackage().equals(Package.getPackage("com.axelor.meta.db"))
        && !property.getTarget().getTypeName().equals("com.axelor.meta.db.MetaFile")) {
      try {
        return Mapper.of(val.getClass()).get(val, "name").toString();
      } catch (Exception e) {
        return (updateImportId) ? ((Model) val).getImportId() : ((Model) val).getId().toString();
      }
    } else {
      return (updateImportId) ? ((Model) val).getImportId() : ((Model) val).getId().toString();
    }
  }

  protected File generateZIP(String dirPath, List<String> fileNameList) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String backupZipFileName = "DataBackup_" + LocalDateTime.now().format(formatter) + ".zip";
    File zipFile = new File(dirPath, backupZipFileName);
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {

      for (String fileName : fileNameList) {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);
        File file = new File(dirPath, fileName);
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file))) {
          byte[] data;
          while (bin.available() > 0) {
            if (bin.available() < BUFFER_SIZE) {
              data = new byte[bin.available()];
            } else {
              data = new byte[BUFFER_SIZE];
            }
            bin.read(data, 0, data.length);
            out.write(data, 0, data.length);
          }
          out.closeEntry();

          file.delete();
        }
      }
    } catch (IOException e) {
      TraceBackService.trace(e, "Error From DataBackupCreateService - generateZIP()");
    }

    return zipFile;
  }

  /* Generate XML File from CSVConfig */
  protected void generateConfig(String dirPath, CSVConfig csvConfig) {

    File file = new File(dirPath, DataBackupServiceImpl.CONFIG_FILE_NAME);
    try (FileWriter fileWriter = new FileWriter(file, true)) {

      XStream xStream = new XStream();
      xStream.processAnnotations(CSVConfig.class);
      xStream.setMode(XStream.NO_REFERENCES);
      fileWriter.append(xStream.toXML(csvConfig));

    } catch (IOException e) {
      TraceBackService.trace(e, "Error From DataBackupCreateService - generateConfig()");
    }
  }

  protected int checkErrors(
      DataBackup dataBackup,
      List<MetaModel> metaModelList,
      String tempDirectoryPath,
      Map<String, List<String>> subClassesMap) {
    int errorsCount = 0;

    for (MetaModel metaModel : metaModelList) {

      try {
        List<String> subClasses = subClassesMap.get(metaModel.getFullName());
        long totalRecord = getMetaModelDataCount(metaModel, subClasses);
        if (totalRecord > 0) {
          LOG.debug("Checking Model : " + metaModel.getFullName());

          File templateFile = new File(tempDirectoryPath, metaModel.getName() + ".csv");
          CSVFile csvFormat =
              CSVFile.DEFAULT.withDelimiter(SEPARATOR).withQuoteAll().withFirstRecordAsHeader();
          CSVPrinter printer = csvFormat.write(templateFile);

          writeCSVData(
              metaModel,
              printer,
              dataBackup,
              1,
              subClasses,
              tempDirectoryPath,
              anonymizeService.getSalt());
          printer.close();
        }
      } catch (ClassNotFoundException e) {
      } catch (IOException e) {
        TraceBackService.trace(e, DataBackupService.class.getName());
      } catch (Exception e) {
        JPA.em().getTransaction().rollback();
        sb.append("\nError occured while processing model : " + metaModel.getFullName() + "\n");
        sb.append(e.getMessage() + "\n");
        JPA.em().getTransaction().begin();
        dataBackup = dataBackupRepository.find(dataBackup.getId());
        dataBackup.setFetchLimit(1);
        errorsCount++;
      }
    }
    return errorsCount;
  }
}
