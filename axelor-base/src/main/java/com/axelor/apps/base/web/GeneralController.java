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
package com.axelor.apps.base.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.General;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.ExportDbObjectService;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class GeneralController {

	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);

	@Inject
	private ExportDbObjectService eos;
	
	@Inject
	private MapService mapService;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;

	public Set<MetaField> setFields(String model) throws IOException {
		LOG.debug("Model: {}",model);
		Set<MetaField> fieldSet = new HashSet<MetaField>();
		List<String> fields = new ArrayList<String>();

		MetaFieldRepository metaFieldRepository = Beans.get(MetaFieldRepository.class);

		for(MetaField field : metaFieldRepository.all().filter("metaModel.fullName = ?1 AND (relationship = null OR relationship = 'ManyToOne')",model).fetch()){
			fieldSet.add(field);
			fields.add(field.getName());
		}

		LOG.debug("Fields set: {}",fields);
		return fieldSet;
	}

	@SuppressWarnings("unchecked")
	public void showDuplicate(ActionRequest request, ActionResponse response){
		
		String model = (String)request.getContext().get("object");
		List<String> fields = new ArrayList<String>();
		
		if(model == null){
			model = request.getModel();
			String searchFields = (String)request.getContext().get("searchFields");
			if(searchFields != null){
				fields.addAll(Arrays.asList(searchFields.split(";")));
			}
		}else{
			List<HashMap<String,Object>> fieldsSet = (List<HashMap<String,Object>>)request.getContext().get("fieldsSet");
			for(HashMap<String,Object> field : fieldsSet){
				if(field.get("selected") != null && (Boolean)field.get("selected")){
					MetaField metaField = metaFieldRepo.find(Long.parseLong(field.get("id").toString()));
					fields.add(metaField.getName());
				}
			}
		}
		
		LOG.debug("Duplicate record model: {}",model);
		
		if(fields.size() > 0){
			LOG.debug("Duplicate record joinList: {}", fields);
			String ids = findDuplicateRecords(fields,model);
			if(ids.isEmpty())
				response.setFlash(I18n.get(IExceptionMessage.GENERAL_1));
			else{
				response.setView(ActionView
						  .define(I18n.get(IExceptionMessage.GENERAL_2))
						  .model(model)
						  .domain("self.id in ("+ids+")")
						  .map());
				response.setCanClose(true);
			}
		}
		else
			response.setFlash(I18n.get(IExceptionMessage.GENERAL_3));
	}

	public void exportObjects(ActionRequest request, ActionResponse response){
		MetaFile metaFile = eos.exportObject();
		if(metaFile == null){
			response.setFlash(I18n.get(IExceptionMessage.GENERAL_4));
		}
		else {
			response.setView(ActionView
					  .define(I18n.get(IExceptionMessage.GENERAL_5))
					  .model("com.axelor.meta.db.MetaFile")
					  .add("form", "meta-files-form")
					  .add("grid", "meta-files-grid")
					  .param("forceEdit", "true")
					  .context("_showRecord", metaFile.getId().toString())
					  .map());
		}
	}
	
	private String findDuplicateRecords(List<String> fieldList,String object){
		
		
		List<List<String>> allRecords = getAllRecords(fieldList, object);
		
		Map<String, List<String>> recordMap = new HashMap<String, List<String>>();
		
		for(List<String> rec : allRecords){
			
			List<String> record = new ArrayList<String>();
			for(String field : rec) {
				if(field != null){
					record.add(StringTool.deleteAccent(field.toLowerCase()));
				}
			}
			
			String recId = record.get(0);
			record.remove(0);
			if(!record.isEmpty()){
				recordMap.put(recId, record);
			}
		}
		
		Iterator<String> keys = recordMap.keySet().iterator();
		
		List<String> ids = getDuplicateIds(keys, recordMap, new ArrayList<String>());

		return Joiner.on(",").join(ids);
	}
	
	private List<List<String>> getAllRecords(List<String> fieldList,String object){
		
		String query = "SELECT new List( CAST ( m.id AS string )";
		
		for(String field : fieldList){
			query += ", m."+field;
		}
		
		List<List<Object>> resultList = JPA.em().createQuery(query + ") FROM "+ object + " m").getResultList();
		
		List<List<String>> records = new ArrayList<List<String>>();
		
		
		for(List<Object> result : resultList){
			
			List<String> record = new ArrayList<String>();
			for(Object field : result){
				if(field == null){
					continue;
				}
				if(field instanceof Model){
					record.add(((Model)field).getId().toString());
				}
				else{
					record.add(field.toString());
				}
			}
			
			records.add(record);
		}
		
		return records;
	}
	
	private List<String> getDuplicateIds(Iterator<String> keys, Map<String, List<String>> recordMap, List<String> ids){
		
		if(!keys.hasNext()){
			return ids;
		}
		
		String recId = keys.next();
		List<String> record = recordMap.get(recId);
		keys.remove();
		recordMap.remove(recId);
		
		Iterator<String> compareKeys = recordMap.keySet().iterator();
		
		while(compareKeys.hasNext()){
			String compareId = compareKeys.next();
			List<String> value = recordMap.get(compareId);
			if(value.containsAll(record)){
				ids.add(recId);
				ids.add(compareId);
				compareKeys.remove();
				recordMap.remove(compareId);
				keys = recordMap.keySet().iterator();
			}
		}
		
		return getDuplicateIds(keys, recordMap, ids);
	}
	
	public void checkMapApi(ActionRequest request, ActionResponse response)  {
		
		General general = request.getContext().asType(General.class);;
		
		boolean connected = false;
		
		Integer apiType = general.getMapApiSelect();
		
		if(apiType == 1){
			connected = mapService.testGMapService();
		}
		
		if(connected){
			response.setFlash(IExceptionMessage.GENERAL_6);
		}
		else{
			response.setFlash(IExceptionMessage.GENERAL_7);
		}
	}

}
