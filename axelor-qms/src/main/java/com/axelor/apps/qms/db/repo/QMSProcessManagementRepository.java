package com.axelor.apps.qms.db.repo;

import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.db.QMSProcess;
import com.axelor.apps.qms.service.QMSDocumentService;
import com.google.inject.Inject;

public class QMSProcessManagementRepository extends QMSProcessRepository {
	protected QMSDocumentService documentService;
	
	@Inject
	public QMSProcessManagementRepository(QMSDocumentService documentService) {
		super();
		this.documentService = documentService;
	}


	@Override
	public QMSProcess save(QMSProcess entity) {
		for(QMSDocument document : entity.getDocuments()) {
			documentService.assignReference(document);
		}
		return super.save(entity);
	}
}
