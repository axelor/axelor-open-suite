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
package com.axelor.studio.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.studio.db.Filter;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

/**
 * This service class use to generate groovy expression from chart filters.
 * 
 * @author axelor
 *
 */
public class FilterService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private MetaFieldRepository metaFieldRepo;

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
		String operator = filter.getFilterOperator().getValue();
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

	public List<Object> getTargetField(MetaField metaField, String target) {

		List<Object> result = new ArrayList<Object>();
		List<String> targetList = new ArrayList<String>();
		targetList.add(metaField.getName());

		if (target != null) {
			String modelName = metaField.getTypeName();
			List<String> fields = Arrays.asList(target.split("\\."));

			if (fields.size() >= 2) {

				fields = fields.subList(1, fields.size());

				for (String field : fields) {
					MetaField subField = metaFieldRepo
							.all()
							.filter("self.name = ?1 and self.metaModel.name = ?2",
									field, modelName).fetchOne();
					if (subField == null) {
						break;
					}

					metaField = subField;

					targetList.add(field);

					if (subField.getRelationship() == null) {
						break;
					}

					if (fields.get(fields.size() - 1) == field) {
						break;
					} else {
						modelName = subField.getTypeName();
					}

				}

			}
		}

		result.add(Joiner.on(".").join(targetList));
		result.add(metaField);

		return result;
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

		return value;
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

	/**
	 * Method to generate simple query condition for non relational chart filter
	 * fields.
	 * 
	 * @param filter
	 *            Chart filter record of chart builder
	 * @return String condition
	 */
	public String getSimpleCondition(Filter filter, String paramName) {

		MetaField field = filter.getMetaField();

		String fieldName = field.getName();
		if (paramName == null) {
			paramName = fieldName;
		}
		String conditionField = "self." + fieldName;

		String value = filter.getValue();
		String[] values = new String[] { "" };
		if (value != null) {
			values = value.split(",");
		}

		String operator = filter.getFilterOperator().getValue();
		String typeName = field.getTypeName().toUpperCase();

		value = getTagValue(value, true);

		if (filter.getIsParameter()) {
			value = ":" + paramName;
			if (typeName.equals("STRING")) {
				value = "CONCAT('%',LOWER(" + value + "),'%')";
			}
		}

		switch (operator) {
			case "=":
				if (typeName.equals("STRING")) {
					return getLikeCondition(conditionField, value, true);
				}
				return conditionField + " IN" + " (" + value + ") ";
			case "!=":
				if (typeName.equals("STRING")) {
					return getLikeCondition(conditionField, value, false);
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
				operator = operator.replace("<", "&lt;");
				operator = operator.replace(">", "&gt;");
				return conditionField + " " + operator + " " + value;
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
		String fieldName = metaField.getName();
		if (paramName == null) {
			paramName = fieldName;
		}
		String conditionField = "self." + filter.getTargetField();
		String value = filter.getValue();
		String targetType = filter.getTargetType().toUpperCase();
		if (targetType == null) {
			MetaField targetField = (MetaField) getTargetField(metaField,
					filter.getTargetField()).get(1);
			if (targetField.getRelationship() != null) {
				targetType = targetField.getRelationship();
			} else {
				targetType = targetField.getTypeName();
			}
		}
		Boolean isParam = filter.getIsParameter();

		String operator = filter.getFilterOperator().getValue();

		if (isParam) {
			value = ":" + paramName;
			// conditionField = "self." + fieldName;
		}

		switch (operator) {
			case "=":
				if (targetType.equals("STRING") && !isParam) {
					return getLikeCondition(conditionField, value, true);
				}
				return conditionField + " IN (" + value + ") ";
			case "!=":
				if (targetType.equals("STRING") && !isParam) {
					return getLikeCondition(conditionField, value, false);
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
			return m2mCondition + getLikeCondition(targetName, value, true)
					+ ")";
		}

		return m2mCondition + targetName + " IN (" + value + ")" + ")";
	}

	/**
	 * It will return value of tag used in chart filter 'value'.
	 * 
	 * @param value
	 *            Value of chart filter.
	 * @return Contex variable to use instead of tag.
	 */
	public String getTagValue(String value, boolean addColon) {

		if (value != null) {
			if (addColon) {
				value = value.replace("$user", ":__user__");
				value = value.replace("$date", ":__date__");
				value = value.replace("$time", ":__datetime__");
			} else {
				value = value.replace("$user", "__user__");
				value = value.replace("$date", "__date__");
				value = value.replace("$time", "__datetime__");
			}
		}

		return value;
	}

	/**
	 * Method create like condition for chart filter with string field. Also it
	 * add LOWER function condition.
	 * 
	 * @param conditionField
	 *            Chart filter field name
	 * @param value
	 *            Value of input in chart filter.
	 * @param isLike
	 *            boolean to check if condition is like or notLike
	 * @return String condition.
	 */
	private String getLikeCondition(String conditionField, String value,
			boolean isLike) {

		String likeCondition = null;

		conditionField = "LOWER(" + conditionField + ")";

		String likeOpr = "LIKE";
		if (!isLike) {
			likeOpr = "NOT LIKE";
		}

		if (value.contains(",")) {
			for (String val : Arrays.asList(value.split(";"))) {
				if (likeCondition == null) {
					likeCondition = conditionField + " " + likeOpr + " " + val;
				} else {
					likeCondition += " OR " + conditionField + " " + likeOpr
							+ " " + val;
				}
			}
		} else {
			likeCondition = conditionField + " " + likeOpr + " " + value;
		}

		return likeCondition;
	}

}
