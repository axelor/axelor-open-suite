package com.axelor.apps.hr.service.publicHoliday;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.PublicHolidayDay;
import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.apps.hr.db.repo.PublicHolidayDayRepository;
import com.axelor.apps.hr.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PublicHolidayService {

	@Inject
	protected WeeklyPlanningService weeklyPlanningService;
	
	@Inject
	protected PublicHolidayDayRepository publicHolidayDayRepo;

	public BigDecimal computePublicHolidayDays(LocalDate dateFrom, LocalDate dateTo, WeeklyPlanning weeklyPlanning, Employee employee) throws AxelorException{
		BigDecimal publicHolidayDays = BigDecimal.ZERO;

		List<PublicHolidayDay> publicHolidayDayList= publicHolidayDayRepo.all().filter("self.publicHolidayPlann = ?1 AND self.date BETWEEN ?2 AND ?3", employee.getPublicHolidayPlanning(), dateFrom, dateTo).fetch();
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
}
