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

public class PublicHolidayService extends PublicHolidayDayRepository{

	@Inject
	protected WeeklyPlanningService weeklyPlanningService;

	public BigDecimal computePublicHolidayDays(LocalDate dateFrom, LocalDate dateTo, WeeklyPlanning weeklyPlanning, Employee employee) throws AxelorException{
		BigDecimal publicHolidayDays = BigDecimal.ZERO;

		List<PublicHolidayDay> publicHolidayDayList= this.all().filter("self.publicHolidayPlann = ?1 AND self.date BETWEEN ?2 AND ?3", employee.getPublicHolidayPlanning(), dateFrom, dateTo).fetch();
		for (PublicHolidayDay publicHolidayDay : publicHolidayDayList) {
			publicHolidayDays = publicHolidayDays.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, publicHolidayDay.getDate())));
		}
		return publicHolidayDays;
	}
}
