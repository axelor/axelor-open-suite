package com.axelor.studio.web;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.RecordImport;
import com.axelor.studio.db.repo.RecordImportRepository;
import com.axelor.studio.service.data.record.ImportService;
import com.google.inject.Inject;

public class RecordImportController {
	
	@Inject
	private RecordImportRepository recordImportRepo;
	
	@Inject
	private ImportService importService;
	
	public void importRecord(ActionRequest request, ActionResponse response) throws AxelorException {
		
		RecordImport recordImport = request.getContext().asType(RecordImport.class);
		recordImport = recordImportRepo.find(recordImport.getId());
		
		boolean imported = importService.importRecord(recordImport);
		
		if (imported) {
			response.setFlash(I18n.get("Record imported successfully"));
		}
		else {
			response.setFlash(I18n.get("Error in import, please check the 'Error log'"));
		}
		
		response.setReload(true);
		
	}
}
