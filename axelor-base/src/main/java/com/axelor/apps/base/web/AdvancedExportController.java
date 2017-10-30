/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.AdvancedExportLineRepository;
import com.axelor.apps.base.service.AdvancedExportService;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.itextpdf.text.DocumentException;

public class AdvancedExportController {
	
	@Inject
	private AdvancedExportService advancedExportService;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private AdvancedExportLineRepository advancedExportLineRepo;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	private Inflector inflector;
	
	public void getModelAllFields(ActionRequest request, ActionResponse response) throws ClassNotFoundException {
		
		AdvancedExport advancedExport = request.getContext().asType(AdvancedExport.class);
		inflector = Inflector.getInstance();

		if (advancedExport.getMetaModel() != null) {
			
			List<Map<String, Object>> allFieldList = new ArrayList<>();
			
			for (MetaField fields : advancedExport.getMetaModel().getMetaFields()) {
				
				Map<String, Object> allFieldMap = new HashMap<>();
				allFieldMap.put("currentDomain", advancedExport.getMetaModel().getName());
				
				if (!Strings.isNullOrEmpty(fields.getRelationship())) {
					
					MetaModel metaModel = metaModelRepo.all().filter("self.name = ?", fields.getTypeName()).fetchOne();
					
					Class<?> klass = Class.forName(metaModel.getFullName());
					Mapper mapper = Mapper.of(klass);
					
					String fieldName = mapper.getNameField() == null ? "id" : mapper.getNameField().getName();
					MetaField metaField = metaFieldRepo.all().filter("self.name = ?1 AND self.metaModel = ?2", fieldName, metaModel).fetchOne();
					
					allFieldMap.put("metaField", metaField);
					allFieldMap.put("targetField", fields.getName() + "." + metaField.getName());
					
				} else {
					allFieldMap.put("metaField", fields);
					allFieldMap.put("targetField", fields.getName());
				}
				
				if (Strings.isNullOrEmpty(fields.getLabel())) {
					allFieldMap.put("title", this.getFieldTitle(inflector, fields.getName()));
				} else {
					allFieldMap.put("title", fields.getLabel());
				}
				allFieldList.add(allFieldMap);
			}
			response.setAttr("advancedExportLineList", "value", allFieldList);
		}
	}
	
	private String getFieldTitle(Inflector inflector, String fieldName) {
		return inflector.humanize(fieldName);
	}
	
	@SuppressWarnings("deprecation")
	public void fillTargetField(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		MetaModel parentMetaModel = (MetaModel) context.getParentContext().get("metaModel");
		
		MetaField metaField = (MetaField) context.get("metaField");
		
		if (metaField != null) {
			
			String targetField = "";
			if (context.get("targetField") == null) {
				targetField = metaField.getName();
			} else {
				targetField = advancedExportService.getTargetField(context, metaField, targetField, parentMetaModel);
			}
			response.setValue("targetField", targetField);
	
			if (metaField.getRelationship() != null) {
				response.setValue("currentDomain", metaField.getTypeName());
				response.setValue("metaField", "");
			}
			
			if (Strings.isNullOrEmpty(metaField.getLabel())) {
				inflector = Inflector.getInstance();
				response.setValue("title", this.getFieldTitle(inflector, metaField.getName()));
			} else {
				response.setValue("title", metaField.getLabel());
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void showAdvancedExportData(ActionRequest request, ActionResponse response) throws ClassNotFoundException {
		
		List<Map> allDataList = new ArrayList<>();
		
		List<Map<String, Object>> advancedExportLines = (List<Map<String, Object>>) request.getData().get("advancedExportLineList");
		
		if (advancedExportLines != null && advancedExportLines.size() > 0) {
			MetaModel metaModel = metaModelRepo.find(Long.parseLong(((Map) request.getData().get("metaModel")).get("id").toString()));
			allDataList = advancedExportService.showAdvancedExportData(advancedExportLines, metaModel);
			response.setData(allDataList);
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void advancedExportPDF(ActionRequest request, ActionResponse response) throws DocumentException, IOException, ClassNotFoundException {
		
		AdvancedExport advancedExport = request.getContext().asType(AdvancedExport.class);
		MetaFile exportFile = advancedExport.getAdvancedExportFile();
		
		List<Map> allDataList = new ArrayList<>();
		List<Map<String, Object>> advancedExportLineList = new ArrayList<>();
		
		List<AdvancedExportLine> advancedExportLines = advancedExportLineRepo.all().filter("self.advancedExport = ?", advancedExport).fetch();
		Collections.sort(advancedExportLines, (line1, line2) -> line1.getSequence() - line2.getSequence());

		if (advancedExportLines != null) {
			
			for (AdvancedExportLine advancedExportLine : advancedExportLines) {
				Map<String, Object> fieldMap = new HashMap<>();
				fieldMap.put("id", advancedExportLine.getId());
				advancedExportLineList.add(fieldMap);
			}
			
			if (advancedExportLineList.size() > 0) {
				
				MetaModel metaModel = (MetaModel) request.getContext().get("metaModel");
				allDataList = advancedExportService.showAdvancedExportData(advancedExportLineList, metaModel);
				
				exportFile = advancedExportService.advancedExportPDF(exportFile, advancedExportLineList, allDataList, metaModel);
			}
		}
		response.setValue("advancedExportFile", exportFile);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void advancedExportExcel(ActionRequest request, ActionResponse response) throws IOException, ClassNotFoundException {
		
		AdvancedExport advancedExport = request.getContext().asType(AdvancedExport.class);
		MetaFile exportFile = advancedExport.getAdvancedExportFile();
		
		List<Map> allDataList = new ArrayList<>();
		List<Map<String, Object>> advancedExportLineList = new ArrayList<>();
		
		List<AdvancedExportLine> advancedExportLines = advancedExportLineRepo.all().filter("self.advancedExport = ?", advancedExport).fetch();
		Collections.sort(advancedExportLines, (line1, line2) -> line1.getSequence() - line2.getSequence());
		
		if (advancedExportLines != null) {
			
			for (AdvancedExportLine advancedExportLine : advancedExportLines) {
				Map<String, Object> fieldMap = new HashMap<>();
				fieldMap.put("id", advancedExportLine.getId());
				advancedExportLineList.add(fieldMap);
			}
			
			if (advancedExportLineList.size() > 0) {
				
				MetaModel metaModel = (MetaModel) request.getContext().get("metaModel");
				allDataList = advancedExportService.showAdvancedExportData(advancedExportLineList, metaModel);
				
				exportFile = advancedExportService.advancedExportExcel(exportFile, metaModel, allDataList, advancedExportLineList);
			}
		}
		response.setValue("advancedExportFile", exportFile);
	}
}
