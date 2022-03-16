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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.docx4j.wml.ContentAccessor;

public class WordReportHelperServiceImpl implements WordReportHelperService {

  private static final String DATE_FORMAT = "dd/MM/YYYY";
  private static final String DATE_TIME_FORMAT = "dd/MM/YYYY HH:mm";
  private static final int BIG_DECIMAL_SCALE = 2;

  @Inject WordReportGroovyService groovyService;
  @Inject AppBaseService appBaseService;

  @Override
  public List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
    List<Object> result = new ArrayList<>();
    if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

    if (obj.getClass().equals(toSearch)) result.add(obj);
    else if (obj instanceof ContentAccessor) {
      List<?> children = ((ContentAccessor) obj).getContent();
      for (Object child : children) {
        result.addAll(getAllElementFromObject(child, toSearch));
      }
    }
    return result;
  }

  @Override
  public Property getProperty(Mapper mapper, String propertyName) {
    Property property;

    if (propertyName.contains(".")) {
      property = mapper.getProperty(propertyName.substring(0, propertyName.indexOf(".")));
    } else {
      property = mapper.getProperty(propertyName);
    }

    return property;
  }

  @Override
  public Mapper getMapper(String modelFullName) throws ClassNotFoundException {
    Class<?> klass = Class.forName(modelFullName);
    return Mapper.of(klass);
  }

  @Override
  public ImmutablePair<Property, Object> findField(final Mapper mapper, Object value, String name) {
    final Iterator<String> iter = Splitter.on(".").split(name).iterator();
    Mapper current = mapper;
    Property property = current.getProperty(iter.next());

    if (property == null || (property.isJson() && iter.hasNext())) {
      return null;
    }

    while (property != null && property.getTarget() != null && iter.hasNext()) {
      if (ObjectUtils.notEmpty(value)) {
        value = property.get(value);
      }
      current = Mapper.of(property.getTarget());
      property = current.getProperty(iter.next());
    }

    return ImmutablePair.of(property, value);
  }

  @Override
  public Object findNameColumn(Property targetField, Object value) {
    String nameColumn = targetField.getTargetName();
    for (Property property : Mapper.of(targetField.getTarget()).getProperties()) {
      if (nameColumn.equals(property.getName())) {
        return property.get(value);
      }
    }
    return null;
  }

  @Override
  public String getDateTimeFormat(Object value) {
    String formattedDateTime = "";
    if (value.getClass() == LocalDate.class) {
      LocalDate date = (LocalDate) value;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
      formattedDateTime = date.format(formatter);
    } else if (value.getClass() == LocalDateTime.class) {
      LocalDateTime dateTime = (LocalDateTime) value;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
      formattedDateTime = dateTime.format(formatter);
    }
    return formattedDateTime;
  }

  @Override
  public Pair<String, Boolean> checkHideWrapper(String value, Object bean) {
    boolean hide = false;
    if (value.contains(": hide(")
        || value.contains(":hide(")
        || value.contains(": show(")
        || value.contains(":show(")) {
      String conditionText =
          StringUtils.substringBetween(value.substring(value.lastIndexOf(":") + 1), "(", ")");
      String operator =
          value.substring(value.lastIndexOf(":") + 1).contains("hide") ? "hide" : "show";
      Object result = groovyService.validateCondition(conditionText, bean);

      if (ObjectUtils.notEmpty(result)
          && (result.equals(Boolean.FALSE) && operator.equalsIgnoreCase("show")
              || result.equals(Boolean.TRUE) && operator.equalsIgnoreCase("hide"))) {
        hide = true;
      }
      value = value.substring(0, value.lastIndexOf(":")).trim();
    }

    return Pair.of(value, hide);
  }

  @Override
  public Pair<String, String> checkIfElseWrapper(String value, Object bean)
      throws IOException, AxelorException {
    String operationString = "";

    if (value.startsWith("if") && value.contains("->")) {
      ImmutablePair<String, String> valueOperationPair =
          groovyService.getIfConditionResult(value, bean);

      value = valueOperationPair.getLeft();
      operationString = valueOperationPair.getRight();
    }
    return Pair.of(value, operationString);
  }

  @Override
  public int getBigDecimalScale() {
    int bigDecimalScale = appBaseService.getAppBase().getBigdecimalScale();
    if (bigDecimalScale == 0) {
      bigDecimalScale = BIG_DECIMAL_SCALE;
    }
    return bigDecimalScale;
  }
}
