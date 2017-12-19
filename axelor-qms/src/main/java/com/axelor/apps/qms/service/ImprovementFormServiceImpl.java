package com.axelor.apps.qms.service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.qms.db.ImprovementForm;
import com.axelor.apps.qms.db.repo.ImprovementFormRepository;
import com.axelor.apps.qms.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImprovementFormServiceImpl implements ImprovementFormService {
	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	protected SequenceService sequenceService;

	@Inject
	protected ImprovementFormRepository improvementFormRepository;

	@Override
	public String getSequence(Company company) throws AxelorException {
		if(logger.isDebugEnabled()) {
			logger.debug("Fetching next improvement form sequence value for company " + company.getCode());
		}

		String sequence = sequenceService.getSequenceNumber(IAdministration.QMS_IMPROVEMENT_FORM, company);
		if(sequence == null) {
			throw new AxelorException(company, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.IMPROVEMENT_FORM_MISSING_SEQUENCE),company.getName());
		}
		return sequence;
	}

	@Override
	@Transactional
	public void confirmImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_CONFIRMED) {
			throw new IllegalStateException();
		}
		improvementForm.setReference(getSequence(improvementForm.getCompany()));
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_CONFIRMED);
		improvementForm.setConfirmationDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void assignImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_ASSIGNED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_ASSIGNED);
		improvementForm.setAssignmentDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void analyzeImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_ANALYZED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_ANALYZED);
		improvementForm.setAnalysisDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void correctImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_CORRECTED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_CORRECTED);
		improvementForm.setCorrectionDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void assessImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_ASSESSED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_ASSESSED);
		improvementForm.setPostCorrectionAssessmentDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void closeImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() >= ImprovementFormRepository.STATE_CLOSED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_CLOSED);
		improvementForm.setClosingDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void cancelImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() == ImprovementFormRepository.STATE_CANCELED) {
			throw new IllegalStateException();
		}
		improvementForm.setPreCancelStatusSelect(improvementForm.getStatusSelect());
		improvementForm.setStatusSelect(ImprovementFormRepository.STATE_CANCELED);
		improvementForm.setCancelationDate(LocalDate.now());
		improvementFormRepository.save(improvementForm);
	}

	@Override
	@Transactional
	public void reopenImprovementForm(ImprovementForm improvementForm) throws AxelorException {
		if(improvementForm.getStatusSelect() != ImprovementFormRepository.STATE_CANCELED) {
			throw new IllegalStateException();
		}
		improvementForm.setStatusSelect(improvementForm.getPreCancelStatusSelect());
		improvementForm.setCancelationDate(null);
		improvementFormRepository.save(improvementForm);
	}
}
