/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.filter;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaField;
import com.axelor.studio.db.Filter;
import com.google.inject.Inject;

public class FilterGroovyService {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private FilterCommonService filterCommonService;

	/**
	 * Method to convert chart filter list to groovy expression string. Each
	 * filter of list will be joined by logical operator(logicalOp) selected.
	 * 
	 * @param conditions
	 *            List for chart filters.
	 * @param parentField
	 *            Field that represent parent.
	 * @return Groovy expression string.
	 */
	public String getGroovyFilters(List<Filter> conditions, String parentField) {

		String condition = null;

		if (conditions == null) {
			return null;
		}

		for (Filter filter : conditions) {
			String activeFilter = createGroovyFilter(filter, parentField);
			log.debug("Active filter: {}", filter);

			if (condition == null) {
				condition = "(" + activeFilter;
			} else if (filter.getLogicOp() > 0) {
				condition += ") || (" + activeFilter;
			} else {
				condition += " && " + activeFilter;
			}
		}

		if (condition == null) {
			return null;
		}

		return condition + ")";
	}

	/**
	 * Method to generate groovy expression for a single chart filter.
	 * 
	 * @param chartFilter
	 *            Chart filter to use .
	 * @param parentField
	 *            Parent field.
	 * @return Groovy expression string.
	 */
	private String createGroovyFilter(Filter filter, String parentField) {

		MetaField metaField = filter.getMetaField();
		String field = metaField.getName();
		String targetField = filter.getTargetField();
		if (parentField != null) {
			field = parentField + "." + field;
			if (targetField != null) {
				targetField = parentField + "." + targetField;
			}
		}

		String value = processValue(filter, metaField.getTypeName());
		String operator = filter.getOperator();
		String relationship = metaField.getRelationship();

		if (relationship != null && targetField != null) {
			targetField = targetField.replace(".", "?.");
			if (relationship.equals("ManyToOne")) {
				field = targetField;
			} else if (relationship.equals("ManyToMany")
					&& !operator.contains("mpty")) {
				targetField = targetField.replace(field + "?.", "it?.");
				String condition = getConditionExpr(operator, targetField,
						value);
				return field + ".findAll{it->" + condition + "}.size() > 0";
			}
		}

		return getConditionExpr(operator, field, value);

	}

	
	private String processValue(Filter filter, String typeName) {

		String value = filter.getValue();
		if (value == null) {
			return value;
		}

		String targetType = filter.getTargetType();
		if (targetType != null) {
			typeName = targetType;
		}

		value = value.replace("$$", "_parent.");

		return filterCommonService.getTagValue(value,false);
	}

	private String getConditionExpr(String operator, String field, String value) {

		switch (operator) {
			case "=":
				return field + " == " + value;
			case "isNull":
				return field + " == null";
			case "notNull":
				return field + " != null";
			case "empty":
				return field + ".empty";
			case "notEmpty":
				return "!" + field + ".empty";
			case "TRUE":
				return field;
			case "FALSE":
				return "!" + field;
			default:
				return field + " " + operator + " " + value;

		}

	}
}
