package com.axelor.studio.service.data.record;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.RecordImport;
import com.axelor.studio.db.RecordImportRule;
import com.axelor.studio.db.repo.RecordImportRepository;
import com.axelor.studio.db.repo.RecordImportRuleRepository;
import com.axelor.studio.service.data.importer.DataReaderExcel;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportService {
	
	@Inject
	private RecordImportRuleRepository importRuleRepo;
	
	@Inject
	private ImportValidateService validateService;
	
	@Inject
	private RecordImportRepository recordImportRepo;
	
	@Inject
	private ImportModelService importModelService;
	
	@Transactional
	public boolean importRecord(RecordImport recordImport) throws AxelorException {
		
		DataReaderExcel reader = new DataReaderExcel();
		
		reader.initialize(recordImport.getImportFile());
		
		String[] tabNames = reader.getKeys();
		
		Map<String, RecordImportRule> ruleMap = getRuleMap(tabNames);
		
		String log = validateService.validate(ruleMap, tabNames, reader);
		
		if (log.isEmpty()) {
			for (String tab : tabNames) {
				log = importModelService.importModel(tab, ruleMap.get(tab), reader);
				if (!Strings.isNullOrEmpty(log)) {
					break;
				}
			}
		}
		
		recordImport.setResult(log);
		recordImport.setImportDate(new LocalDateTime());
		recordImport.setImportedBy(AuthUtils.getUser());
		recordImportRepo.save(recordImport);
		
		return Strings.isNullOrEmpty(log);
	}

	private Map<String, RecordImportRule> getRuleMap(String[] tabNames) throws AxelorException {
		
		Map<String, RecordImportRule> ruleMap = new HashMap<String, RecordImportRule>();
		
		for (String name : tabNames) {
			RecordImportRule rule = importRuleRepo.all()
					.filter("self.tabName = ?1 or self.metaModel.name = ?1", name)
					.order("defaultRule")
					.fetchOne();
			
			if (rule == null) {
				throw new AxelorException(I18n.get("No record import rule found for tab: %s"), 1, name);
			}
			ruleMap.put(name, rule);
		}
		
		return ruleMap;
	}
	
	

}	
