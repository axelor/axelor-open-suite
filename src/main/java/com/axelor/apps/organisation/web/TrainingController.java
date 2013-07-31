package com.axelor.apps.organisation.web;

import org.joda.time.Days;

import com.axelor.apps.organisation.db.Training;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TrainingController {

	public void computeDuration(ActionRequest request, ActionResponse response) {

		Training training = request.getContext().asType(Training.class);

		if (training.getEndDate() != null && training.getStartDate() != null) {
			Days d = Days.daysBetween(training.getStartDate(), training.getEndDate());
			response.setValue("duration", d.getDays());
		}
		else {
			response.setValue("duration", null);
		}
	}

	public void computeDate(ActionRequest request, ActionResponse response) {

		Training training = request.getContext().asType(Training.class);
		
		try {
			int duration = Integer.parseInt(training.getDuration());

			if (duration >= 0) {
				if (training.getStartDate() != null) {
					response.setValue("endDate", training.getStartDate().plusDays(duration));
				}
				else if (training.getEndDate() != null) {
					response.setValue("startDate", training.getEndDate().minusDays(duration));
				}
			}
		} catch(NumberFormatException e) {
			
		}
	}
}
