package com.axelor.apps.qms.db.repo;

import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.service.QMSDocumentService;
import com.google.inject.Inject;

public class QMSDocumentManagementRepository extends QMSDocumentRepository {
	protected QMSDocumentService documentService;

	@Inject
	public QMSDocumentManagementRepository(QMSDocumentService documentService) {
		this.documentService = documentService;
	}

	@Override
	public QMSDocument save(QMSDocument entity) {
		documentService.assignReference(entity);
		return super.save(entity);
	}
}
