/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
