package com.axelor.apps.hr.service.weeklyplanning;

import java.util.List;

import com.axelor.apps.hr.db.DayPlanning;
import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class WeeklyPlanningServiceImp implements WeeklyPlanningService{
	
	@Transactional(rollbackOn={Exception.class})
	public WeeklyPlanning initPlanning(WeeklyPlanning planning){
		String[] dayTab= new String[]{"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
		for (int i = 0; i < dayTab.length; i++) {
			DayPlanning day = new DayPlanning();
			day.setName(dayTab[i]);
			planning.addWeekDay(day);
		}
		return planning;
	}
	
	@Transactional(rollbackOn={Exception.class})
	public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException{
		
		List<DayPlanning> listDay = planning.getWeekDays();
		for (DayPlanning dayPlanning : listDay) {
			
			if(dayPlanning.getMorningFrom() != null && dayPlanning.getMorningTo() != null){
				if(dayPlanning.getMorningFrom().isAfter(dayPlanning.getMorningTo()))
				{
					String message = "Invalid times in morning on "+dayPlanning.getName();
					throw new AxelorException(message, 5);
				}
			}
			
			if(dayPlanning.getMorningTo() != null && dayPlanning.getAfternoonFrom() != null){
				if(dayPlanning.getMorningTo().isAfter(dayPlanning.getAfternoonFrom()))
				{
					String message = "Invalid times between morning and afternoon on "+dayPlanning.getName();
					throw new AxelorException(message, 5);
				}
			}
			
			if(dayPlanning.getAfternoonFrom() != null && dayPlanning.getAfternoonTo() != null){
				if(dayPlanning.getAfternoonFrom().isAfter(dayPlanning.getAfternoonTo()))
				{
					String message = "Invalid times in afternoon on "+dayPlanning.getName();
					throw new AxelorException(message, 5);
				}
			}
			
			if((dayPlanning.getMorningFrom() == null && dayPlanning.getMorningTo() != null) 
				|| (dayPlanning.getMorningTo() == null && dayPlanning.getMorningFrom() != null)
				|| (dayPlanning.getAfternoonFrom() == null && dayPlanning.getAfternoonTo() != null) 
				|| (dayPlanning.getAfternoonTo() == null && dayPlanning.getAfternoonFrom() != null))
			{
				String message = "Some times are null and should not on "+dayPlanning.getName();
				throw new AxelorException(message, 5);
			}
		}
		return planning;
	}
}
