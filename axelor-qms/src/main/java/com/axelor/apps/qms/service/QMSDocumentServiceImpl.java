package com.axelor.apps.qms.service;

import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.db.QMSProcess;
import com.axelor.apps.qms.db.repo.QMSProcessRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class QMSDocumentServiceImpl implements QMSDocumentService {
	protected QMSProcessRepository processRepository;

	@Inject
	public QMSDocumentServiceImpl(QMSProcessRepository processRepository) {
		super();
		this.processRepository = processRepository;
	}

	@Override
	@Transactional
	public void addDocumentToProcess(QMSDocument document) {
		if(document.getProcesses().size() == 1 && document.getProcesses().contains(document.getProcess())) {
			return;
		}
		for(final QMSProcess process : document.getProcesses()) {
			process.removeDocument(document);
			processRepository.save(process);
		}
		document.getProcess().addDocument(document);
		processRepository.save(document.getProcess());
	}
}
