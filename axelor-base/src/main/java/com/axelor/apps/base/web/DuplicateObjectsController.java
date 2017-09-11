/**
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.DuplicateObjectsService;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;


public class DuplicateObjectsController {
	
	@Inject
	private DuplicateObjectsService duplicateObjectService;
	
	public void removeDuplicate(ActionRequest request, ActionResponse response) {
		List<Long> selectedIds = new ArrayList<>();
		String originalId = ((Map)request.getContext().get("originalObject")).get("recordId").toString();
		selectedIds.add(Long.parseLong(originalId));
		List<Map<String,Object>> duplicateObjects = (List<Map<String, Object>>) request.getContext().get("duplicateObjects");
		for(Map map : duplicateObjects) {
			if(!map.get("recordId").toString().equals(originalId)) {
				selectedIds.add(Long.parseLong(map.get("recordId").toString()));
			}
		}
		String model = request.getContext().get("_modelName").toString();
		String modelName = model.substring(model.lastIndexOf(".")+1, model.length());
		duplicateObjectService.removeDuplicate(selectedIds, modelName);
		response.setCanClose(true);
	}
	
	public void defaultObjects(ActionRequest request, ActionResponse response) throws NoSuchFieldException, SecurityException {
		List<Long> selectedIds = new ArrayList<>();
		List<Object[]> duplicateObjects = new ArrayList<>();
		List<Wizard> wizardDataList = new ArrayList<>();
		for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
			selectedIds.add(Long.parseLong("" + id));
		}
		String modelName = request.getContext().get("_modelName").toString();
		List<Object> duplicateObj= duplicateObjectService.getAllSelectedObject(selectedIds, modelName);
		
		for(Object object : duplicateObj) {
			Long id = (Long) Mapper.of(object.getClass()).get(object, "id"); 
			Property propertyNameColumn = Mapper.of(object.getClass()).getNameField();
			String nameColumn = propertyNameColumn == null ? null : propertyNameColumn.getName().toString();
			Property propertyCode = Mapper.of(object.getClass()).getProperty("code");
			String code = propertyCode == null ? null : propertyCode.getName().toString();
			String noColumn = null;
			if(nameColumn != null) {
				duplicateObjects.add((Object[]) duplicateObjectService.getWizardValue(id, modelName, nameColumn));
			} else if(code != null) {
				duplicateObjects.add((Object[]) duplicateObjectService.getWizardValue(id, modelName, code));	
			} else {
				Object obj = duplicateObjectService.getWizardValue(id, modelName, noColumn);
				Wizard wizard = new Wizard();
				wizard.setRecordId(obj.toString());
				wizard.setName(obj.toString());
				wizardDataList.add(wizard);
			}
		}
		for(Object[] obj : duplicateObjects) {
			String recordName = obj[1].toString();
			String recordId = obj[0].toString();
			Wizard wizard = new Wizard();
			wizard.setRecordId(recordId);
			wizard.setRecordName(recordName);
			wizard.setName(recordName);
			wizardDataList.add(wizard);
			
		}
		
		response.setAttr("$duplicateObjects", "value", wizardDataList);
		
	}
	
	public void addOriginal(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		List<Map<String, Object>> duplicateObj = (List<Map<String, Object>>) context.get("duplicateObjects");
		Object originalObj = null;
		Object original = "";
		boolean flag = false;
		for (Map map : duplicateObj) {
			if ((boolean) map.get("selected")) {
				originalObj = context.get("originalObject");
				response.setAttr("$originalObject", "value", map);
				original = map;
				flag = true;
			} 
		}
		if(!flag) {
			response.setAlert("Please select original object");
  		}
		duplicateObj.remove(original);
		if(originalObj != null) {
			duplicateObj.add((Map<String, Object>) originalObj);
		}
		
		response.setAttr("$duplicateObjects", "value", duplicateObj);
	}
}
