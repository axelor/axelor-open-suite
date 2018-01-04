package com.axelor.apps.qms.service;

import com.axelor.apps.qms.db.QMSDocument;

public interface QMSDocumentVersionService {
	/**
	 * Returns the next free version index for the given document.
	 * Consistency is ensured through unique index in database,
	 * concurrent calls to this function without flush to DB may lead
	 * to duplicate indices (which won't be saved).
	 * @param document Document for which we want the next index
	 * @return The next index as a string (A = first, B = second, etc.).
	 */
	String getNextVersionIndex(QMSDocument document);
}
