/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.publicHoliday;

import java.math.BigDecimal;
import java.util.List;

import java.time.LocalDate;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.PublicHolidayDay;
import com.axelor.apps.hr.db.PublicHolidayPlanning;
import com.axelor.apps.hr.db.repo.PublicHolidayDayRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PublicHolidayService {

	protected WeeklyPlanningService weeklyPlanningService;
	protected PublicHolidayDayRepository publicHolidayDayRepo;

	@Inject
	public PublicHolidayService(WeeklyPlanningService weeklyPlanningService, PublicHolidayDayRepository publicHolidayDayRepo){
		
		this.weeklyPlanningService = weeklyPlanningService;
		this.publicHolidayDayRepo = publicHolidayDayRepo;
	}
	
	public BigDecimal computePublicHolidayDays(LocalDate fromDate, LocalDate toDate, WeeklyPlanning weeklyPlanning, PublicHolidayPlanning publicHolidayPlanning) throws AxelorException{
		BigDecimal publicHolidayDays = BigDecimal.ZERO;

		List<PublicHolidayDay> publicHolidayDayList= publicHolidayDayRepo.all().filter("self.publicHolidayPlann = ?1 AND self.date BETWEEN ?2 AND ?3", publicHolidayPlanning, fromDate, toDate).fetch();
		for (PublicHolidayDay publicHolidayDay : publicHolidayDayList) {
			publicHolidayDays = publicHolidayDays.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, publicHolidayDay.getDate())));
		}
		return publicHolidayDays;
	}
	
	public boolean checkPublicHolidayDay(LocalDate date, Employee employee) throws AxelorException{

		List<PublicHolidayDay> publicHolidayDayList = publicHolidayDayRepo.all().filter("self.publicHolidayPlann = ?1 AND self.date = ?2", employee.getPublicHolidayPlanning(), date).fetch();
		if(publicHolidayDayList == null || publicHolidayDayList.isEmpty()){
			return false;
		}
		else{
			return true;
		}
	}
	
	public int getImposedDayNumber(Employee employee, LocalDate startDate, LocalDate endDate){
		
		PublicHolidayPlanning imposedDays =  employee.getImposedDayPlanning();
		
		if (imposedDays == null || imposedDays.getPublicHolidayDayList() == null || imposedDays.getPublicHolidayDayList().isEmpty()) { return 0; }
		
		List<PublicHolidayDay> imposedDayList= publicHolidayDayRepo.all().filter("self.publicHolidayPlann = ?1 AND self.date BETWEEN ?2 AND ?3", imposedDays, startDate, endDate).fetch();
		
		return imposedDayList.size();
	}
}
