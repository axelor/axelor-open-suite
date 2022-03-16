/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

public class WordReportTableServiceImpl implements WordReportTableService {

  @Inject WordReportHelperService helperService;
  @Inject WordReportTranslationService translationService;
  @Inject WordReportGroovyService groovyService;
  @Inject WordReportQueryBuilderService queryBuilderService;

  private ResourceBundle resourceBundle;

  @Override
  public void setTable(
      Tbl table,
      Mapper mapper,
      Object object,
      ResourceBundle rsBundle,
      Map<String, List<Object>> reportQueryBuilderResultMap)
      throws ScriptException, AxelorException, IOException, ClassNotFoundException {
    // check for internal tables
    this.processInternalTables(table, mapper, object, rsBundle, reportQueryBuilderResultMap);

    // get all rows
    Triple<Tr, Tr, Tr> rowTriple = this.getRows(table);
    Tr headerRow = rowTriple.getLeft();
    Tr collectionRow = rowTriple.getMiddle();
    Tr footerRow = rowTriple.getRight();
    if (headerRow == null && collectionRow == null && footerRow == null) {
      return;
    }

    this.resourceBundle = rsBundle;
    List<Integer> removeColumnIndexList = new ArrayList<>();

    // set collection data rows
    this.setCollectionRows(
        table, mapper, object, collectionRow, removeColumnIndexList, reportQueryBuilderResultMap);

    // set footer row
    this.setFooterRow(table, footerRow);

    // hide columns
    this.hideColumns(table, removeColumnIndexList);
  }

  protected void processInternalTables(
      Tbl table,
      Mapper mapper,
      Object object,
      ResourceBundle rsBundle,
      Map<String, List<Object>> reportQueryBuilderResultMap)
      throws ScriptException, AxelorException, IOException, ClassNotFoundException {
    List<Object> internalCellList = helperService.getAllElementFromObject(table, Tc.class);
    if (ObjectUtils.notEmpty(internalCellList)) {
      for (Object internalCellOb : internalCellList) {
        List<Object> internalTableList =
            helperService.getAllElementFromObject(internalCellOb, Tbl.class);
        if (ObjectUtils.notEmpty(internalTableList)) {
          for (Object internalTableOb : internalTableList) {
            setTable((Tbl) internalTableOb, mapper, object, rsBundle, reportQueryBuilderResultMap);
          }
        }
      }
    }
  }

  protected Triple<Tr, Tr, Tr> getRows(Tbl table) {
    List<Object> tableRows = table.getContent();
    int tableSize = tableRows.size();
    Tr headerRow = null;
    Tr collectionRow = null;
    Tr footerRow = null;

    if (tableSize == 1) {
      collectionRow = (Tr) tableRows.get(0);
    } else if (tableSize == 2) {
      headerRow = (Tr) tableRows.get(0);
      collectionRow = (Tr) tableRows.get(1);
    } else if (tableSize == 3) {
      headerRow = (Tr) tableRows.get(0);
      collectionRow = (Tr) tableRows.get(1);
      footerRow = (Tr) tableRows.get(2);
    }

    return Triple.of(headerRow, collectionRow, footerRow);
  }

  @SuppressWarnings("unchecked")
  protected void setCollectionRows(
      Tbl table,
      Mapper mapper,
      Object object,
      Tr collectionRow,
      List<Integer> removeColumnIndexList,
      Map<String, List<Object>> reportQueryBuilderResultMap)
      throws AxelorException, ScriptException, IOException, ClassNotFoundException {
    List<Object> cellList = helperService.getAllElementFromObject(collectionRow, Tc.class);
    List<Object> variableTextList =
        helperService.getAllElementFromObject(collectionRow, Text.class);
    Map<Integer, List<String>> dataMap = new HashMap<>();
    int totalRecord = 0;
    boolean isCollectionRow = false;
    boolean hide = false;

    for (int i = 0; i < variableTextList.size(); i++) {
      Text text = (Text) variableTextList.get(i);
      String propertyName = text.getValue();

      // check for hide wrapper
      Pair<String, Boolean> hidePair = helperService.checkHideWrapper(propertyName, object);
      propertyName = hidePair.getLeft();
      hide = hidePair.getRight();

      // check for if else wrapper
      Pair<String, String> valueOperationPair =
          helperService.checkIfElseWrapper(propertyName, object);
      propertyName = valueOperationPair.getLeft();
      String operationString = valueOperationPair.getRight();

      // check for translation
      Pair<Boolean, String> translationPair =
          translationService.checkTranslationFunction(propertyName);
      boolean translate = translationPair.getLeft();
      propertyName = translationPair.getRight();

      if (StringUtils.notEmpty(propertyName) && propertyName.startsWith("$")) {
        if (hide) {
          removeColumnIndexList.add(findColumnIndex(cellList, text));
          dataMap.put(i, null);
          continue;
        }

        Property property = helperService.getProperty(mapper, propertyName.substring(1));
        if (property != null && property.isCollection()) {
          isCollectionRow = true;
          Collection<Object> collection = (Collection<Object>) property.get(object);
          totalRecord = collection.size();
          dataMap.put(
              i,
              this.getCollectionColumnData(
                  collection, property, Triple.of(propertyName, operationString, translate)));
        } else if (ObjectUtils.notEmpty(reportQueryBuilderResultMap)
            && reportQueryBuilderResultMap.containsKey(
                propertyName.substring(1, propertyName.indexOf(".")))) {
          isCollectionRow = true;
          List<Object> collection =
              reportQueryBuilderResultMap.get(propertyName.substring(1, propertyName.indexOf(".")));
          totalRecord = collection.size();
          dataMap.put(
              i,
              queryBuilderService.getReportQueryColumnData(
                  table, propertyName.substring(1), collection, operationString, resourceBundle));
        } else {
          dataMap.put(i, null);
        }
      }
    }

    // if not a collection row, keep it unchanged
    if (!isCollectionRow) {
      return;
    }

    // add data rows
    for (int i = 0; i < totalRecord; i++) {
      this.addRowToTable(table, collectionRow, dataMap, i);
    }

    // remove the template row
    table.getContent().remove(collectionRow);
  }

  private List<String> getCollectionColumnData(
      Collection<Object> collection,
      Property property,
      Triple<String, String, Boolean> propertyNameOperationStringTranslateTriple)
      throws AxelorException, ScriptException {
    String propertyName = propertyNameOperationStringTranslateTriple.getLeft();
    String operationString = propertyNameOperationStringTranslateTriple.getMiddle();
    boolean translate = propertyNameOperationStringTranslateTriple.getRight();
    List<String> columnData = new ArrayList<>();
    Mapper o2mMapper = Mapper.of(property.getTarget());
    for (Object ob : collection) {
      ImmutablePair<Property, Object> pair =
          helperService.findField(
              o2mMapper, ob, propertyName.substring(propertyName.indexOf(".") + 1));
      if (ObjectUtils.isEmpty(pair)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            IExceptionMessage.SYNTAX_ERROR + propertyName);
      }
      property = pair.getLeft();
      ob = pair.getRight();
      String result = this.getCollectionKeyValue(property, ob, translate);
      result =
          ObjectUtils.notEmpty(operationString)
              ? groovyService.calculateFromString(
                  result.concat(operationString), helperService.getBigDecimalScale())
              : result;
      columnData.add(result);
    }
    return columnData;
  }

  private String getCollectionKeyValue(Property property, Object ob, boolean translate) {
    Object keyValue = "";

    if (property.isReference()) {
      keyValue = helperService.findNameColumn(property, property.get(ob));
    } else if (!ObjectUtils.isEmpty(property.getSelection())) {
      String title =
          MetaStore.getSelectionItem(property.getSelection(), property.get(ob).toString())
              .getTitle();
      keyValue = I18n.get(title);
    } else if (property.get(ob).getClass().equals(BigDecimal.class)) {
      keyValue =
          ((BigDecimal) property.get(ob)).setScale(helperService.getBigDecimalScale()).toString();
    } else {
      keyValue = property.get(ob).toString();
    }

    if (translate) {
      keyValue = translationService.getValueTranslation(keyValue.toString(), resourceBundle);
    }
    return keyValue.toString();
  }

  private void addRowToTable(
      Tbl table, Tr templateRow, Map<Integer, List<String>> dataMap, int rowNumber) {
    Tr workingRow = XmlUtils.deepCopy(templateRow);
    List<?> textElements = helperService.getAllElementFromObject(workingRow, Text.class);
    for (int i = 0; i < textElements.size(); i++) {
      Text text = (Text) textElements.get(i);
      String replacementValue = "";
      if (ObjectUtils.notEmpty(dataMap.get(i))) {
        replacementValue = dataMap.get(i).get(rowNumber);
      }
      if (replacementValue != null) text.setValue(replacementValue);
    }
    table.getContent().add(workingRow);
  }

  private void setFooterRow(Tbl table, Tr footerRow) {
    if (footerRow == null) {
      return;
    }
    Tr workingRow = XmlUtils.deepCopy(footerRow);
    // process row here
    table.getContent().add(workingRow);
    table.getContent().remove(footerRow);
  }

  private int findColumnIndex(List<Object> cellList, Text text) {
    int columnIndex = -1;

    for (Object cell : cellList) {
      Tc tc = (Tc) cell;
      List<Object> textList = helperService.getAllElementFromObject(tc, Text.class);
      for (Object ob : textList) {
        Text t = (Text) ob;
        if (t.equals(text)) {
          return cellList.indexOf(cell);
        }
      }
    }
    return columnIndex;
  }

  private void hideColumns(Tbl table, List<Integer> removeColumnIndexList) {
    if (ObjectUtils.notEmpty(removeColumnIndexList)) {
      Collections.sort(removeColumnIndexList, Collections.reverseOrder());
      List<Object> rowList = helperService.getAllElementFromObject(table, Tr.class);
      for (Object row : rowList) {
        Tr tr = (Tr) row;
        for (int index : removeColumnIndexList) {
          tr.getContent().remove(index);
        }
      }
    }
  }
}
