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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.studio.db.Filter;
import com.google.inject.Inject;

/**
 * This service class use to generate groovy expression from chart filters.
 * 
 * @author axelor
 *
 */
public class FilterJpqlService {
	
	public static final List<String> NO_PARAMS = Arrays.asList(new String[]{"isNull","notNull", "empty", "notEmpty"});
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private FilterCommonService filterCommonService;
	


	/**
	 * Method to generate simple query condition for non relational chart filter
	 * fields.
	 * 
	 * @param filter
	 *            Chart filter record of chart builder
	 * @return String condition
	 */
	public String getSimpleCondition(Filter filter, String paramName) {

		String fieldName = null;
		String conditionField = null;
		String typeName =  null;
		
		boolean isJson = filter.getIsJson() != null && filter.getIsJson();
		MetaJsonField json = filter.getMetaJsonField();
		if (isJson) {
			fieldName = json.getModelField() + ",'" + json.getName() + "'";
			conditionField = getJsonJpql(json) + "(self." + fieldName + ")";
			typeName = json.getType().toUpperCase();
		}
		else {
			MetaField field = filter.getMetaField();
			fieldName =  field.getName();
			conditionField = "self." + fieldName;
			typeName = field.getTypeName().toUpperCase();
		}
		
		if (paramName == null) {
			paramName = fieldName;
			if (isJson) {
				paramName = json.getName();
			}
		}
		

		String value = filter.getValue();
		String[] values = new String[] { "" };
		if (value != null) {
			values = value.split(",");
		}

		String operator = filter.getOperator();

		value = filterCommonService.getTagValue(value, true);

		if (filter.getIsParameter() != null && filter.getIsParameter()) {
			value = ":" + paramName;
			if (typeName.equals("STRING")) {
				value = "CONCAT('%',LOWER(" + value + "),'%')";
			}
		}
		
		switch (operator) {
			case "=":
				if (typeName.equals("STRING")) {
					return filterCommonService.getLikeCondition(conditionField, value, true);
				}
				return conditionField + " IN" + " (" + value + ") ";
			case "!=":
				if (typeName.equals("STRING")) {
					return filterCommonService.getLikeCondition(conditionField, value, false);
				}
				return conditionField + " NOT IN" + " (" + value + ") ";
			case "isNull":
				return conditionField + " IS NULL ";
			case "notNull":
				return conditionField + " IS NOT NULL ";
			case "between":
				if (values.length > 1) {
					return conditionField + " BETWEEN  " + values[0] + " AND "
							+ values[1];
				}
				return conditionField + " BETWEEN  " + values[0] + " AND "
						+ values[0];
			case "notBetween":
				if (values.length > 1) {
					return conditionField + " NOT BETWEEN  " + values[0] + " AND "
							+ values[1];
				}
				return conditionField + " NOT BETWEEN  " + values[0] + " AND "
						+ values[0];
			case "TRUE":
				return conditionField + " IS TRUE ";
			case "FALSE":
				return conditionField + " IS FALSE ";
			default:
//				operator = operator.replace("<", "&lt;");
//				operator = operator.replace(">", "&gt;");
				return conditionField + " " + operator + " " + value;
		}

	}
	
	
	
	
	public String getJsonJpql(MetaJsonField jsonField) {
		
		switch(jsonField.getType()) {
		case "integer":
			return "json_extract_integer";
		case "decimal":
			return "json_extract_decimal";
		case "boolean":
			return "json_extract_boolean";
		 default:
			 return "json_extract";
		}
		
	}
	
	/**
	 * Method to generate query condition for relational chart filter fields.
	 * 
	 * @param filter
	 *            Chart filter record of chart builder
	 * @return String condition
	 */
	public String getRelationalCondition(Filter filter, String paramName) {

		MetaField metaField = filter.getMetaField();
		MetaJsonField metaJson = filter.getMetaJsonField();
		
		boolean json = filter.getIsJson() != null ? filter.getIsJson() : false;
		
		String fieldName = json ? metaJson.getName() : metaField.getName();
		if (paramName == null) {
			paramName = fieldName;
		}
		String conditionField = "self." + filter.getTargetField();
		if (json) {
			conditionField = "json_extract(self." + filter.getTargetField() + ")";
		}
		String value = filter.getValue();
		String targetType = filter.getTargetType().toUpperCase();
//			Object targetField = getTargetField(metaField,
//					filter.getTargetField()).get(1);
////		}
		Boolean isParam = filter.getIsParameter() != null ? filter.getIsParameter() : false;

		String operator = filter.getOperator();

		if (isParam) {
			value = ":" + paramName;
			// conditionField = "self." + fieldName;
		}
		
		switch (operator) {
			case "=":
				if (targetType.equals("STRING") && !isParam) {
					return filterCommonService.getLikeCondition(conditionField, value, true);
				}
				return conditionField + " IN (" + value + ") ";
			case "!=":
				if (targetType.equals("STRING") && !isParam) {
					return filterCommonService.getLikeCondition(conditionField, value, false);
				}
				return conditionField + " NOT IN (" + value + ") ";
			case "isNull":
				return conditionField + " IS NULL ";
			case "notNull":
				return conditionField + " IS NOT NULL ";
			case "empty":
				return "self." + fieldName + " IS EMPTY ";
			case "notEmpty":
				return "self." + fieldName + " IS NOT EMPTY ";
			case "notInclude":
				return getM2MCondition(fieldName, targetType, conditionField,
						value, false);
			case "include":
				return getM2MCondition(fieldName, targetType, conditionField,
						value, true);
			default:
				break;
		}

		return conditionField + " " + operator + " (" + value + ") ";

	}
	
	

	/***
	 * Method to get condition for chart filter with M2M field
	 * 
	 * @param field
	 *            M2M field name
	 * @param nameColumn
	 *            Array of string with first element as nameColumn field name
	 *            and second as its type.
	 * @param value
	 *            Value of M2M field
	 * @param like
	 *            boolean to check if its like or non like condition.
	 * @param notTag
	 *            boolean to check if tag used in value.
	 * @return String condition
	 */
	private String getM2MCondition(String field, String targetType,
			String targetName, String value, boolean like) {

		String m2mCondition = "EXISTS(SELECT id FROM self." + field + " WHERE ";

		if (!like) {
			m2mCondition = "NOT " + m2mCondition;
		}
		// targetName = targetName.replace("self."+field, "");

		if (targetType.equals("STRING")) {
			return m2mCondition + filterCommonService.getLikeCondition(targetName, value, true)
					+ ")";
		}

		return m2mCondition + targetName + " IN (" + value + ")" + ")";
	}


	
	public String getJpqlFilters(List<Filter> filterList) {

		String filters = null;
		
		if (filterList == null) {
			return filters;
		}

		for (Filter filter : filterList) {

			MetaField field = filter.getMetaField();

			String relationship = field.getRelationship();
			String condition = "";

			if (relationship != null) {
				condition = getRelationalCondition(filter, null);
			} else {
				condition = getSimpleCondition(filter, null);
			}

			if (filters == null) {
				filters = condition;
			} else {
				String opt = filter.getLogicOp() != null && filter.getLogicOp() == 0 ? " AND " : " OR ";
				filters = filters + opt + condition;
			}
		}
		
		log.debug("JPQL filter: {}", filters);
		return filters;

	}
	

}
