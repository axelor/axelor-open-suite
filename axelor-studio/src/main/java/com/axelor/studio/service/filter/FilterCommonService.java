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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaField;

public class FilterCommonService {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	/**
	 * It will return value of tag used by filter 'value'.
	 * 
	 * @param value
	 *            Value of chart filter.
	 * @return Context variable to use instead of tag.
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
	 * Method create like condition for filter with string field.
	 * 
	 * @param conditionField
	 *            Chart filter field name
	 * @param value
	 *            Value of input in chart filter.
	 * @param isLike
	 *            boolean to check if condition is like or notLike
	 * @return String condition.
	 */
	public String getLikeCondition(String conditionField, String value,
			boolean isLike) {

		String likeCondition = null;

		String likeOpr = "LIKE";
		if (!isLike) {
			likeOpr = "NOT LIKE";
		}
		
		if (value.contains(",")) {
			for (String val : Arrays.asList(value.split(","))) {
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
	
	/**
	 * Get simple field type from typeName of MetaField
	 * 
	 * @param metaField
	 *            MetaField to check for typeName.
	 * @return Simple field type.
	 */
	public String getFieldType(MetaField metaField) {

		String relationship = metaField.getRelationship();

		if (relationship != null) {
			switch (relationship) {
			case "OneToMany":
				return "one-to-many";
			case "ManyToMany":
				return "many-to-many";
			case "ManyToOne":
				return "many-to-one";
			}
		}
		
		return getFieldType(metaField.getTypeName());
		
	}
	
	public String getFieldType(String type) {
		
		switch (type) {
			case "String":
				return "string";
			case "Integer":
				return "integer";
			case "Boolean":
				return "boolean";
			case "BigDecimal":
				return "decimal";
			case "Long":
				return "long";
			case "byte[]":
				return "binary";
			case "LocalDate":
				return "date";
			case "ZonedDateTime":
				return "datetime";
			case "LocalDateTime":
				return "datetime";
			default:
				return "string";
		}
		
	}
}
