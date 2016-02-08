/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.ProjectPlanningLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningLineRepository;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectPlanningService {

	@Inject
	protected ProjectPlanningLineRepository projectPlanningLineRepository;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected ProjectPlanningRepository projectPlanningRepo;

	@Transactional
	public ProjectPlanning createPlanning(int year, int week) throws AxelorException{
		ProjectPlanning planning = new ProjectPlanning();
		planning.setYear(year);
		planning.setWeek(week);

		projectPlanningRepo.save(planning);
		return planning;
	}

	public static String getNameForColumns(int year, int week, int day){
		LocalDate date = new LocalDate().withYear(year).withWeekOfWeekyear(week).withDayOfWeek(1);
		LocalDate newDate = date.plusDays(day - 1);
		return " " + Integer.toString(newDate.getDayOfMonth())+"/"+Integer.toString(newDate.getMonthOfYear());
	}

	@Transactional
	public List<ProjectPlanningLine> populateMyPlanning(ProjectPlanning planning, User user) throws AxelorException{
		List<ProjectPlanningLine> planningLineList = new ArrayList<ProjectPlanningLine>();
		String query = "self.assignedTo = ?1 OR ?1 MEMBER OF self.membersUserSet";
		List<ProjectTask> projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter(query, user).fetch();
		if(projectTaskList == null || projectTaskList.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_PLANNING_NO_TASK)), IException.CONFIGURATION_ERROR);
		}
		for (ProjectTask projectTask : projectTaskList) {
			ProjectPlanningLine projectPlanningLine = null;
			projectPlanningLine = projectPlanningLineRepository.all().filter("self.user = ?1 AND self.projectTask = ?2 AND self.year = ?3 AND self.week = ?4", user, projectTask, planning.getYear(), planning.getWeek()).fetchOne();
			if(projectPlanningLine == null){
				projectPlanningLine = new ProjectPlanningLine();
				projectPlanningLine.setUser(user);
				projectPlanningLine.setProjectTask(projectTask);
				projectPlanningLine.setYear(planning.getYear());
				projectPlanningLine.setWeek(planning.getWeek());
				projectPlanningLineRepository.save(projectPlanningLine);
			}
			planningLineList.add(projectPlanningLine);
		}
		return planningLineList;
	}

	@Transactional
	public List<ProjectPlanningLine> populateMyTeamPlanning(ProjectPlanning planning, Team team) throws AxelorException{
		List<ProjectPlanningLine> planningLineList = new ArrayList<ProjectPlanningLine>();
		List<ProjectTask> projectTaskList = null;
		Set<User> userList = team.getUserSet();

		for (User user : userList) {
			String query = "self.assignedTo = ?1 OR ?1 MEMBER OF self.membersUserSet";
			projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter(query, user).fetch();
			if(projectTaskList != null && !projectTaskList.isEmpty()){
				for (ProjectTask projectTask : projectTaskList) {
					ProjectPlanningLine projectPlanningLine = null;
					projectPlanningLine = projectPlanningLineRepository.all().filter("self.user = ?1 AND self.projectTask = ?2 AND self.year = ?3 AND self.week = ?4", user, projectTask, planning.getYear(), planning.getWeek()).fetchOne();
					if(projectPlanningLine == null){
						projectPlanningLine = new ProjectPlanningLine();
						projectPlanningLine.setUser(user);
						projectPlanningLine.setProjectTask(projectTask);
						projectPlanningLine.setYear(planning.getYear());
						projectPlanningLine.setWeek(planning.getWeek());
						projectPlanningLineRepository.save(projectPlanningLine);
					}
					planningLineList.add(projectPlanningLine);
				}
			}
		}

		if(planningLineList.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_PLANNING_NO_TASK_TEAM)), IException.CONFIGURATION_ERROR);
		}
		return planningLineList;
	}

	public LocalDate getFromDate(){
		LocalDate todayDate = generalService.getTodayDate();
		return new LocalDate(todayDate.getYear(), todayDate.getMonthOfYear(), todayDate.dayOfMonth().getMinimumValue());
	}

	public LocalDate getToDate(){
		LocalDate todayDate = generalService.getTodayDate();
		return new LocalDate(todayDate.getYear(), todayDate.getMonthOfYear(), todayDate.dayOfMonth().getMaximumValue());
	}
	
	public void getTasksForUser(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			LocalDate todayDate = Beans.get(GeneralService.class).getTodayDate();
			List<ProjectPlanningLine> linesList = Beans.get(ProjectPlanningLineRepository.class).all().
					filter("self.user.id = ?1 AND self.year >= ?2 AND self.week >= ?3", 
					AuthUtils.getUser().getId(), todayDate.getYear(), todayDate.getWeekOfWeekyear()).fetch();
			
			for (ProjectPlanningLine line : linesList) {
				if(line.getMonday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.MONDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getMonday().toString());
						dataList.add(map);
					}
				}
				if(line.getTuesday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.TUESDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getTuesday().toString());
						dataList.add(map);
					}
				}
				if(line.getWednesday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.WEDNESDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getWednesday().toString());
						dataList.add(map);
					}
				}
				if(line.getThursday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.THURSDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getThursday().toString());
						dataList.add(map);
					}
				}
				if(line.getFriday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.FRIDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getFriday().toString());
						dataList.add(map);
					}
				}
				if(line.getSaturday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.SATURDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getSaturday().toString());
						dataList.add(map);
					}
				}
				if(line.getSunday().compareTo(BigDecimal.ZERO) != 0){
					LocalDate date = new LocalDate().withYear(line.getYear()).withWeekOfWeekyear(line.getWeek()).withDayOfWeek(DateTimeConstants.SUNDAY);
					if(date.isAfter(todayDate) || date.isEqual(todayDate)){
						Map<String, String> map = new HashMap<String,String>();
						map.put("taskId", line.getProjectTask().getId().toString());
						map.put("name", line.getProjectTask().getFullName());
						if(line.getProjectTask().getProject() != null){
							map.put("projectName", line.getProjectTask().getProject().getFullName());
						}
						else{
							map.put("projectName", "");
						}
						map.put("date", date.toString());
						map.put("duration", line.getSunday().toString());
						dataList.add(map);
					}
				}
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}

}
