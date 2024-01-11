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
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.db.repo.AnonymizerLineRepository;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.utils.ComputeNameTool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

public class DataBackupAnonymizeServiceImpl implements DataBackupAnonymizeService {

  protected AnonymizerLineRepository anonymizerLineRepository;
  protected AnonymizeService anonymizeService;

  @Inject
  public DataBackupAnonymizeServiceImpl(
      AnonymizerLineRepository anonymizerLineRepository, AnonymizeService anonymizeService) {
    this.anonymizerLineRepository = anonymizerLineRepository;
    this.anonymizeService = anonymizeService;
  }

  @Override
  public List<AnonymizerLine> searchAnonymizerLines(
      DataBackup dataBackup, Property property, String metaModelName) {
    List<AnonymizerLine> anonymizerLines = new ArrayList<>();
    for (AnonymizerLine anonymizerLine : dataBackup.getAnonymizer().getAnonymizerLineList()) {
      anonymizerLines =
          getAnonymizerLines(dataBackup, property, metaModelName, anonymizerLines, anonymizerLine);
      filterAnonymizerLines(property, metaModelName, anonymizerLines, anonymizerLine);
    }
    return anonymizerLines;
  }

  protected void filterAnonymizerLines(
      Property property,
      String metaModelName,
      List<AnonymizerLine> anonymizerLines,
      AnonymizerLine anonymizerLine) {
    if (metaModelName.equals(anonymizerLine.getMetaModel().getName())
        && anonymizerLine.getMetaField() != null
        && property.getName().equals(anonymizerLine.getMetaField().getName())) {
      anonymizerLines.add(anonymizerLine);
    }
  }

  protected List<AnonymizerLine> getAnonymizerLines(
      DataBackup dataBackup,
      Property property,
      String metaModelName,
      List<AnonymizerLine> anonymizerLines,
      AnonymizerLine anonymizerLine) {
    if (property.isJson()
        && metaModelName.equals(anonymizerLine.getMetaModel().getName())
        && anonymizerLine.getMetaField() != null
        && property.getName().equals(anonymizerLine.getMetaField().getName())
        && anonymizerLine.getMetaJsonField() != null) {
      anonymizerLines = fetchAnonymizerLines(dataBackup, property, metaModelName);
    }
    return anonymizerLines;
  }

  protected List<AnonymizerLine> fetchAnonymizerLines(
      DataBackup dataBackup, Property property, String metaModelName) {
    return anonymizerLineRepository
        .all()
        .filter(
            "self.anonymizer = :anonymizer "
                + "AND self.metaModel.name = :metaModel "
                + "AND self.metaField.name = :metaField "
                + "AND self.metaJsonField is NOT NULL")
        .bind("anonymizer", dataBackup.getAnonymizer())
        .bind("metaModel", metaModelName)
        .bind("metaField", property.getName())
        .fetch();
  }

  @Override
  public String anonymizeMetaModelData(
      Property property, String metaModelName, Object value, List<AnonymizerLine> anonymizerLines)
      throws AxelorException {
    if (property.isJson()) {
      return anonymizeMetaModelDataJsonValues(anonymizerLines, value);
    }
    return anonymizeMetaModelDataValues(anonymizerLines, value, property, metaModelName);
  }

  protected String anonymizeMetaModelDataJsonValues(
      List<AnonymizerLine> anonymizerLines, Object value) throws AxelorException {
    return anonymizeService
        .createAnonymizedJson(value, computeFakerMap(anonymizerLines))
        .toString();
  }

  protected HashMap<MetaJsonField, FakerApiField> computeFakerMap(
      List<AnonymizerLine> anonymizerLines) {
    HashMap<MetaJsonField, FakerApiField> fakerMap = new HashMap<>();
    anonymizerLines.stream()
        .filter(anonymizerLine -> anonymizerLine.getMetaJsonField() != null)
        .forEach(
            anonymizerLine ->
                fakerMap.put(anonymizerLine.getMetaJsonField(), anonymizerLine.getFakerApiField()));
    return fakerMap;
  }

  protected String anonymizeMetaModelDataValues(
      List<AnonymizerLine> anonymizerLines, Object value, Property property, String metaModelName)
      throws AxelorException {
    String result = "";
    for (AnonymizerLine anonymizerLine : anonymizerLines) {
      if (metaModelName.equals(anonymizerLine.getMetaModel().getName())
          && anonymizerLine.getMetaField() != null
          && property.getName().equals(anonymizerLine.getMetaField().getName())) {
        if (anonymizerLine.getFakerApiField() != null) {
          result =
              anonymizeService
                  .anonymizeValue(value, property, anonymizerLine.getFakerApiField())
                  .toString();
        } else {
          result = anonymizeService.anonymizeValue(value, property).toString();
        }
      }
    }
    return result;
  }

  @Override
  public List<String> csvComputeAnonymizedFullname(List<String> dataArr, List<String> headerArr) {

    if (headerArr.indexOf("simpleFullName") < 0
        || headerArr.indexOf("fullName") < 0
        || headerArr.indexOf("firstName") < 0
        || headerArr.indexOf("name") < 0
        || headerArr.indexOf("importId") < 0) {
      return dataArr;
    }

    List<Integer> headersIndex = new ArrayList<>();
    int headerMax;

    headersIndex.add(headerArr.indexOf("simpleFullName"));
    headersIndex.add(headerArr.indexOf("fullName"));
    headersIndex.add(headerArr.indexOf("firstName"));
    headersIndex.add(headerArr.indexOf("name"));
    headersIndex.add(headerArr.indexOf("importId"));

    headerMax = Collections.max(headersIndex);

    if (dataArr.size() < headerMax) {
      return dataArr;
    }

    dataArr.set(
        headerArr.indexOf("simpleFullName"),
        ComputeNameTool.computeSimpleFullName(
            dataArr.get(headerArr.indexOf("firstName")),
            dataArr.get(headerArr.indexOf("name")),
            dataArr.get(headerArr.indexOf("importId"))));
    dataArr.set(
        headerArr.indexOf("fullName"),
        ComputeNameTool.computeFullName(
            dataArr.get(headerArr.indexOf("firstName")),
            dataArr.get(headerArr.indexOf("name")),
            dataArr.get(headerArr.indexOf("partnerSeq")),
            dataArr.get(headerArr.indexOf("importId"))));

    return dataArr;
  }

  @Override
  public List<String> csvAnonymizeImportId(
      List<String> dataArr, List<String> headerArr, byte[] salt) {

    headerArr.stream()
        .filter(
            header ->
                header.contains("importId")
                    && dataArr.get(headerArr.indexOf(header)) != null
                    && !"".equals(dataArr.get(headerArr.indexOf(header))))
        .forEach(
            header ->
                dataArr.set(
                    headerArr.indexOf(header),
                    anonymizeImportId(dataArr, headerArr, header, salt)));
    return dataArr;
  }

  protected String anonymizeImportId(
      List<String> dataArr, List<String> headerArr, String header, byte[] salt) {
    StringBuilder anoImportId = new StringBuilder();
    if (dataArr.get(headerArr.indexOf(header)).contains("|")) {
      String[] importIds = dataArr.get(headerArr.indexOf(header)).split("\\|");
      for (String importId : importIds) {
        anoImportId.append(anonymizeService.hashValue(importId, salt)).append("|");
      }
    } else {
      anoImportId.append(anonymizeService.hashValue(dataArr.get(headerArr.indexOf(header)), salt));
    }
    return anoImportId.toString();
  }
}
