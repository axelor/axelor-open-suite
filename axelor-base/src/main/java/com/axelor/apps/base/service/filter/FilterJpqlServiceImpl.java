/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.filter;

import com.axelor.apps.base.db.Filter;
import com.axelor.meta.db.MetaField;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterJpqlServiceImpl implements FilterJpqlService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public String getJpqlFilters(List<Filter> filterList) {

    if (filterList == null) {
      return null;
    }

    StringBuilder filters = null;

    for (Filter filter : filterList) {

      MetaField field = filter.getMetaField();

      if (filter.getValue() != null) {
        String value = filter.getValue();
        value = value.replace("\"", "");
        value = value.replace("'", "");

        if (filter.getOperator().contains("like") && !value.contains("%")) {
          value = "%" + value + "%";
        }
        filter.setValue("'" + value + "'");
      }
      String fieldName =
          field.getRelationship() != null ? filter.getTargetField() : field.getName();
      fieldName = "self." + fieldName;
      String fieldValue;
      if (filter.getTargetType().equals("String")) {
        fieldName = "LOWER(" + fieldName + ")";
        fieldValue = "LOWER(" + filter.getValue() + ")";
      } else {
        fieldValue = filter.getValue();
      }
      String condition = getCondition(fieldName, filter.getOperator(), fieldValue);

      if (filters == null) {
        filters = new StringBuilder(condition);
      } else {
        String opt = filter.getLogicOp() != null && filter.getLogicOp() == 0 ? " AND " : " OR ";
        filters.append(opt).append(condition);
      }
    }

    log.debug("JPQL filter: {}", filters);
    return filters != null ? filters.toString() : null;
  }

  protected String getCondition(String conditionField, String operator, String value) {

    value = getTagValue(value);

    String[] values = new String[] {""};
    if (value != null) {
      values = value.split(",");
    }

    return switch (operator) {
      case "like" -> getLikeCondition(conditionField, value, true);
      case "notLike" -> getLikeCondition(conditionField, value, false);
      case "in" -> conditionField + " IN" + " (" + value + ") ";
      case "notIn" -> conditionField + " NOT IN" + " (" + value + ") ";
      case "isNull" -> conditionField + " IS NULL ";
      case "notNull" -> conditionField + " IS NOT NULL ";
      case "between" -> getBetweenCondition(conditionField, values, "BETWEEN");
      case "notBetween" -> getBetweenCondition(conditionField, values, "NOT BETWEEN");
      case "isTrue" -> conditionField + " IS TRUE ";
      case "isFalse" -> conditionField + " IS FALSE ";
      default -> conditionField + " " + operator + " " + value;
    };
  }

  /** Replaces the $user, $date and $time tags by the matching query parameters. */
  protected String getTagValue(String value) {
    if (value == null) {
      return null;
    }
    return value
        .replace("$user", ":__user__")
        .replace("$date", ":__date__")
        .replace("$time", ":__datetime__");
  }

  protected String getLikeCondition(String conditionField, String value, boolean isLike) {

    String likeOperator = isLike ? "LIKE" : "NOT LIKE";

    StringBuilder likeCondition = new StringBuilder();
    for (String val : value.split(",")) {
      if (!likeCondition.isEmpty()) {
        likeCondition.append(" OR ");
      }
      likeCondition.append(conditionField).append(" ").append(likeOperator).append(" ").append(val);
    }

    return likeCondition.toString();
  }

  protected String getBetweenCondition(String conditionField, String[] values, String operator) {
    String upperValue = values.length > 1 ? values[1] : values[0];
    return conditionField + " " + operator + "  " + values[0] + " AND " + upperValue;
  }
}
