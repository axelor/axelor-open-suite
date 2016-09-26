package com.axelor.studio.service.data.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.exporter.ExportMenu;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.inject.Inject;

public class MenuValidator {
	
	private ValidatorService validatorService;
	
	private List<String> menus;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private ConfigurationService configService;
	
	public void validate(ValidatorService validatorService, DataReader reader, String key) throws IOException {
		if (validatorService == null) {
			return;
		}
		this.validatorService = validatorService;
		if (key == null || reader == null) {
			return;
		}
		
		menus = new ArrayList<String>();
		
		int totalLines = reader.getTotalLines(key);
		
		for (int rowNum = 1; rowNum < totalLines; rowNum++) {
			
			String[] row = reader.read(key, rowNum);
			if (row == null) {
				continue;
			}
			
			String module = row[ExportMenu.MODULE];
			
			if (module == null || configService.getNonCustomizedModules().contains(module)) {
				continue;
			}
			
			validateMenu(row, key, rowNum);
		}
		
	}
	
	private void validateMenu(String[] row, String key, int rowNum) throws IOException {
		
		String name = row[ExportMenu.NAME];
		String title = row[ExportMenu.TITLE];
		
		if (title == null) {
			title = row[ExportMenu.TITLE_FR];
		}
		
		if (name == null && title == null) {
			validatorService.addLog(I18n.get("Name and title is empty" ), key, rowNum);
		}
		
		String model = row[ExportMenu.OBJECT];
		if (model != null && !validatorService.isValidModel(model)) {
			validatorService.addLog(I18n.get("Invalid model" ), key, rowNum);
		}
		
		String order = row[ExportMenu.ORDER];
		if (order != null) {
			try {
				Integer.parseInt(order.trim());
			} catch(Exception e) {
				validatorService.addLog(I18n.get("Invalid menu order"), key, rowNum);
			}
		}
		
		if (name == null) {
			name = title;
		}
		
		String parent = row[ExportMenu.PARENT];
		if (parent != null && !menus.contains(parent)) {
			MetaMenu menu = metaMenuRepo.all().filter("self.name = ?1" , parent).fetchOne();
			if(menu == null){
				validatorService.addLog(I18n.get("No parent menu defined"), key, rowNum);
			}
		}
		
		menus.add(name);
		
	}
}
