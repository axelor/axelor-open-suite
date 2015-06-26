package com.axelor.apps.project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.ProjectPlanningLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class ProjectPlanningService extends ProjectPlanningRepository{

	@Transactional
	public ProjectPlanning createMyPlanning(User user, int year, int week) throws AxelorException{
		ProjectPlanning planning = new ProjectPlanning();
		planning.setUser(user);
		planning.setYear(year);
		planning.setWeek(week);
		String query = "self.assignedTo = ?1 OR ?1 MEMBER OF self.membersSet";
		List<ProjectTask> projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter(query, user).fetch();
		if(projectTaskList == null || projectTaskList.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_PLANNING_NO_TASK)), IException.CONFIGURATION_ERROR);
		}
		List<ProjectPlanningLine> planningLineList = new ArrayList<ProjectPlanningLine>();
		for (ProjectTask projectTask : projectTaskList) {
			ProjectPlanningLine projectPlanningLine = new ProjectPlanningLine();
			projectPlanningLine.setUser(user);
			projectPlanningLine.setProjectTask(projectTask);
			planningLineList.add(projectPlanningLine);
		}
		planning.setProjectPlanningLineList(planningLineList);

		save(planning);
		return planning;
	}

	@Transactional
	public ProjectPlanning createMyTeamPlanning(Team team,int year, int week) throws AxelorException{
		ProjectPlanning planning = new ProjectPlanning();
		planning.setTeam(team);
		planning.setYear(year);
		planning.setWeek(week);
		List<ProjectTask> projectTaskList = null;
		Set<User> userList = team.getUserSet();
		List<ProjectPlanningLine> planningLineList = new ArrayList<ProjectPlanningLine>();

		for (User user : userList) {
			String query = "self.assignedTo = ?1 OR ?1 MEMBER OF self.membersSet";
			projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter(query, user).fetch();
			if(projectTaskList != null && !projectTaskList.isEmpty()){
				for (ProjectTask projectTask : projectTaskList) {
					ProjectPlanningLine projectPlanningLine = new ProjectPlanningLine();
					projectPlanningLine.setUser(user);
					projectPlanningLine.setProjectTask(projectTask);
					planningLineList.add(projectPlanningLine);
				}
			}
		}

		if(planningLineList.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_PLANNING_NO_TASK_TEAM)), IException.CONFIGURATION_ERROR);
		}
		planning.setProjectPlanningLineList(planningLineList);

		save(planning);
		return planning;
	}

	public static String getNameForColumns(int year, int week, int day){
		LocalDate date = new LocalDate().withYear(year).withWeekOfWeekyear(week).withDayOfWeek(1);
		LocalDate newDate = date.plusDays(day - 1);
		return " " + Integer.toString(newDate.getDayOfMonth())+"/"+Integer.toString(newDate.getMonthOfYear());
	}

	public static LocalDate getFromDate(){
		LocalDate today = GeneralService.getTodayDate();
		LocalDate newDate = new LocalDate().withYear(today.getYear()).withWeekOfWeekyear(today.getWeekOfWeekyear()).withDayOfMonth(1);
		return newDate;
	}

	public static LocalDate getToDate(){
		LocalDate today = GeneralService.getTodayDate();
		LocalDate newDate = new LocalDate().withYear(today.getYear()).withWeekOfWeekyear(today.getWeekOfWeekyear()).withDayOfMonth(today.dayOfMonth().withMaximumValue().getDayOfMonth());
		return newDate;
	}

}
