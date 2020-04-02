/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaField;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupCreateService {

  private static final char SEPARATOR = ',';
  private static final char QUOTE_CHAR = '"';
  private static final char REFERENCE_FIELD_SEPARATOR = '|';

  @Inject private MetaModelRepository metaModelRepo;

  private Logger LOG = LoggerFactory.getLogger(getClass());

  private boolean notNullReferenceFlag, referenceFlag;

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
          "attr");

  private static Map<Object, Object> AutoImportModelMap =
      ImmutableMap.builder()
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

  /* Generate csv Files for each individual MetaModel and single config file */
  public File create(Integer fetchLimit) {
    File tempDir = Files.createTempDir();
    String tempDirectoryPath = tempDir.getAbsolutePath();

    List<String> fileNameList = new ArrayList<>();
    List<MetaModel> metaModelList = getMetaModels();

    LinkedList<CSVInput> simpleCsvs = new LinkedList<>();
    LinkedList<CSVInput> refernceCsvs = new LinkedList<>();
    LinkedList<CSVInput> notNullReferenceCsvs = new LinkedList<>();

    for (MetaModel metaModel : metaModelList) {
      try {
        if (Class.forName(metaModel.getFullName())
            .getSuperclass()
            .isAssignableFrom(AuditableModel.class)) {
          long totalRecord = getMetaModelDataCount(metaModel);
          if (totalRecord > 0) {
            LOG.debug("Exporting Model : " + metaModel.getFullName());

            notNullReferenceFlag = false;
            referenceFlag = false;

            CSVWriter csvWriter =
                new CSVWriter(
                    new FileWriter(new File(tempDirectoryPath, metaModel.getName() + ".csv")),
                    SEPARATOR,
                    QUOTE_CHAR);
            CSVInput csvInput = writeCSVData(metaModel, csvWriter, fetchLimit, totalRecord);
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
              simpleCsvs.add(temcsv);
            } else {
              simpleCsvs.add(csvInput);
            }

            fileNameList.add(metaModel.getName() + ".csv");
          }
        }
      } catch (ClassNotFoundException e) {
      } catch (IOException e) {
        TraceBackService.trace(e);
      }
    }

    CSVConfig csvConfig = new CSVConfig();
    csvConfig.setInputs(simpleCsvs);
    csvConfig.getInputs().addAll(notNullReferenceCsvs);
    csvConfig.getInputs().addAll(refernceCsvs);
    csvConfig.getInputs().addAll(notNullReferenceCsvs);
    generateConfig(tempDirectoryPath, csvConfig);

    fileNameList.add(DataBackupServiceImpl.configFileName);
    File zippedFile = generateZIP(tempDirectoryPath, fileNameList);
    LOG.debug("Data Backup Completed");
    return zippedFile;
  }

  /* Get All MetaModels */
  private List<MetaModel> getMetaModels() {
    String filterStr =
        "self.packageName NOT LIKE '%meta%' AND self.packageName !='com.axelor.studio.db' AND self.name!='DataBackup' AND self.tableName NOT LIKE 'BASE_APP%'";
    List<MetaModel> metaModels = metaModelRepo.all().filter(filterStr).fetch();
    metaModels.add(metaModelRepo.findByName(MetaFile.class.getSimpleName()));
    metaModels.add(metaModelRepo.findByName(MetaJsonField.class.getSimpleName()));
    return metaModels;
  }

  /* Get All Data of Specific MetaModel */
  private List<Model> getMetaModelDataList(MetaModel metaModel, int start, Integer fetchLimit) {
    List<Model> metaModelDataList = new ArrayList<>();
    try {
      metaModelDataList =
          JPA.em()
              .createQuery("FROM " + metaModel.getName(), Model.class)
              .setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE)
              .setFirstResult(start)
              .setMaxResults(fetchLimit)
              .getResultList();
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return metaModelDataList;
  }

  private long getMetaModelDataCount(MetaModel metaModel) {
    return (long)
        JPA.em().createQuery("SELECT count(*) FROM " + metaModel.getName()).getSingleResult();
  }

  private CSVInput writeCSVData(
      MetaModel metaModel, CSVWriter csvWriter, Integer fetchLimit, long totalRecord) {
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

        dataList = getMetaModelDataList(metaModel, i, fetchLimit);

        if (dataList != null && dataList.size() > 0) {
          for (Object dataObject : dataList) {
            dataArr = new ArrayList<>();

            for (Property property : pro) {
              if (StringUtils.isEmpty(property.getMappedBy())
                  && !exceptColumnNameList.contains(property.getName())) {
                if (headerFlag) {
                  headerArr.add(getMetaModelHeader(dataObject, property, csvInput));
                }
                dataArr.add(
                    getMetaModelData(
                        property, metaModelMapper.get(dataObject, property.getName())));
              }
            }

            if (headerFlag) {
              csvWriter.writeNext(headerArr.toArray(new String[headerArr.size()]), true);
              headerFlag = false;
            }
            csvWriter.writeNext(dataArr.toArray(new String[dataArr.size()]), true);
          }
        }
      }
      if (AutoImportModelMap.containsKey(csvInput.getTypeName())) {
        csvInput.setSearch(AutoImportModelMap.get(csvInput.getTypeName()).toString());
      }
    } catch (ClassNotFoundException e) {
    }
    return csvInput;
  }

  /* Get Header For csv File */
  private String getMetaModelHeader(Object value, Property property, CSVInput csvInput) {
    String propertyTypeStr = property.getType().toString();
    String propertyName = property.getName();
    switch (propertyTypeStr) {
      case "LONG":
        return propertyName.equalsIgnoreCase("id") ? "importId" : propertyName;
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
    if (property.getTarget().isAssignableFrom(MetaModel.class)
        || property.getTarget().isAssignableFrom(MetaField.class)) {
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
  private String getMetaModelData(Property property, Object value) {
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
        return Arrays.toString((byte[]) value);
      case "ONE_TO_ONE":
      case "MANY_TO_ONE":
        return getRelationFieldValue(property, value);
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
      referenceData = getRelationFieldValue(property, val);
      if (StringUtils.notBlank(referenceData)) {
        idStringBuilder.append(referenceData + REFERENCE_FIELD_SEPARATOR);
      }
    }

    if (StringUtils.notBlank(idStringBuilder)) {
      idStringBuilder.setLength(idStringBuilder.length() - 1);
    }
    return idStringBuilder.toString();
  }

  private String getRelationFieldValue(Property property, Object val) {
    if (property.getTarget().isAssignableFrom(MetaModel.class)) {
      return ((MetaModel) val).getName().toString();
    } else if (property.getTarget().isAssignableFrom(MetaField.class)) {
      return ((MetaField) val).getName().toString();
    } else {
      return ((Model) val).getId().toString();
    }
  }

  private File generateZIP(String dirPath, List<String> fileNameList) {
    int length = 0;
    byte[] data = null;

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
        length = (int) file.length();
        data = new byte[length];
        data = FileUtils.readFileToByteArray(file);
        out.write(data, 0, data.length);
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
      File file = new File(dirPath, DataBackupServiceImpl.configFileName);
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
