package com.axelor.studio.service.data.importer;

import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.service.ConfigurationService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ModuleImporter {
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	@Transactional
	public void createModules(DataReader reader, String key) {

		if (key == null || reader == null) {
			return;
		}
		
		int totalLines = reader.getTotalLines(key);
		
		for (int rowNum = 0; rowNum < totalLines; rowNum++) {
			
			if (rowNum == 0) {
				continue;
			}
			
			String[] row = reader.read(key, rowNum);
			if (row == null) {
				continue;
			}
			
			String name = row[0];
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
			module.setDepends(row[1]);
			module.setTitle(row[2]);
			module.setModuleVersion(row[3]);
			module.setDescription(row[4]);
			module.setCustomised(true);
			
			metaModuleRepo.save(module);
		}
		
	}
}
