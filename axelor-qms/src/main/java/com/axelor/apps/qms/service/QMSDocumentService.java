package com.axelor.apps.qms.service;

import com.axelor.apps.qms.db.QMSDocument;

public interface QMSDocumentService {
	/**
	 * Ensure that document is present in exactly one
	 * process' documents list: the one referenced through
	 * its "process" attribute.
	 * @param document Document to check & adjust.
	 */
	void addDocumentToProcess(QMSDocument document);
}
