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
package com.axelor.studio.service.data.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.DataManager;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.validator.ValidatorService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportService extends CommonService {

	private final static Logger log = LoggerFactory
			.getLogger(ImportService.class);
	
	private Map<String, Map<String, List<String>>> moduleMap;

	private Map<String, Map<String, List<MetaField>>> gridViewMap;
	
	private Map<Long, Integer> fieldSeqMap;
	
	private Map<String, MetaModel> nestedModels;
	
	private boolean replace = false;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private ValidatorService validatorService;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private ViewItemRepository viewItemRepo;
	
	@Inject
	private ActionBuilderRepository actionBuilderRepo;
	
	@Inject
	private ImportModule importModule;
	
	@Inject
	private ImportForm importForm;
	
	@Inject
	private ImportMenu importMenu;
	
	@Inject
	private ImportGrid importGrid;
	
	@Inject
	private ImportModel importModel;
	
	/**
	 * Root method to access the service. It will call other methods required to
	 * import model.
	 * 
	 * @param modelImporter
	 *            ModelImporter class that store all import meta file.
	 * @return Return true if import done successfully else false.
	 * @throws AxelorException 
	 */
	public File importData(DataManager dataManager) throws AxelorException {
		
		File inputFile = MetaFiles.getPath(dataManager.getMetaFile())
				.toFile();

		if (!inputFile.exists()) {
			throw new AxelorException(I18n.get("Input file not exist"), 4);
		}

		moduleMap = new HashMap<String, Map<String,List<String>>>();
		fieldSeqMap = new HashMap<Long, Integer>();
		gridViewMap = new HashMap<String, Map<String,List<MetaField>>>();
		nestedModels = new HashMap<String, MetaModel>();

		try {
			
			FileInputStream fis = new FileInputStream(inputFile);
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			
			File logFile = validatorService.validate(workBook);
			if(logFile != null){
				return logFile;
			}
			
			importForm.clear();
			
			XSSFSheet sheet = workBook.getSheet("Modules");
			importModule.createModules(sheet);

			processSheets(workBook.iterator());
			
			sheet = workBook.getSheet("Menu");
			importMenu.importMenus(sheet);
			
			generateGridView();

		} catch (IOException e) {
			throw new AxelorException(e, 5);
		}
		
		return null;

	}

	/**
	 * Method create list of MetaModels by reading given input file.
	 * 
	 * @param inputFile
	 *            Data input file.
	 * @return List of MetaModels created.
	 * @throws IOException
	 *             Exception in file handling.
	 * @throws AxelorException 
	 */
	private void processSheets(Iterator<XSSFSheet> sheetIter) throws IOException, AxelorException {

		if (!sheetIter.hasNext()) {
			return;
		}
		
		XSSFSheet sheet = sheetIter.next();
		String name = sheet.getSheetName(); 
		if (!name.equals("Modules") && !name.equals("Menu")) {
			log.debug("Importing sheet: {}", name);
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			extractRow(rowIter);
		}
		
		processSheets(sheetIter);
	}
	
	/**
	 * Create MetaModel from data sheet data.
	 * 
	 * @param sheet
	 *            Excel sheet to process
	 * @return MetaModel created
	 * @throws AxelorException 
	 */

	private void extractRow(Iterator<Row> rowIter) throws AxelorException {

		if (!rowIter.hasNext()) {
			return;
		}
		
		replace = true;
		Row row = rowIter.next();
		String module = getValue(row, MODULE);
		if (module == null) {
			extractRow(rowIter);
			return;
		}
		
		if (module.startsWith("*")) {
			replace = false;
			module = module.replace("*", "");
		}
		
		MetaModule metaModule = getModule(module, getValue(row, IF_MODULE));
		if (metaModule == null) {
			extractRow(rowIter);
			return;
		}
		
		importModel.importModel(this, row, metaModule);

		extractRow(rowIter);
	}
	
    public MetaModule getModule(String module, String checkModule) {
		
		List<String> nonCustomModules = configService.getNonCustomizedModules();
		
		if (module == null || nonCustomModules.contains(module)) {
			return null;
		}
		
		if (checkModule != null && !nonCustomModules.contains(checkModule)) {
			return configService.getCustomizedModule(checkModule);
		}
		
		MetaModule metaModule = configService.getCustomizedModule(module);
		
		if (metaModule  != null && checkModule != null) {
			String depends = metaModule.getDepends();
			if (depends != null && Arrays.asList(depends.split(",")).contains(checkModule)) {
				return metaModule;
			}
			return null;
		}
		
		return metaModule;
    }
    
    
    @Transactional
    public void generateGridView() throws AxelorException {
		
    	Map<String, List<ActionBuilder>> actionViewMap = importForm.getViewActionMap();
		
		for (String module : moduleMap.keySet()) {
			for (String modelName : moduleMap.get(module).keySet()) {
	 			MetaModel model = metaModelRepo.findByName(modelName);
	 			if (model != null && !model.getCustomised()) {
	 				continue;
	 			}
				model = clearModel(module, model);
				if (model == null || moduleMap.get(module).get(modelName).size() < 1) {
					continue;
				}
				if (gridViewMap.containsKey(module)) {
					List<MetaField> fields = gridViewMap.get(module).get(model);
					ViewBuilder viewBuilder = importGrid.createGridView(getModule(module, null), model, fields);
					if (actionViewMap.containsKey(viewBuilder.getName())) {
						
						for (ActionBuilder builder : actionViewMap.get(viewBuilder.getName())) {
							builder.setViewBuilder(viewBuilder);
							builder.setMetaModel(viewBuilder.getMetaModel());
							actionBuilderRepo.save(builder);
						}
						
						actionViewMap.remove(viewBuilder.getName());
					}
				}
			}
		}
		
		if (!actionViewMap.isEmpty()) {
			throw new AxelorException(I18n.get("Views not found: %s"), 1,
					importForm.getViewActionMap().keySet());
		}

	}
    

	@Transactional
	public MetaModel clearModel(String module, MetaModel model) {

		List<String> fieldNames = moduleMap.get(module).get(model);
		
		if (fieldNames == null) {
			return model;
		}
		
		Iterator<MetaField> fieldIter = model.getMetaFields().iterator();
		
		while (fieldIter.hasNext()) {
			MetaField field = fieldIter.next();
			if (field.getCustomised() && !fieldNames.contains(field.getName())) {
				log.debug("Removing field : {}", field.getName());
				List<ViewItem> viewItems = viewItemRepo.all().filter("self.metaField = ?1", field).fetch();
				for (ViewItem viewItem : viewItems) {
					viewItemRepo.remove(viewItem);
				}
				fieldIter.remove();
			}
		}
		
		return metaModelRepo.save(model);
	}
    
    public void addNestedModel(String name, MetaModel metaModel) {
    	
    	if (!nestedModels.containsKey(name)) {
    		nestedModels.put(name, metaModel);
    	}
    }
    
    public MetaModel getNestedModels(String name) {
    	
    	return nestedModels.get(name);
    }
    
    public void updateModuleMap(String module, String model, String field) {
    	
    	if (!moduleMap.containsKey(module)) {
    		moduleMap.put(module, new HashMap<String, List<String>>());
    	}
    	
    	if (!moduleMap.get(module).containsKey(model)) {
    		moduleMap.get(module).put(model, new ArrayList<String>());
    	}
    	
    	if (field != null) {
    		moduleMap.get(module).get(model).add(field);
    	}
    }
    
    public void addView(MetaModel model, String[] basic, Row row, MetaField field) throws AxelorException {
    	
    	importForm.importForm(model, basic, row, field, replace);
    }
    
    public Integer getFieldSeq(Long modelId) {
    	
    	Integer seq = 0;
    	if (fieldSeqMap.containsKey(modelId)) {
    		seq = fieldSeqMap.get(modelId) + 1;
    	}
    	
    	fieldSeqMap.put(modelId, seq);
    	
    	return seq;
    }
    
    
    public void addGridField(String module, String model, MetaField metaField, String addGrid) {
		
		Map<String, List<MetaField>> gridMap = null; 
		if (!gridViewMap.containsKey(module)) {
			gridViewMap.put(module, new HashMap<String, List<MetaField>>());
		}
		gridMap = gridViewMap.get(module);
		if (!gridMap.containsKey(model)) {
			gridMap.put(model, new ArrayList<MetaField>());
		}
		
		if (addGrid != null && addGrid.equalsIgnoreCase("x")) {
			gridMap.get(model).add(metaField);
		}
		
		gridViewMap.put(module, gridMap);
		
	}
	
}
