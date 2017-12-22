package com.axelor.apps.qms.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class QMSDocumentManagementRepository extends QMSDocumentRepository {

	@Inject
	protected SequenceService sequenceService;

	@Override
	public QMSDocument save(QMSDocument entity) {
		if(Strings.isNullOrEmpty(entity.getReference())) {
			final String index = sequenceService.getSequenceNumber("qmsDocument", entity.getCompany());
			if(index == null) {
				throw new PersistenceException(I18n.get(IExceptionMessage.DOCUMENT_MISSING_SEQUENCE));
			}
			// TODO should we make pattern configurable?
			entity.setReference(String.format("%s-%s-%s", entity.getProcess().getCode(), entity.getType().getCode(), index));
		}
		return super.save(entity);
	}
}
