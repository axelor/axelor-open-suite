package com.axelor.apps.talent.db.repo;

import javax.inject.Inject;
import javax.validation.ValidationException;

import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.exception.IExceptionMessage;
import com.axelor.apps.talent.service.TrainingSessionService;
import com.axelor.i18n.I18n;

public class TrainingSessionTalentRepository extends TrainingSessionRepository {
	
	@Inject
	private TrainingSessionService trainingSessionService;
	
	@Override
	public TrainingSession save(TrainingSession trainingSession) {
		
		if (trainingSession.getFromDate().isAfter(trainingSession.getToDate())) {
			throw new ValidationException(I18n.get(IExceptionMessage.INVALID_DATE_RANGE));
		}
		
		trainingSession.setFullName(trainingSessionService.computeFullName(trainingSession));
		
		return super.save(trainingSession);
	}
}
