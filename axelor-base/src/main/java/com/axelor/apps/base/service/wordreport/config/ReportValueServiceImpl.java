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
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.docx4j.wml.Text;

public class ReportValueServiceImpl implements ReportValueService {

  @Inject WordReportHelperService helperService;
  @Inject WordReportGroovyService groovyService;
  @Inject WordReportTranslationService translationService;
  @Inject WordReportQueryBuilderService queryBuilderService;

  @Override
  public void setTextValue(
      Mapper mapper,
      Text text,
      Object object,
      ResourceBundle resourceBundle,
      Map<String, List<Object>> reportQueryBuilderResultMap)
      throws AxelorException, ClassNotFoundException, IOException, ScriptException {
    String value = text.getValue();
    if (com.axelor.common.StringUtils.isEmpty(value)
        || com.axelor.common.StringUtils.isBlank(value)) {
      return;
    }

    // check for hide wrapper
    Pair<String, Boolean> hidePair = helperService.checkHideWrapper(value, object);
    boolean hide = hidePair.getRight();
    value = hidePair.getLeft();

    // check for if else wrapper
    Pair<String, String> valueOperationPair = helperService.checkIfElseWrapper(value, object);
    value = valueOperationPair.getLeft();
    String operationString = valueOperationPair.getRight();

    // check for translation wrapper
    Pair<Boolean, String> translationPair = translationService.checkTranslationFunction(value);
    boolean translate = translationPair.getLeft();
    value = translationPair.getRight();

    if (!value.startsWith("$")) {
      setConstantValue(text, value, translate, hide, resourceBundle);
    } else if (value.startsWith("$")) {
      setVariableValue(
          text,
          value.substring(1),
          operationString,
          mapper,
          Pair.of(reportQueryBuilderResultMap, object),
          Triple.of(resourceBundle, translate, hide));
    }
  }

  private void setConstantValue(
      Text text, String value, boolean translate, boolean hide, ResourceBundle resourceBundle) {
    if (hide) {
      text.setValue("");
    } else {
      text.setValue(translate ? resourceBundle.getString(value) : value);
    }
  }

  private void setVariableValue(
      Text text,
      String value,
      String operationString,
      Mapper mapper,
      Pair<Map<String, List<Object>>, Object> reportQueryBuilderResultMapObject,
      Triple<ResourceBundle, Boolean, Boolean> resourceBundleTranslateHidePair)
      throws AxelorException, ClassNotFoundException, ScriptException {
    Property property = helperService.getProperty(mapper, value);
    boolean hide = resourceBundleTranslateHidePair.getRight();

    if (ObjectUtils.isEmpty(property)) {
      if (hide) {
        text.setValue("");
      } else if (value.startsWith("eval:")) {
        text.setValue(
            groovyService
                .evaluate(
                    value.substring(value.indexOf(":") + 1),
                    reportQueryBuilderResultMapObject.getRight())
                .toString());
      } else if (ObjectUtils.notEmpty(reportQueryBuilderResultMapObject.getLeft())
          && reportQueryBuilderResultMapObject
              .getLeft()
              .containsKey(value.substring(0, value.indexOf(".")))) {
        queryBuilderService.setReportQueryTextValue(
            text,
            reportQueryBuilderResultMapObject.getLeft().get(value.substring(0, value.indexOf("."))),
            value,
            operationString,
            resourceBundleTranslateHidePair.getLeft());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(I18n.get(IExceptionMessage.NO_SUCH_FIELD), value));
      }
    } else if (!property.isCollection()) {
      String result =
          getNonCollectionValue(
              mapper,
              value,
              reportQueryBuilderResultMapObject.getRight(),
              Pair.of(
                  resourceBundleTranslateHidePair.getLeft(),
                  resourceBundleTranslateHidePair.getMiddle()));
      result =
          ObjectUtils.notEmpty(operationString)
              ? groovyService.calculateFromString(
                  result.concat(operationString), helperService.getBigDecimalScale())
              : result;

      // add hide check here
      text.setValue(hide ? "" : result);
    }
  }

  private String getNonCollectionValue(
      Mapper mapper,
      String value,
      Object object,
      Pair<ResourceBundle, Boolean> resourceBundleTranslatePair)
      throws AxelorException {
    Property property = null;
    ImmutablePair<Property, Object> pair = helperService.findField(mapper, object, value);
    if (ObjectUtils.isEmpty(pair)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(IExceptionMessage.NO_SUCH_FIELD), value));
    }
    property = pair.getLeft();
    object = pair.getRight();

    Object outputValue = "";
    if (object == null || ObjectUtils.isEmpty(property.get(object))) {
      outputValue = "";
    } else if (property.isReference()) {
      outputValue = helperService.findNameColumn(property, property.get(object));
    } else if (!ObjectUtils.isEmpty(property.getSelection())) {

      String title =
          MetaStore.getSelectionItem(property.getSelection(), property.get(object).toString())
              .getTitle();
      outputValue = I18n.get(title);

    } else if (property.get(object).getClass() == LocalDate.class
        || property.get(object).getClass() == LocalDateTime.class) {
      outputValue = helperService.getDateTimeFormat(property.get(object));
    } else if (property.get(object).getClass().equals(BigDecimal.class)) {
      outputValue =
          ((BigDecimal) property.get(object))
              .setScale(helperService.getBigDecimalScale())
              .toString();
    } else {
      outputValue = property.get(object).toString();
    }

    if (Boolean.TRUE.equals(resourceBundleTranslatePair.getRight())) {
      outputValue =
          translationService.getValueTranslation(value, resourceBundleTranslatePair.getLeft());
    }

    return outputValue.toString();
  }
}
