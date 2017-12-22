package com.axelor.apps.qms.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.qms.db.repo.QMSDocumentManagementRepository;
import com.axelor.apps.qms.db.repo.QMSDocumentRepository;
import com.axelor.apps.qms.service.ImprovementFormService;
import com.axelor.apps.qms.service.ImprovementFormServiceImpl;

public class QMSModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ImprovementFormService.class).to(ImprovementFormServiceImpl.class);
		bind(QMSDocumentRepository.class).to(QMSDocumentManagementRepository.class);
	}

}
