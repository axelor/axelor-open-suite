/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.tool.NammingTool;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.itextpdf.text.DocumentException;
import com.mysql.jdbc.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedExportServiceImpl implements AdvancedExportService {

  private final Logger log = LoggerFactory.getLogger(AdvancedExportServiceImpl.class);

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaSelectRepository metaSelectRepo;

  @Inject private MetaFiles metaFiles;

  private LinkedHashSet<String> joinFieldSet = new LinkedHashSet<>(),
      selectionJoinFieldSet = new LinkedHashSet<>(),
      selectionRelationalJoinFieldSet = new LinkedHashSet<>();

  private List<Object> params = null;

  private String language = "", selectNormalField = "", selectSelectionField = "", aliasName = "";

  private boolean isSelectionField = false;

  private int firstLevel = 0, secondLevel = 0, thirdLevel = 0, nbrField = 0, msi = 0, mt = 0;

  /**
   * This method create the target field using <b>Dot(.)</b> if field is relational.
   *
   * <p>e.g. Company.currency.code
   */
  @Override
  public String getTargetField(Context context, MetaField metaField, MetaModel parentMetaModel) {
    String targetField = context.get("targetField").toString();
    String[] splitField = targetField.split("\\.");
    MetaField metaField2 = checkLastMetaField(splitField, 0, parentMetaModel, metaField);

    if (metaField2.getRelationship() != null) {
      targetField += "." + metaField.getName();
      return targetField;
    } else {
      return targetField.replace(splitField[splitField.length - 1], metaField.getName());
    }
  }

  private MetaField checkLastMetaField(
      String[] splitField, int index, MetaModel parentMetaModel, MetaField metaField) {

    MetaModel metaModel = parentMetaModel;
    MetaField metaField3 = metaField;
    if (index <= splitField.length - 1) {
      metaField3 =
          metaFieldRepo
              .all()
              .filter("self.name = ?1 and self.metaModel = ?2", splitField[index], parentMetaModel)
              .fetchOne();
      if (!splitField[0].equals(splitField[splitField.length - 1])) {
        if (metaField3.getRelationship() != null) {
          metaModel = metaModelRepo.findByName(metaField3.getTypeName());
        }
      }
      metaField3 = checkLastMetaField(splitField, index + 1, metaModel, metaField3);
    }
    return metaField3;
  }

  /**
   * This method split and join the all fields/columns which are selected by user and create the
   * query.
   *
   * @param advancedExport
   * @param criteria
   * @return
   * @throws ClassNotFoundException
   * @throws DocumentException
   * @throws IOException
   * @throws AxelorException
   */
  private Query getAdvancedExportQuery(AdvancedExport advancedExport, String criteria)
      throws ClassNotFoundException, DocumentException, IOException, AxelorException {

    StringBuilder selectFieldBuilder = new StringBuilder(),
        selectJoinFieldBuilder = new StringBuilder(),
        selectionFieldBuilder = new StringBuilder(),
        orderByFieldBuilder = new StringBuilder();

    joinFieldSet.clear();
    selectionJoinFieldSet.clear();
    selectionRelationalJoinFieldSet.clear();
    firstLevel = 0;
    nbrField = 0;
    int col = 0;
    language = AuthUtils.getUser().getLanguage();

    MetaModel metaModel = advancedExport.getMetaModel();
    List<AdvancedExportLine> advancedExportLineList =
        sortAdvancedExportLineList(advancedExport.getAdvancedExportLineList());

    for (AdvancedExportLine advancedExportLine : advancedExportLineList) {
      String[] splitField = advancedExportLine.getTargetField().split("\\.");

      createQueryParts(splitField, 0, metaModel);

      if (!joinFieldSet.isEmpty()
          && (!selectNormalField.equals("") || selectNormalField.indexOf(0) == '.')) {
        selectJoinFieldBuilder.append(
            aliasName + selectNormalField + " AS " + ("Col_" + (col)) + ",");
      }
      if (!selectSelectionField.equals("")
          && (!selectionJoinFieldSet.isEmpty() || !selectionRelationalJoinFieldSet.isEmpty())) {
        selectionFieldBuilder.append(selectSelectionField + " AS " + ("Col_" + (col)) + ",");

      } else if (firstLevel == 0
          && selectSelectionField.equals("")
          && !selectNormalField.equals("")) {
        selectFieldBuilder.append(
            "self." + advancedExportLine.getTargetField() + " AS " + ("Col_" + (col)) + ",");
      }
      if (advancedExportLine.getOrderBy()) {
        orderByFieldBuilder.append("Col_" + col + ",");
      }
      selectNormalField = "";
      selectSelectionField = "";
      aliasName = "";
      secondLevel = 0;
      thirdLevel = 0;
      nbrField++;
      col++;
    }
    return createQuery(
        createQueryBuilder(
            metaModel,
            selectFieldBuilder,
            selectJoinFieldBuilder,
            selectionFieldBuilder,
            criteria,
            orderByFieldBuilder));
  }

  /**
   * This method create query parts based on field.
   *
   * @param splitField
   * @param parentIndex
   * @param metaModel
   * @throws ClassNotFoundException
   */
  private void createQueryParts(String[] splitField, int parentIndex, MetaModel metaModel)
      throws ClassNotFoundException {

    while (parentIndex <= splitField.length - 1) {
      MetaField relationalField =
          metaFieldRepo
              .all()
              .filter("self.name = ?1 and self.metaModel = ?2", splitField[parentIndex], metaModel)
              .fetchOne();
      MetaModel subMetaModel =
          metaModelRepo.all().filter("self.name = ?1", relationalField.getTypeName()).fetchOne();

      if (relationalField.getRelationship() != null) {
        checkRelationalField(splitField, parentIndex);
      } else {
        checkSelectionField(splitField, parentIndex, metaModel);
        checkNormalField(splitField, parentIndex);
      }
      parentIndex += 1;
      metaModel = subMetaModel;
    }
  }

  private void checkRelationalField(String[] splitField, int parentIndex) {
    String tempAliasName = "";
    firstLevel++;
    if (secondLevel != 0 || thirdLevel != 0) {
      selectNormalField = "";
    }
    if (parentIndex != 0) {
      tempAliasName = isKeyword(splitField, 0);
      aliasName = tempAliasName;

      for (int subIndex = 1; subIndex <= parentIndex; subIndex++) {
        tempAliasName = isKeyword(splitField, subIndex);
        if (!aliasName.equals(splitField[parentIndex])) {
          joinFieldSet.add(
              "LEFT JOIN " + aliasName + "." + splitField[subIndex] + " " + tempAliasName);
          aliasName = tempAliasName;
        }
      }
    } else {
      tempAliasName = isKeyword(splitField, parentIndex);
      if (nbrField > 0
          && !joinFieldSet.isEmpty()
          && !joinFieldSet.contains("self." + splitField[parentIndex] + " " + tempAliasName)) {
        joinFieldSet.add("LEFT JOIN self." + splitField[parentIndex] + " " + tempAliasName);
      } else {
        joinFieldSet.add("self." + splitField[parentIndex] + " " + tempAliasName);
      }
      aliasName = tempAliasName;
    }
  }

  private String isKeyword(String[] fieldNames, int ind) {
    if (NammingTool.isKeyword(fieldNames[ind])) {
      return fieldNames[ind] + "_id";
    }
    return fieldNames[ind];
  }

  private void checkSelectionField(String[] fieldName, int index, MetaModel metaModel)
      throws ClassNotFoundException {

    Class<?> klass = Class.forName(metaModel.getFullName());
    Mapper mapper = Mapper.of(klass);
    MetaSelect metaSelect =
        metaSelectRepo.findByName(mapper.getProperty(fieldName[index]).getSelection());

    if (metaSelect != null) {
      isSelectionField = true;
      msi++;
      mt++;
      if (firstLevel > 0 && index != 0) {
        checkRelationalSelectionField(fieldName, index, metaSelect);
      } else {
        checkJoinSelectionField(fieldName, index, metaSelect);
      }
    }
  }

  private void checkRelationalSelectionField(String[] fieldName, int index, MetaSelect metaSelect) {
    if (language.equals(LANGUAGE_FR)) {
      selectionRelationalJoinFieldSet.add(
          "LEFT JOIN "
              + "MetaSelectItem "
              + ("msi_" + (msi))
              + " ON CAST("
              + aliasName
              + "."
              + fieldName[index]
              + " AS text) = "
              + ("msi_" + (msi))
              + ".value AND "
              + ("msi_" + (msi))
              + ".select = "
              + metaSelect.getId()
              + " LEFT JOIN "
              + "MetaTranslation "
              + ("mt_" + (mt))
              + " ON "
              + ("msi_" + (msi))
              + ".title = "
              + ("mt_" + (mt))
              + ".key AND "
              + ("mt_" + (mt))
              + ".language = \'"
              + language
              + "\'");
    } else {
      selectionRelationalJoinFieldSet.add(
          "LEFT JOIN "
              + "MetaSelectItem "
              + ("msi_" + (msi))
              + " ON CAST("
              + aliasName
              + "."
              + fieldName[index]
              + " AS text) = "
              + ("msi_" + (msi))
              + ".value AND "
              + ("msi_" + (msi))
              + ".select = "
              + metaSelect.getId());
    }
  }

  private void checkJoinSelectionField(String[] fieldName, int index, MetaSelect metaSelect) {
    if (language.equals(LANGUAGE_FR)) {
      selectionJoinFieldSet.add(
          "LEFT JOIN "
              + "MetaSelectItem "
              + ("msi_" + (msi))
              + " ON CAST("
              + "self."
              + fieldName[index]
              + " AS text) = "
              + ("msi_" + (msi))
              + ".value AND "
              + ("msi_" + (msi))
              + ".select = "
              + metaSelect.getId()
              + " LEFT JOIN "
              + "MetaTranslation "
              + ("mt_" + (mt))
              + " ON "
              + ("msi_" + (msi))
              + ".title = "
              + ("mt_" + (mt))
              + ".key AND "
              + ("mt_" + (mt))
              + ".language = \'"
              + language
              + "\'");

    } else {
      selectionJoinFieldSet.add(
          "LEFT JOIN "
              + "MetaSelectItem "
              + ("msi_" + (msi))
              + " ON CAST("
              + "self."
              + fieldName[index]
              + " AS text) = "
              + ("msi_" + (msi))
              + ".value AND "
              + ("msi_" + (msi))
              + ".select = "
              + metaSelect.getId());
    }
  }

  private void checkNormalField(String[] splitField, int parentIndex)
      throws ClassNotFoundException {

    if (isSelectionField) {
      if (parentIndex == 0) {
        selectSelectionField = "";
      }
      if (language.equals(LANGUAGE_FR)) {
        selectSelectionField += ("mt_" + (mt)) + ".message";
      } else {
        selectSelectionField += ("msi_" + (msi)) + ".title";
      }
      isSelectionField = false;
    } else {
      if (parentIndex == 0) {
        selectNormalField = "";
        selectNormalField += "self";
      }
      selectNormalField += "." + splitField[parentIndex];
    }
  }

  /**
   * This method build a dynamic query using <i>StringBuilder</i>.
   *
   * @param metaModel
   * @param selectFieldBuilder
   * @param selectJoinFieldBuilder
   * @param selectionFieldBuilder
   * @param criteria
   * @param orderByFieldBuilder
   * @return
   */
  private StringBuilder createQueryBuilder(
      MetaModel metaModel,
      StringBuilder selectFieldBuilder,
      StringBuilder selectJoinFieldBuilder,
      StringBuilder selectionFieldBuilder,
      String criteria,
      StringBuilder orderByFieldBuilder) {

    String joinField = "",
        selectionJoinField = "",
        selectionRelationalJoinField = "",
        orderByCol = "";
    StringBuilder queryBuilder = new StringBuilder();

    joinField = String.join(" ", joinFieldSet);
    selectionJoinField = String.join(" ", selectionJoinFieldSet);
    selectionRelationalJoinField = String.join(" ", selectionRelationalJoinFieldSet);

    params = null;
    criteria = getCriteria(metaModel, criteria);

    if (!orderByFieldBuilder.toString().equals(""))
      orderByCol =
          " ORDER BY " + orderByFieldBuilder.substring(0, orderByFieldBuilder.length() - 1);

    queryBuilder.append("SELECT NEW Map(");
    queryBuilder.append(
        (!selectFieldBuilder.toString().equals(""))
            ? selectFieldBuilder.substring(0, selectFieldBuilder.length() - 1) + ","
            : "");
    queryBuilder.append(
        (!selectJoinFieldBuilder.toString().equals(""))
            ? selectJoinFieldBuilder.substring(0, selectJoinFieldBuilder.length() - 1) + ","
            : "");
    queryBuilder.append(
        (!selectionFieldBuilder.toString().equals(""))
            ? selectionFieldBuilder.substring(0, selectionFieldBuilder.length() - 1)
            : "");
    if (queryBuilder.toString().endsWith(","))
      queryBuilder = new StringBuilder(queryBuilder.substring(0, queryBuilder.length() - 1));
    queryBuilder.append(") FROM " + metaModel.getName() + " self ");
    queryBuilder.append((!Strings.isNullOrEmpty(joinField)) ? "LEFT JOIN " : "");
    queryBuilder.append((!Strings.isNullOrEmpty(joinField)) ? joinField + " " : "");
    queryBuilder.append(
        (!Strings.isNullOrEmpty(selectionJoinField)) ? selectionJoinField + " " : "");
    queryBuilder.append(
        (!Strings.isNullOrEmpty(selectionRelationalJoinField)) ? selectionRelationalJoinField : "");
    queryBuilder.append((!Strings.isNullOrEmpty(criteria)) ? criteria : "");
    queryBuilder.append((!Strings.isNullOrEmpty(orderByCol)) ? orderByCol : "");

    return queryBuilder;
  }

  /**
   * This method make <i>WHERE</i> clause with criteria.
   *
   * @param metaModel
   * @param criteria
   * @return
   */
  private String getCriteria(MetaModel metaModel, String criteria) {
    if (!StringUtils.isNullOrEmpty(criteria)) {
      String criteriaIds = criteria.substring(1, criteria.length() - 1);
      log.trace("criteria : {}", criteriaIds);
      criteria = " WHERE self.id IN (" + criteriaIds + ")";
      return criteria;
    }
    Filter filter = getJpaSecurityFilter(metaModel);
    if (filter != null) {
      String permissionFilter = filter.getQuery();
      if (StringUtils.isNullOrEmpty(criteria)) {
        criteria = " WHERE " + permissionFilter;
      } else {
        criteria = criteria + " AND (" + permissionFilter + ")";
      }
      params = filter.getParams();
    }
    return criteria;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Filter getJpaSecurityFilter(MetaModel metaModel) {
    JpaSecurity jpaSecurity = Beans.get(JpaSecurity.class);
    try {
      return jpaSecurity.getFilter(
          JpaSecurity.CAN_EXPORT,
          (Class<? extends Model>) Class.forName(metaModel.getFullName()),
          (Long) null);
    } catch (ClassNotFoundException e) {
      log.error(e.getMessage());
    }
    return null;
  }

  private Query createQuery(StringBuilder queryBuilder) {
    log.trace("query : {}", queryBuilder.toString());
    Query query = JPA.em().createQuery(queryBuilder.toString(), Map.class);
    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        query.setParameter(i, params.get(i));
      }
    }
    return query;
  }

  private List<AdvancedExportLine> sortAdvancedExportLineList(
      List<AdvancedExportLine> advancedExportLineList) {

    Collections.sort(
        advancedExportLineList, (line1, line2) -> line1.getSequence() - line2.getSequence());
    return advancedExportLineList;
  }

  /**
   * Initialize the object of <i>AdvancedExportGenerator</i> based on file type.
   *
   * @throws AxelorException
   * @throws IOException
   * @throws DocumentException
   * @throws ClassNotFoundException
   */
  @Override
  public Map<Boolean, MetaFile> getAdvancedExport(
      AdvancedExport advancedExport, String criteria, String fileType)
      throws ClassNotFoundException, DocumentException, IOException, AxelorException {

    String extension = "";
    AdvancedExportGenerator exportGenerator = null;

    switch (fileType) {
      case PDF:
        extension = PDF_EXTENSION;
        exportGenerator = new PdfExportGenerator();
        break;
      case EXCEL:
        extension = EXCEL_EXTENSION;
        exportGenerator = new ExcelExportGenerator();
        break;
      case CSV:
        extension = CSV_EXTENSION;
        exportGenerator = new CsvExportGenerator();
        break;
      default:
        break;
    }
    return createAdvancedExportFile(exportGenerator, advancedExport, criteria, extension, fileType);
  }

  /**
   * Create the export file based on <i>AdvancedExportGenerator</i>.
   *
   * @param exportGenerator
   * @param advancedExport
   * @param criteria
   * @param extension
   * @param fileType
   * @return
   * @throws DocumentException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws AxelorException
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<Boolean, MetaFile> createAdvancedExportFile(
      AdvancedExportGenerator exportGenerator,
      AdvancedExport advancedExport,
      String criteria,
      String extension,
      String fileType)
      throws ClassNotFoundException, DocumentException, IOException, AxelorException {

    Map<Boolean, MetaFile> exportMap = new HashMap<Boolean, MetaFile>();
    List<Map> dataList = new ArrayList<>();
    boolean isReachMaxExportLimit = false;
    int startPosition = 0;
    int reachLimit = 0;
    int maxExportLimit = advancedExport.getMaxExportLimit();
    int queryFetchLimit = advancedExport.getQueryFetchSize();
    MetaModel metaModel = advancedExport.getMetaModel();
    List<AdvancedExportLine> advancedExportLineList =
        sortAdvancedExportLineList(advancedExport.getAdvancedExportLineList());

    Query query = getAdvancedExportQuery(advancedExport, criteria);
    query.setMaxResults(queryFetchLimit);

    String fileName = metaModel.getName() + extension;
    File tempFile = File.createTempFile(metaModel.getName(), extension);

    // Initialize the object depends on fileType. e.g. Pdf - Document, Excel - WorkBook, etc.
    exportGenerator.initialize(advancedExportLineList, metaModel, tempFile);

    log.debug("Export file : {}", fileName);
    // generate the header of export file.
    exportGenerator.generateHeader();

    while (startPosition < maxExportLimit) {
      if ((maxExportLimit - startPosition) < queryFetchLimit) {
        query.setMaxResults((maxExportLimit - startPosition));
      }
      query.setFirstResult(startPosition);
      dataList = query.getResultList();
      if (dataList.isEmpty()) break;

      // generate the body of export file.
      exportGenerator.generateBody(dataList);

      startPosition = startPosition + queryFetchLimit;
      reachLimit += dataList.size();
    }
    if (maxExportLimit == reachLimit) {
      isReachMaxExportLimit = true;
    }
    // close the object.
    exportGenerator.close();

    FileInputStream inStream = new FileInputStream(tempFile);
    MetaFile exportFile = metaFiles.upload(inStream, fileName);
    inStream.close();
    tempFile.delete();

    exportMap.put(isReachMaxExportLimit, exportFile);
    return exportMap;
  }

  /**
   * Explicitly convert decimal value with it's scale.
   *
   * @param value
   * @return
   */
  public static String convertDecimalValue(Object value) {
    BigDecimal decimalVal = (BigDecimal) value;
    return String.format("%." + decimalVal.scale() + "f", decimalVal);
  }
}
