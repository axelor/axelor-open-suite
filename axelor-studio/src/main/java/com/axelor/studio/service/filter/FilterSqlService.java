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

import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.Filter;
import com.google.inject.Inject;

public class FilterSqlService {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private FilterCommonService filterCommonService;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private MetaJsonFieldRepository metaJsonFieldRepo;
	
	public String getSimpleSql(Filter filter) {

		String conditionField = null;
		String typeName =  null;
		
		if (filter.getIsJson()) {
			MetaJsonField json = filter.getMetaJsonField();
			conditionField = "cast(self." + getColumn(json.getModel(), json.getModelField()) 
							 + "->>'" + json.getName() 
							 + "' as " + getSqlType(json.getType()) + ")";
			typeName = json.getType().toUpperCase();
		}
		else {
			MetaField field = filter.getMetaField();
			conditionField = "self." + getColumn(field);
			typeName = field.getTypeName().toUpperCase();
		}
		
		
		String value = filter.getValue();
		String[] values = new String[] { "" };
		if (value != null) {
			values = value.split(",");
		}

		String operator = filter.getOperator();

		value = filterCommonService.getTagValue(value, true);

		if (filter.getIsParameter() != null && filter.getIsParameter()) {
			value = ":param" + filter.getId();
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
				return conditionField + " " + operator + " " + value;
		}

	}
	
	public String getColumn(String model, String field) {
		
		SessionImpl sessionImpl = (SessionImpl) JPA.em().getDelegate();
		AbstractEntityPersister aep=((AbstractEntityPersister)sessionImpl
					.getSession().getSessionFactory().getClassMetadata(model));
		String[] columns = aep.getPropertyColumnNames(field);
		if (columns != null && columns.length > 0) {
			return columns[0];
		}
		
		return null;
	}
	
	public String getColumn(MetaField metaField) {
		
		return getColumn(metaField.getMetaModel().getFullName(), metaField.getName());
	}
	
	public String getSqlType(String type) {
		
		switch (type) {
		case "string":
			return "varchar";
		}
		
		return type;
	}
	
	public String getRelationalSql(Filter filter, List<String> joins) throws AxelorException {
		
		Object target = null;
		StringBuilder parent = new StringBuilder("self");
		if (filter.getIsJson()) {
			target = parseJsonField(filter.getMetaJsonField(), filter.getTargetField(), joins, parent);
		}
		else {
			target = parseMetaField(filter.getMetaField(), filter.getTargetField(), joins, parent);
		}
		
		String[] field = getSqlField(target, parent.toString());
		String value = filter.getValue();
		String operator = filter.getOperator();
		
		Boolean isParam = filter.getIsParameter();
		if (isParam) {
			value = ":param" + filter.getId();
		}
		
		return getCondition(field, value, operator, isParam);

	}

	private String getCondition(String[] field, String value, String operator, Boolean isParam) {
		
		String condition = null;
		
		switch (operator) {
			case "=":
				if (field[1].equals("STRING") && !isParam) {
					condition = filterCommonService.getLikeCondition(field[0], value, true);
					break;
				}
				condition = field[0] + " IN (" + value + ") ";
				break;
			case "!=":
				if (field[1].equals("STRING") && !isParam) {
					condition = filterCommonService.getLikeCondition(field[0], value, false);
					break;
				}
				condition = field[0] + " NOT IN (" + value + ") ";
				break;
			case "isNull":
				condition =  field[0] + " IS NULL ";
				break;
			case "notNull":
				condition = field[0] + " IS NOT NULL ";
				break;
			default:
				break;
		}
		
		if (condition == null) {
			condition = field[0] + " " + operator + " (" + value + ") ";
		}
		
		return condition;
	}
	
	public String[] getSqlField(Object target, String source) {
		
		String field = null;
		String type = null;
		
		if (target instanceof MetaField) {
			MetaField metaField = (MetaField)target;
			field = source + "." + getColumn(metaField);
			type = metaField.getTypeName();
		}
		else {
			MetaJsonField metaJsonField = (MetaJsonField)target;
			String jsonColumn = getColumn(metaJsonField.getModel(), metaJsonField.getModelField());
			field = "cast(" + source + "." + jsonColumn + "->>'"
					+ metaJsonField.getName() 
					+ "' as " + getSqlType(metaJsonField.getType()) + ")";
			type = metaJsonField.getType();
		}
		
		return new String[]{field, type};
	}

	public String[] getDefaultTarget(String fieldName, String modelName) {
		
		MetaModel targetModel = null;
		if (modelName.contains(".")) {
			targetModel = metaModelRepo.all().filter("self.fullName = ?1", modelName).fetchOne();
		}
		else {
			targetModel = metaModelRepo.findByName(modelName);
		}
		
		if (targetModel == null) {
			return new String[]{fieldName, null};
		}
		
		try {
			Mapper mapper = Mapper.of(Class.forName(targetModel.getFullName()));
			if (mapper.getNameField() != null) {
				return new String[]{fieldName + "." + mapper.getNameField().getName(),
							mapper.getNameField().getJavaType().getSimpleName()};
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		for (MetaField field : targetModel.getMetaFields()) {
			if (field.getName().equals("name")) {
				return new String[]{fieldName + ".name", field.getTypeName()};
			}
			if (field.getName().equals("code")) {
				return new String[]{fieldName + ".code", field.getTypeName()};
			}
		}
		
		return new String[]{fieldName, null};
	}
	
	public String[] getDefaultTargetJson(String fieldName, MetaJsonModel targetModel) {
		
		return new String[]{fieldName + "." +  targetModel.getNameField(), "string"};
	}
	
	public Object parseMetaField(MetaField field, String target, List<String> joins, 
			StringBuilder parent) throws AxelorException {
		
		if (!target.contains(".")) {
			if (field.getRelationship() != null && joins != null) {
				target = getDefaultTarget(field.getName(), field.getTypeName())[0];
			}
			else {
				return field;
			}
		}	
		
		target = target.substring(target.indexOf(".") + 1);
		String targetName = target.contains(".") ? target.substring(0,target.indexOf(".")) : target;
		if (field.getRelationship() == null) {
			return field;
		}
		
		MetaModel model = metaModelRepo.findByName(field.getTypeName());
		MetaField subMeta = findMetaField(targetName, model.getFullName());
		if (subMeta != null) {
			if (joins != null) { addJoin(field, joins, parent); }
			return parseMetaField(subMeta, target, joins, parent);
		}
		else {
			MetaJsonField subJson = findJsonField(targetName, model.getName());
			if (subJson != null) {
				if (joins != null) { addJoin(field, joins, parent); }
				return parseJsonField(subJson, target, joins, parent);
			}
			throw new AxelorException("No sub field found field: %s model: %s ", 
					1, targetName, model.getFullName());
		}
		
	}
	
	public Object parseJsonField(MetaJsonField field, String target, List<String> joins,
			StringBuilder parent) throws AxelorException {
		
		log.debug("Parse json target: {}", target);
		
		if (!target.contains(".")) {
			if (field.getTargetJsonModel() != null && joins != null) {
				target = getDefaultTargetJson(field.getName(), field.getTargetJsonModel())[0];
			}
			else if (field.getTargetModel() != null && joins != null) {
				target = getDefaultTarget(field.getName(), field.getTargetModel())[0];
			}
			else {
				return field;
			}
		}
		
		target = target.substring(target.indexOf(".") + 1);
		
		String targetName = target.contains(".") ? target.substring(0,target.indexOf(".")) : target;
		if (field.getTargetJsonModel() == null && field.getTargetModel() == null) {
			return field;
		}
		
		if (field.getTargetJsonModel() != null) {
			MetaJsonField subJson = metaJsonFieldRepo.all()
					.filter("self.name = ?1 and self.jsonModel = ?2",
							targetName, field.getTargetJsonModel()).fetchOne();
			if (subJson != null) {
				if (joins != null) { addJoin(field, joins, parent); }
				return parseJsonField(subJson, target, joins, parent);
			}
			throw new AxelorException("No sub field found model: %s field %s ", 1,
					field.getTargetJsonModel().getName(), targetName);
		}
		else {
			MetaField subMeta = findMetaField(targetName, field.getTargetModel());
			if (subMeta != null) {
				if (joins != null) { addJoin(field, joins, parent); }
				return parseMetaField(subMeta, target, joins, parent);
			}
			throw new AxelorException("No sub field found model: %s field %s ", 1,
					field.getTargetModel(), targetName);
		}
		
	}
	
	private MetaField findMetaField(String name, String model) {
		
		return metaFieldRepo.all()
				.filter("self.name = ?1 and self.metaModel.fullName = ?2",
						name, model).fetchOne();
		
	}
	
	private MetaJsonField findJsonField(String name, String model) {
		
		return metaJsonFieldRepo.all()
				.filter("self.name = ?1 and self.model = ?2",
						name, model).fetchOne();
		
	}

	private void addJoin(MetaField field, List<String> joins, StringBuilder parent) {
		
		MetaModel metaModel = metaModelRepo.findByName(field.getTypeName());
		String parentField = getColumn(field);
		joins.add("left join " + metaModel.getTableName() + " " 
					+ "obj" + joins.size() + " on (" 
					+ "obj" + joins.size() + ".id = " 
					+ parent.toString() + "." + parentField + ")");
		parent.replace(0, parent.length(), "obj" + (joins.size() - 1));
	}
	
	private void addJoin(MetaJsonField field, List<String> joins , StringBuilder parent) {
		
		if (field.getTargetModel() != null) {
			MetaModel metaModel = metaModelRepo.all()
					.filter("self.fullName = ?1", field.getTargetModel()).fetchOne();
			joins.add("left join " + metaModel.getTableName() + " " 
					+ "obj" + joins.size() + " on (obj" + joins.size() + ".id = " 
					+ "cast(" + parent  + "." + field.getModelField() 
					+ "->'" + field.getName() +  "'->>'id' as integer))");
		}
		else if (field.getTargetJsonModel() != null) {
			joins.add("left join meta_json_record " 
					+ "obj" + joins.size() + " on (" +  "obj" + joins.size() + ".id = " 
					+ "cast(" + parent + "." + field.getModelField() 
					+  "->'" + field.getName() +"'->>'id' as integer))");
		}
		
		parent.replace(0, parent.length(), "obj" + (joins.size() - 1));
	}

}