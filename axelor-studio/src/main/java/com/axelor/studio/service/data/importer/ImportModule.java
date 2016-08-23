package com.axelor.studio.service.data.importer;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.CommonService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportModule extends CommonService {
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	@Transactional
	public void createModules(XSSFSheet sheet) {
		if (sheet == null) {
			return;
		}
		
		Iterator<Row> rowIterator = sheet.rowIterator();
		
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0) {
				continue;
			}
			
			String name = getValue(row, 0);
			if (name == null) {
				continue;
			}
			if (configService.getNonCustomizedModules().contains(name)) {
				continue;
			}
			MetaModule module = configService.getCustomizedModule(name);
			if (module == null) {
				module = new MetaModule(name);
			}
			module.setDepends(getValue(row, 1));
			module.setTitle(getValue(row, 2));
			module.setModuleVersion(getValue(row, 3));
			module.setDescription(getValue(row, 4));
			module.setCustomised(true);
			
			metaModuleRepo.save(module);
		}
		
	}
}
