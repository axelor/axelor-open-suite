/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.App;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupCreateService {

  private static final char SEPARATOR = ',';
  private static final char QUOTE_CHAR = '"';
  private static final char REFERENCE_FIELD_SEPARATOR = '|';

  private static final int BUFFER_SIZE = 1000;

  @Inject private MetaModelRepository metaModelRepo;

  private Logger LOG = LoggerFactory.getLogger(getClass());

  private boolean notNullReferenceFlag, referenceFlag;
  private boolean byteArrFieldFlag = false;

  private static Set<String> exceptColumnNameList =
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

  private static Map<Object, Object> AutoImportModelMap =
      ImmutableMap.builder()
          .put("com.axelor.apps.base.db.App", "self.code = :code")
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

  List<String> fileNameList;

  /* Generate csv Files for each individual MetaModel and single config file */
  public File create(Integer fetchLimit) throws InterruptedException {
    File tempDir = Files.createTempDir();
    String tempDirectoryPath = tempDir.getAbsolutePath();

    fileNameList = new ArrayList<>();
    List<MetaModel> metaModelList = getMetaModels();

    LinkedList<CSVInput> simpleCsvs = new LinkedList<>();
    LinkedList<CSVInput> refernceCsvs = new LinkedList<>();
    LinkedList<CSVInput> notNullReferenceCsvs = new LinkedList<>();
    Map<String, List<String>> subClassesMap = getSubClassesMap();

    for (MetaModel metaModel : metaModelList) {
      try {
        List<String> subClasses = subClassesMap.get(metaModel.getFullName());
        long totalRecord = getMetaModelDataCount(metaModel, subClasses);
        if (totalRecord > 0) {
          LOG.debug("Exporting Model : " + metaModel.getFullName());

          notNullReferenceFlag = false;
          referenceFlag = false;

          CSVWriter csvWriter =
              new CSVWriter(
                  new FileWriter(new File(tempDirectoryPath, metaModel.getName() + ".csv")),
                  SEPARATOR,
                  QUOTE_CHAR);
          CSVInput csvInput =
              writeCSVData(
                  metaModel, csvWriter, fetchLimit, totalRecord, subClasses, tempDirectoryPath);
          csvWriter.close();

          if (notNullReferenceFlag) {
            notNullReferenceCsvs.add(csvInput);
          } else if (referenceFlag) {
            refernceCsvs.add(csvInput);

            CSVInput temcsv = new CSVInput();
            temcsv.setFileName(csvInput.getFileName());
            temcsv.setTypeName(csvInput.getTypeName());
            if (AutoImportModelMap.containsKey(csvInput.getTypeName())) {
              temcsv.setSearch(AutoImportModelMap.get(csvInput.getTypeName()).toString());
            }
            if (Class.forName(metaModel.getFullName()).getSuperclass() == App.class) {
              temcsv.setSearch("self.code = :code");
            }
            simpleCsvs.add(temcsv);
          } else {
            simpleCsvs.add(csvInput);
          }

          fileNameList.add(metaModel.getName() + ".csv");
        }
      } catch (ClassNotFoundException e) {
      } catch (IOException e) {
        TraceBackService.trace(e, DataBackupService.class.getName());
      }
    }

    CSVConfig csvConfig = new CSVConfig();
    csvConfig.setInputs(simpleCsvs);
    csvConfig.getInputs().addAll(notNullReferenceCsvs);
    csvConfig.getInputs().addAll(refernceCsvs);
    csvConfig.getInputs().addAll(notNullReferenceCsvs);
    generateConfig(tempDirectoryPath, csvConfig);

    fileNameList.add(DataBackupServiceImpl.CONFIG_FILE_NAME);
    File zippedFile = generateZIP(tempDirectoryPath, fileNameList);
    return zippedFile;
  }

  /* Get All MetaModels */
  private List<MetaModel> getMetaModels() {
    String filterStr =
        "self.packageName NOT LIKE '%meta%' AND self.packageName !='com.axelor.studio.db' AND self.name!='DataBackup'";
    List<MetaModel> metaModels = metaModelRepo.all().filter(filterStr).order("fullName").fetch();
    metaModels.add(metaModelRepo.findByName(MetaFile.class.getSimpleName()));
    metaModels.add(metaModelRepo.findByName(MetaJsonField.class.getSimpleName()));
    return metaModels;
  }

  private Map<String, List<String>> getSubClassesMap() {
    List<MetaModel> metaModels = getMetaModels();
    List<String> subClasses;
    Map<String, List<String>> subClassMap = new HashMap<String, List<String>>();
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
        e.printStackTrace();
      }
    }
    return subClassMap;
  }

  /* Get All Data of Specific MetaModel */
  private List<Model> getMetaModelDataList(
      MetaModel metaModel, int start, Integer fetchLimit, List<String> subClasses)
      throws ClassNotFoundException {
    return getQuery(metaModel, subClasses).fetch(fetchLimit, start);
  }

  private long getMetaModelDataCount(MetaModel metaModel, List<String> subClasses)
      throws InterruptedException, ClassNotFoundException {
    Query<Model> query = getQuery(metaModel, subClasses);
    long count = 0;
    if (query != null) {
      count = query.count();
    }
    return count;
  }

  private Query<Model> getQuery(MetaModel metaModel, List<String> subClasses)
      throws ClassNotFoundException {
    String whereStr = "";
    if (subClasses != null && subClasses.size() > 0) {
      for (String subClassName : subClasses) {
        whereStr += whereStr.length() > 0 ? " AND " : "";
        whereStr += "id NOT IN (select id from " + subClassName + ")";
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
      if (StringUtils.notEmpty(whereStr)) {
        query.filter(whereStr);
      }
    }
    return query;
  }

  private CSVInput writeCSVData(
      MetaModel metaModel,
      CSVWriter csvWriter,
      Integer fetchLimit,
      long totalRecord,
      List<String> subClasses,
      String dirPath) {
    CSVInput csvInput = new CSVInput();
    boolean headerFlag = true;
    List<String> dataArr = null;
    List<String> headerArr = new ArrayList<>();
    List<Model> dataList = null;

    try {
      Mapper metaModelMapper = Mapper.of(Class.forName(metaModel.getFullName()));
      Property[] pro = metaModelMapper.getProperties();

      csvInput.setFileName(metaModel.getName() + ".csv");
      csvInput.setTypeName(metaModel.getFullName());
      csvInput.setBindings(new ArrayList<>());

      for (int i = 0; i < totalRecord; i = i + fetchLimit) {

        dataList = getMetaModelDataList(metaModel, i, fetchLimit, subClasses);

        if (dataList != null && dataList.size() > 0) {
          for (Object dataObject : dataList) {
            dataArr = new ArrayList<>();

            for (Property property : pro) {
              if (isPropertyExportable(property)) {
                if (headerFlag) {
                  String headerStr = getMetaModelHeader(dataObject, property, csvInput);
                  headerArr.add(headerStr);
                }
                dataArr.add(
                    getMetaModelData(
                        metaModel.getName(),
                        metaModelMapper.get(dataObject, "id").toString(),
                        property,
                        metaModelMapper.get(dataObject, property.getName()),
                        dirPath));
              }
            }

            if (headerFlag) {
              if (byteArrFieldFlag) {
                csvInput.setCallable(
                    "com.axelor.apps.base.service.app.DataBackupRestoreService:importObjectWithByteArray");
                byteArrFieldFlag = false;
              }
              csvWriter.writeNext(headerArr.toArray(new String[headerArr.size()]), true);
              headerFlag = false;
            }
            csvWriter.writeNext(dataArr.toArray(new String[dataArr.size()]), true);
          }
        }
      }
      if (AutoImportModelMap.containsKey(csvInput.getTypeName())) {
        csvInput.setSearch(AutoImportModelMap.get(csvInput.getTypeName()).toString());
      } else if (Class.forName(metaModel.getFullName()).getSuperclass() == App.class) {
        csvInput.setSearch("self.code = :code");
      }
    } catch (ClassNotFoundException e) {
    }
    return csvInput;
  }

  private boolean isPropertyExportable(Property property) {
    if (!exceptColumnNameList.contains(property.getName())
        && ((StringUtils.isEmpty(property.getMappedBy()))
            || (!StringUtils.isEmpty(property.getMappedBy())
                && (property.getTarget() != null
                    && property
                        .getTarget()
                        .getPackage()
                        .equals(Package.getPackage("com.axelor.meta.db"))
                    && !property.getTarget().isAssignableFrom(MetaFile.class)
                    && !property.getTarget().isAssignableFrom(MetaJsonField.class))))) {
      return true;
    }
    return false;
  }

  /* Get Header For csv File */
  private String getMetaModelHeader(Object value, Property property, CSVInput csvInput) {
    String propertyTypeStr = property.getType().toString();
    String propertyName = property.getName();
    switch (propertyTypeStr) {
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

  private String getRelationalFieldHeader(
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
  private String getMetaModelData(
      String metaModelName, String id, Property property, Object value, String dirPath) {
    if (value == null) {
      return "";
    }

    String propertyTypeStr = property.getType().toString();
    switch (propertyTypeStr) {
      case "DATETIME":
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
        return getRelationalFieldValue(property, value);
      case "ONE_TO_MANY":
      case "MANY_TO_MANY":
        return getRelationalFieldData(property, value);
      default:
        return value.toString();
    }
  }

  public String getRelationalFieldData(Property property, Object value) {
    StringBuilder idStringBuilder = new StringBuilder();
    Collection<?> valueList = (Collection<?>) value;
    String referenceData = "";
    for (Object val : valueList) {
      referenceData = getRelationalFieldValue(property, val);
      if (StringUtils.notBlank(referenceData)) {
        idStringBuilder.append(referenceData + REFERENCE_FIELD_SEPARATOR);
      }
    }

    if (StringUtils.notBlank(idStringBuilder)) {
      idStringBuilder.setLength(idStringBuilder.length() - 1);
    }
    return idStringBuilder.toString();
  }

  private String getRelationalFieldValue(Property property, Object val) {
    if (property.getTarget() != null
        && property.getTarget().getPackage().equals(Package.getPackage("com.axelor.meta.db"))
        && !property.getTarget().getTypeName().equals("com.axelor.meta.db.MetaFile")) {
      try {
        return Mapper.of(val.getClass()).get(val, "name").toString();
      } catch (Exception e) {
        return ((Model) val).getId().toString();
      }
    } else {
      return ((Model) val).getId().toString();
    }
  }

  private File generateZIP(String dirPath, List<String> fileNameList) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmSS");
    String backupZipFileName = "DataBackup_" + LocalDateTime.now().format(formatter) + ".zip";
    File zipFile = new File(dirPath, backupZipFileName);
    try {
      ZipOutputStream out = null;
      out = new ZipOutputStream(new FileOutputStream(zipFile));
      for (String fileName : fileNameList) {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);
        File file = new File(dirPath, fileName);
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
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
        bin.close();
        out.closeEntry();
        file.delete();
      }
      out.close();
    } catch (IOException e) {
      TraceBackService.trace(e, "Error From DataBackupCreateService - generateZIP()");
    }
    return zipFile;
  }

  /* Generate XML File from CSVConfig */
  private void generateConfig(String dirPath, CSVConfig csvConfig) {
    try {
      File file = new File(dirPath, DataBackupServiceImpl.CONFIG_FILE_NAME);
      FileWriter fileWriter = new FileWriter(file, true);

      XStream xStream = new XStream();
      xStream.processAnnotations(CSVConfig.class);
      xStream.setMode(XStream.NO_REFERENCES);
      fileWriter.append(xStream.toXML(csvConfig));

      fileWriter.close();
    } catch (IOException e) {
      TraceBackService.trace(e, "Error From DataBackupCreateService - generateConfig()");
    }
  }
}
