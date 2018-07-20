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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.AdvancedExportLineRepository;
import com.axelor.apps.tool.NammingTool;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mysql.jdbc.StringUtils;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Query;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedExportServiceImpl implements AdvancedExportService {

  private final Logger log = LoggerFactory.getLogger(AdvancedExportServiceImpl.class);

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaSelectRepository metaSelectRepo;

  @Inject private AdvancedExportLineRepository advancedExportLineRepo;

  @Inject private MetaFiles metaFiles;

  private LinkedHashSet<String> joinFieldSet = new LinkedHashSet<>();
  private List<String> selectJoinFieldList = new ArrayList<>();
  private LinkedHashSet<String> selectionJoinFieldSet = new LinkedHashSet<>();
  private LinkedHashSet<String> selectionRelationalJoinFieldSet = new LinkedHashSet<>();

  private String language = "";

  private String selectNormalField = "";
  private String selectSelectionField = "";

  private String temp = "";

  private int counter = 0;
  private int counter2 = 0;
  private int counter3 = 0;
  private int nbrField = 0;

  private boolean isSelectionField = false;

  private int msi = 0;
  private int mt = 0;

  private List<Object> params = null;

  @Override
  public String getTargetField(
      Context context, MetaField metaField, String targetField, MetaModel parentMetaModel) {

    targetField = (String) context.get("targetField");
    String[] splitField = targetField.split("\\.");

    MetaField metaField2 = this.checkLastMetaField(splitField, 0, parentMetaModel, metaField);

    if (metaField2.getRelationship() != null) {
      targetField += "." + metaField.getName();
      return targetField;

    } else {
      return targetField.replace(splitField[splitField.length - 1], metaField.getName());
    }
  }

  private MetaField checkLastMetaField(
      String[] splitField, int i, MetaModel parentMetaModel, MetaField metaField) {

    MetaModel metaModel = parentMetaModel;
    MetaField metaField3 = metaField;

    if (i <= splitField.length - 1) {

      if (splitField[0].equals(splitField[splitField.length - 1])) {

        metaField3 =
            metaFieldRepo
                .all()
                .filter("self.name = ?1 and self.metaModel = ?2", splitField[i], parentMetaModel)
                .fetchOne();

      } else {
        metaField3 =
            metaFieldRepo
                .all()
                .filter("self.name = ?1 and self.metaModel = ?2", splitField[i], parentMetaModel)
                .fetchOne();

        if (metaField3.getRelationship() != null) {
          metaModel = metaModelRepo.findByName(metaField3.getTypeName());
        }
      }
      metaField3 = checkLastMetaField(splitField, i + 1, metaModel, metaField3);
    }
    return metaField3;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public String createCriteria(ActionRequest request, int limit) {
    if (request.getContext().get("_criteria") != null) {
      if (request.getContext().get("_criteria").toString().startsWith("[")) {
        return request.getContext().get("_criteria").toString();

      } else {
        MetaModel metaModel =
            metaModelRepo.find(
                Long.valueOf(((Map) request.getContext().get("_metaModel")).get("id").toString()));
        ObjectMapper mapper = new ObjectMapper();
        ActionRequest parentRequest =
            mapper.convertValue(request.getContext().get("_criteria"), ActionRequest.class);
        Class<? extends Model> klass = (Class<? extends Model>) parentRequest.getBeanClass();
        Filter filter = this.getJpaSecurityFilter(metaModel);
        Stream<? extends Model> listObj =
            parentRequest.getCriteria().createQuery(klass, filter).fetchSteam(limit);
        return listObj.map(it -> it.getId()).collect(Collectors.toList()).toString();
      }
    }
    return null;
  }

  private Query getAdvancedExportData(
      List<Map<String, Object>> advancedExportLines, MetaModel metaModel, String criteria)
      throws ClassNotFoundException, DocumentException, IOException, AxelorException {

    int col = 0;
    String selectField = "";
    String selectJoinField = "";
    String joinField = "";
    String selectionField = "";
    String selectionJoinField = "";
    String selectionRelationalJoinField = "";
    String orderByCol = "";
    List<String> selectFieldList = new ArrayList<>();
    List<String> selectionFieldList = new ArrayList<>();
    List<String> orderByColumns = new ArrayList<>();
    selectJoinFieldList.clear();
    joinFieldSet.clear();
    selectionJoinFieldSet.clear();
    selectionRelationalJoinFieldSet.clear();
    counter = 0;

    language = AuthUtils.getUser().getLanguage();

    for (Map<String, Object> fieldLine : advancedExportLines) {

      AdvancedExportLine advancedExportLine =
          advancedExportLineRepo.find(Long.parseLong(fieldLine.get("id").toString()));

      String[] splitField = advancedExportLine.getTargetField().split("\\.");

      int cnt = this.getExportData(splitField, 0, metaModel);

      if (!joinFieldSet.isEmpty()
          && (!selectNormalField.equals("") || selectNormalField.indexOf(0) == '.')) {
        selectJoinFieldList.add(temp + selectNormalField + " AS " + ("Col_" + (col)));
      }

      if (!selectSelectionField.equals("") && !selectionJoinFieldSet.isEmpty()) {
        selectionFieldList.add(selectSelectionField + " AS " + ("Col_" + (col)));

      } else if (!selectSelectionField.equals("") && !selectionRelationalJoinFieldSet.isEmpty()) {
        selectionFieldList.add(selectSelectionField + " AS " + ("Col_" + (col)));

      } else if (cnt == 0 && selectSelectionField.equals("") && !selectNormalField.equals("")) {
        selectFieldList.add(
            "self." + advancedExportLine.getTargetField() + " AS " + ("Col_" + (col)));
      }

      if (advancedExportLine.getOrderBy()) {
        orderByColumns.add("Col_" + col);
      }
      selectNormalField = "";
      selectSelectionField = "";
      temp = "";
      counter2 = 0;
      counter3 = 0;
      nbrField++;
      col++;
    }

    selectField = String.join(",", selectFieldList);
    joinField = String.join(" ", joinFieldSet);
    selectJoinField = String.join(",", selectJoinFieldList);
    selectionField = String.join(",", selectionFieldList);
    selectionJoinField = String.join(" ", selectionJoinFieldSet);
    selectionRelationalJoinField = String.join(" ", selectionRelationalJoinFieldSet);

    params = null;
    criteria = getCriteria(metaModel, criteria);

    if (!orderByColumns.isEmpty()) orderByCol = " ORDER BY " + String.join(",", orderByColumns);

    return this.createQuery(
        metaModel,
        selectField,
        joinField,
        selectJoinField,
        selectionField,
        selectionJoinField,
        selectionRelationalJoinField,
        criteria,
        orderByCol);
  }

  private String getCriteria(MetaModel metaModel, String criteria) {

    if (!StringUtils.isNullOrEmpty(criteria)) {
      log.debug("criteria : {}", criteria.substring(1, criteria.length() - 1));
      criteria = " WHERE self.id IN (" + criteria.substring(1, criteria.length() - 1) + ")";
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

  private Query createQuery(
      MetaModel metaModel,
      String selectField,
      String joinField,
      String selectJoinField,
      String selectionField,
      String selectionJoinField,
      String selectionRelationalJoinField,
      String criteria,
      String orderByCol) {

    Query query = null;

    if (!selectField.equals("")
        && !selectJoinField.equals("")
        && !selectionField.equals("")
        && !selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + ","
              + selectJoinField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + " "
              + selectionRelationalJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ","
                      + selectJoinField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + " "
                      + selectionRelationalJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (!selectField.equals("")
        && !joinField.equals("")
        && !selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + ","
              + selectJoinField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ","
                      + selectJoinField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && selectJoinField.equals("")
        && !joinField.equals("")
        && !selectionField.equals("")
        && !selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + " "
              + selectionRelationalJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + " "
                      + selectionRelationalJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && !selectJoinField.equals("")
        && !joinField.equals("")
        && !selectionField.equals("")
        && !selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectJoinField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + " "
              + selectionRelationalJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectJoinField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + " "
                      + selectionRelationalJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && !joinField.equals("")
        && !selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectJoinField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectJoinField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && joinField.equals("")
        && !selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + selectionJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + selectionJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (!selectField.equals("")
        && joinField.equals("")
        && !selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + selectionJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + selectionJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (!selectField.equals("")
        && selectJoinField.equals("")
        && !selectionField.equals("")
        && !selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + ","
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + " "
              + selectionJoinField
              + " "
              + selectionRelationalJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ","
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + " "
                      + selectionJoinField
                      + " "
                      + selectionRelationalJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && joinField.equals("")
        && !selectionField.equals("")
        && selectionJoinField.equals("")
        && !selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectionField
              + " from "
              + metaModel.getName()
              + " self "
              + selectionRelationalJoinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectionField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + selectionRelationalJoinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (!selectField.equals("")
        && !joinField.equals("")
        && selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + ","
              + selectJoinField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ","
                      + selectJoinField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else if (selectField.equals("")
        && !joinField.equals("")
        && selectionField.equals("")
        && selectionRelationalJoinField.equals("")) {

      log.debug(
          "query : {}",
          "SELECT "
              + selectJoinField
              + " from "
              + metaModel.getName()
              + " self "
              + "LEFT JOIN "
              + joinField
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectJoinField
                      + ") from "
                      + metaModel.getName()
                      + " self "
                      + "LEFT JOIN "
                      + joinField
                      + criteria
                      + orderByCol,
                  Map.class);

    } else {
      log.debug(
          "query : {}",
          "SELECT "
              + selectField
              + " from "
              + metaModel.getName()
              + " self"
              + criteria
              + orderByCol);

      query =
          JPA.em()
              .createQuery(
                  "SELECT NEW Map("
                      + selectField
                      + ") from "
                      + metaModel.getName()
                      + " self"
                      + criteria
                      + orderByCol,
                  Map.class);
    }

    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        query.setParameter(i, params.get(i));
      }
    }

    return query;
  }

  private void checkSelectionField(String[] fieldName, int i, MetaModel metaModel)
      throws ClassNotFoundException {

    Class<?> klass = Class.forName(metaModel.getFullName());
    Mapper mapper = Mapper.of(klass);
    MetaSelect metaSelect =
        metaSelectRepo.findByName(mapper.getProperty(fieldName[i]).getSelection());

    if (metaSelect != null) {
      isSelectionField = true;
      msi++;
      mt++;

      if (counter > 0 && i != 0) {

        if (language.equals("fr")) {

          selectionRelationalJoinFieldSet.add(
              "LEFT JOIN MetaSelectItem "
                  + ("msi_" + (msi))
                  + " ON CAST("
                  + temp
                  + "."
                  + fieldName[i]
                  + " AS text) = "
                  + ("msi_" + (msi))
                  + ".value AND "
                  + ("msi_" + (msi))
                  + ".select = "
                  + metaSelect.getId()
                  + " LEFT JOIN MetaTranslation "
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
              "LEFT JOIN MetaSelectItem "
                  + ("msi_" + (msi))
                  + " ON CAST("
                  + temp
                  + "."
                  + fieldName[i]
                  + " AS text) = "
                  + ("msi_" + (msi))
                  + ".value AND "
                  + ("msi_" + (msi))
                  + ".select = "
                  + metaSelect.getId());
        }
      } else {
        if (language.equals("fr")) {

          selectionJoinFieldSet.add(
              "LEFT JOIN MetaSelectItem "
                  + ("msi_" + (msi))
                  + " ON CAST(self."
                  + fieldName[i]
                  + " AS text) = "
                  + ("msi_" + (msi))
                  + ".value AND "
                  + ("msi_" + (msi))
                  + ".select = "
                  + metaSelect.getId()
                  + " LEFT JOIN MetaTranslation "
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
              "LEFT JOIN MetaSelectItem "
                  + ("msi_" + (msi))
                  + " ON CAST(self."
                  + fieldName[i]
                  + " AS text) = "
                  + ("msi_" + (msi))
                  + ".value AND "
                  + ("msi_" + (msi))
                  + ".select = "
                  + metaSelect.getId());
        }
      }
    }
  }

  private int getExportData(String[] splitField, int i, MetaModel metaModel)
      throws ClassNotFoundException {

    if (i <= splitField.length - 1) {
      MetaField relationalField =
          metaFieldRepo
              .all()
              .filter("self.name = ?1 and self.metaModel = ?2", splitField[i], metaModel)
              .fetchOne();

      MetaModel subMetaModel =
          metaModelRepo.all().filter("self.name = ?1", relationalField.getTypeName()).fetchOne();

      if (relationalField.getPackageName() != null
          && !relationalField.getPackageName().startsWith("java")) {

        String alias = "";
        counter++;
        if (counter2 != 0 || counter3 != 0) {
          selectNormalField = "";
        }
        if (i != 0) {
          alias = isKeyword(splitField, 0);
          if (nbrField > 0 && !joinFieldSet.isEmpty()) {
            joinFieldSet.add("LEFT JOIN ");
          }
          joinFieldSet.add("self." + splitField[0] + " " + alias);
          temp = alias;

          for (int j = 1; j <= i; j++) {
            alias = isKeyword(splitField, j);
            if (!temp.equals(splitField[i])) {
              joinFieldSet.add("LEFT JOIN " + temp + "." + splitField[j] + " " + alias);
              temp = alias;
            }
          }
        } else {
          alias = isKeyword(splitField, i);
          if (nbrField > 0
              && !joinFieldSet.isEmpty()
              && !joinFieldSet.contains("self." + splitField[i] + " " + alias)) {
            joinFieldSet.add("LEFT JOIN self." + splitField[i] + " " + alias);
          } else {
            joinFieldSet.add("self." + splitField[i] + " " + alias);
          }
          temp = alias;
        }

      } else {

        this.checkSelectionField(splitField, i, metaModel);

        if (isSelectionField) {
          if (i == 0) {
            selectSelectionField = "";
          }
          if (language.equals("fr")) {
            selectSelectionField += ("mt_" + (mt)) + ".message";

          } else {
            selectSelectionField += ("msi_" + (msi)) + ".title";
          }

          isSelectionField = false;
        } else {

          if (i == 0) {
            selectNormalField = "";
            selectNormalField += "self";
          }
          selectNormalField += "." + splitField[i];
        }
      }
      getExportData(splitField, i + 1, subMetaModel);
    }
    return counter;
  }

  private String isKeyword(String[] fieldNames, int ind) {

    if (NammingTool.isKeyword(fieldNames[ind])) {
      return fieldNames[ind] + "_id";
    }
    return fieldNames[ind];
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<Boolean, MetaFile> advancedExportPDF(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws DocumentException, IOException, ClassNotFoundException, AxelorException {

    Map<Boolean, MetaFile> exportMap = new HashMap<Boolean, MetaFile>();
    boolean isReachMaxExportLimit = false;
    int startPosition = 0;
    int reachLimit = 0;
    List<Map> dataList = new ArrayList<>();
    Query query = this.getAdvancedExportData(advancedExportLines, metaModel, criteria);
    query.setMaxResults(queryFetchLimit);

    File pdfFile = File.createTempFile(metaModel.getName(), ".pdf");
    FileOutputStream outStream = new FileOutputStream(pdfFile);
    Document document = new Document();
    PdfPTable table = new PdfPTable(advancedExportLines.size());
    PdfWriter.getInstance(document, outStream);
    document.open();

    createPDFHeader(advancedExportLines, table);

    while (startPosition < maxExportLimit) {
      if ((maxExportLimit - startPosition) < queryFetchLimit) {
        query.setMaxResults((maxExportLimit - startPosition));
      }
      query.setFirstResult(startPosition);
      dataList = query.getResultList();
      if (dataList.isEmpty()) break;

      createPDfData(table, dataList);
      log.debug("File processing: {}", pdfFile.getName());

      startPosition = startPosition + queryFetchLimit;
      reachLimit += dataList.size();
    }

    if (maxExportLimit == reachLimit) {
      isReachMaxExportLimit = true;
    }

    document.add(table);
    document.close();

    FileInputStream inStream = new FileInputStream(pdfFile);
    MetaFile exportFile = metaFiles.upload(inStream, metaModel.getName() + ".pdf");
    inStream.close();
    pdfFile.delete();

    exportMap.put(isReachMaxExportLimit, exportFile);
    return exportMap;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void createPDfData(PdfPTable table, List<Map> dataList) {

    PdfPCell cell;
    Font font = new Font();
    font.setSize(7);

    for (Map<String, Object> field : dataList) {
      String[] allCols = field.keySet().toArray(new String[field.size()]);
      Integer[] allColIndices = new Integer[allCols.length];

      for (int j = 0; j < allCols.length; j++) {
        String col = allCols[j];
        allColIndices[j] = Integer.parseInt(col.replace("Col_", ""));
      }
      Arrays.sort(allColIndices);

      for (Integer colIndex : allColIndices) {
        String colName = "Col_" + colIndex;
        Object value = field.get(colName);
        if (value == null) {
          cell = new PdfPCell(new Phrase(null, font));
        } else {
          cell = new PdfPCell(new Phrase(value.toString(), font));
        }
        table.addCell(cell);
      }
    }
  }

  private void createPDFHeader(List<Map<String, Object>> advancedExportLines, PdfPTable table)
      throws DocumentException, IOException {

    PdfPCell headerCell;

    for (Map<String, Object> fieldLine : advancedExportLines) {
      AdvancedExportLine advancedExportLine =
          advancedExportLineRepo.find(Long.parseLong(fieldLine.get("id").toString()));
      headerCell =
          new PdfPCell(
              new Phrase(
                  I18n.get(advancedExportLine.getTitle()),
                  new Font(BaseFont.createFont(), 8, 0, BaseColor.WHITE)));
      headerCell.setBackgroundColor(BaseColor.GRAY);
      headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(headerCell);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<Boolean, MetaFile> advancedExportExcel(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws IOException, ClassNotFoundException, DocumentException, AxelorException,
          InvalidFormatException {

    Map<Boolean, MetaFile> exportMap = new HashMap<Boolean, MetaFile>();
    boolean isReachMaxExportLimit = false;
    int startPosition = 0;
    int reachLimit = 0;
    List<Map> dataList = new ArrayList<>();
    Query query = this.getAdvancedExportData(advancedExportLines, metaModel, criteria);
    query.setMaxResults(queryFetchLimit);

    File excelFile = File.createTempFile(metaModel.getName(), ".xlsx");
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(metaModel.getName());

    createExcelHeader(advancedExportLines, metaModel, sheet);

    while (startPosition < maxExportLimit) {
      if ((maxExportLimit - startPosition) < queryFetchLimit) {
        query.setMaxResults((maxExportLimit - startPosition));
      }
      query.setFirstResult(startPosition);
      dataList = query.getResultList();
      if (dataList.isEmpty()) break;

      createExcelData(dataList, sheet);
      log.debug("File processing: {}", excelFile.getName());

      startPosition = startPosition + queryFetchLimit;
      reachLimit += dataList.size();
    }

    if (maxExportLimit == reachLimit) {
      isReachMaxExportLimit = true;
    }

    FileOutputStream fout = new FileOutputStream(excelFile);
    workbook.write(fout);
    fout.close();

    FileInputStream inStream = new FileInputStream(excelFile);
    MetaFile exportFile = metaFiles.upload(inStream, metaModel.getName() + ".xlsx");
    inStream.close();
    excelFile.delete();

    exportMap.put(isReachMaxExportLimit, exportFile);
    return exportMap;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void createExcelData(List<Map> dataList, Sheet sheet) {

    for (Map<String, Object> field : dataList) {
      String[] allCols = field.keySet().toArray(new String[field.size()]);
      Integer[] allColIndices = new Integer[allCols.length];

      for (int j = 0; j < allCols.length; j++) {
        String col = allCols[j];
        allColIndices[j] = Integer.parseInt(col.replace("Col_", ""));
      }
      Arrays.sort(allColIndices);

      Row row = sheet.createRow(sheet.getLastRowNum() + 1);
      int colNum = 0;
      for (Integer colIndex : allColIndices) {
        String colName = "Col_" + colIndex;
        Object value = field.get(colName);
        Cell cell = row.createCell(colNum++);
        if (value == null) continue;
        cell.setCellValue(value.toString());
      }
    }
  }

  private void createExcelHeader(
      List<Map<String, Object>> advancedExportLines, MetaModel metaModel, Sheet sheet) {

    Row headerRow = sheet.createRow(sheet.getFirstRowNum());
    int colHeaderNum = 0;

    for (Map<String, Object> fieldLine : advancedExportLines) {
      AdvancedExportLine advancedExportLine =
          advancedExportLineRepo.find(Long.parseLong(fieldLine.get("id").toString()));
      Cell headerCell = headerRow.createCell(colHeaderNum++);
      headerCell.setCellValue(I18n.get(advancedExportLine.getTitle()));
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<Boolean, MetaFile> advancedExportCSV(
      List<Map<String, Object>> advancedExportLines,
      MetaModel metaModel,
      String criteria,
      Integer maxExportLimit,
      Integer queryFetchLimit)
      throws IOException, ClassNotFoundException, DocumentException, AxelorException {

    Map<Boolean, MetaFile> exportMap = new HashMap<Boolean, MetaFile>();
    boolean isReachMaxExportLimit = false;
    int startPosition = 0;
    int index = 0;
    int reachLimit = 0;
    List<Map> dataList = new ArrayList<>();
    Query query = this.getAdvancedExportData(advancedExportLines, metaModel, criteria);
    query.setMaxResults(queryFetchLimit);

    File csvFile = File.createTempFile(metaModel.getName(), ".csv");
    CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile, true), ';');
    String[] totalCols = new String[advancedExportLines.size()];

    createCsvHeader(advancedExportLines, totalCols, index, csvWriter, csvFile);

    while (startPosition < maxExportLimit) {
      if ((maxExportLimit - startPosition) < queryFetchLimit) {
        query.setMaxResults((maxExportLimit - startPosition));
      }
      query.setFirstResult(startPosition);
      dataList = query.getResultList();
      if (dataList.isEmpty()) break;

      createCsvData(dataList, totalCols, index, csvWriter);
      log.debug("File processing: {}", csvFile.getName());

      startPosition = startPosition + queryFetchLimit;
      reachLimit += dataList.size();
    }

    if (maxExportLimit == reachLimit) {
      isReachMaxExportLimit = true;
    }

    csvWriter.close();

    FileInputStream inStream = new FileInputStream(csvFile);
    MetaFile exportFile = metaFiles.upload(inStream, metaModel.getName() + ".csv");
    inStream.close();
    csvFile.delete();

    exportMap.put(isReachMaxExportLimit, exportFile);
    return exportMap;
  }

  private void createCsvHeader(
      List<Map<String, Object>> advancedExportLines,
      String[] totalCols,
      int index,
      CSVWriter csvWriter,
      File csvFile)
      throws IOException {

    for (Map<String, Object> fieldLine : advancedExportLines) {
      AdvancedExportLine advancedExportLine =
          advancedExportLineRepo.find(Long.parseLong(fieldLine.get("id").toString()));
      totalCols[index++] = I18n.get(advancedExportLine.getTitle());
    }
    csvWriter.writeNext(totalCols);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void createCsvData(
      List<Map> dataList, String[] totalCols, int index, CSVWriter csvWriter) {

    index = 0;
    for (Map<String, Object> field : dataList) {
      String[] allCols = field.keySet().toArray(new String[field.size()]);
      Integer[] allColIndices = new Integer[allCols.length];

      for (int j = 0; j < allCols.length; j++) {
        String col = allCols[j];
        allColIndices[j] = Integer.parseInt(col.replace("Col_", ""));
      }
      Arrays.sort(allColIndices);

      for (Integer colIndex : allColIndices) {
        String colName = "Col_" + colIndex;
        Object value = field.get(colName);
        if (value == null || value.equals("")) {
          totalCols[index++] = null;
        } else {
          totalCols[index++] = value.toString();
        }
      }
      csvWriter.writeNext(totalCols);
      index = 0;
    }
  }
}
