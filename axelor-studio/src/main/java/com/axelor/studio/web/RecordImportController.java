package com.axelor.studio.web;

import org.joda.time.LocalDateTime;

import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.RecordImportWizard;
import com.axelor.studio.db.repo.RecordImportWizardRepository;
import com.axelor.studio.service.data.importer.DataReader;
import com.axelor.studio.service.data.importer.ExcelReader;
import com.axelor.studio.service.data.record.RecordImporterService;
import com.google.inject.Inject;

public class RecordImportController {
	
	@Inject
	private RecordImportWizardRepository importWizardRepo;
	
	@Inject
	private RecordImporterService importService;
	
	public void importRecord(ActionRequest request, ActionResponse response) {
		
		RecordImportWizard importWizard = request.getContext().asType(RecordImportWizard.class);
		importWizard = importWizardRepo.find(importWizard.getId());
		
		DataReader reader = new ExcelReader();
		reader.initialize(importWizard.getImportFile());
		
		String msg = I18n.get("Records imported successfully");
		try {
			importService.importRecords(reader, importWizard.getImportFile());
		} catch (AxelorException e) {
			msg = e.getMessage();
		}
		
		response.setFlash(msg);
		response.setValue("importLog", importService.getLog());
		response.setValue("importedBy", AuthUtils.getUser());
		response.setValue("importDate", new LocalDateTime());
		
	}
}
