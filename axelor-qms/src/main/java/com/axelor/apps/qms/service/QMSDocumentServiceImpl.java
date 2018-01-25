package com.axelor.apps.qms.service;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class QMSDocumentServiceImpl implements QMSDocumentService {
	@Inject
	protected SequenceService sequenceService;

	@Override
	public void assignReference(QMSDocument document) {
		if(Strings.isNullOrEmpty(document.getReference())) {
			final String index = sequenceService.getSequenceNumber("qmsDocument", document.getCompany());
			if(index == null) {
				throw new PersistenceException(String.format(I18n.get(IExceptionMessage.DOCUMENT_MISSING_SEQUENCE), document.getCompany().getName()));
			}
			// TODO should we make pattern configurable?
			document.setReference(String.format("%s-%s-%s", document.getProcess().getCode(), document.getType().getCode(), index));
		}
	}
}
