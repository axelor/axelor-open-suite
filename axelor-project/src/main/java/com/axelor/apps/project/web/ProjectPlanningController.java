package com.axelor.apps.project.web;

import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.ProjectPlanningLine;
import com.axelor.apps.project.db.repo.ProjectPlanningLineRepository;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.service.ProjectPlanningService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectPlanningController extends ProjectPlanningRepository{

	@Inject
	protected ProjectPlanningService projectPlanningService;

	@Inject
	protected ProjectPlanningLineRepository projectPlanningLineRepository;

	public void myPlanning(ActionRequest request, ActionResponse response) throws AxelorException{
		LocalDate todayDate = GeneralService.getTodayDate();
		ProjectPlanning planning = this.all().filter("self.year = ?1 AND self.week = ?2",todayDate.getYear(),todayDate.getWeekOfWeekyear()).fetchOne();
		if(planning == null){
			planning = projectPlanningService.createPlanning(todayDate.getYear(),todayDate.getWeekOfWeekyear());
		}
		response.setView(ActionView
				.define("Week"+planning.getWeek())
				.model(ProjectPlanning.class.getName())
				.add("form", "project-planning-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(planning.getId()))
				.context("_type", "user").map());
	}

	public void myTeamPlanning(ActionRequest request, ActionResponse response) throws AxelorException{
		LocalDate todayDate = GeneralService.getTodayDate();
		ProjectPlanning planning = this.all().filter("self.year = ?1 AND self.week = ?2",todayDate.getYear(),todayDate.getWeekOfWeekyear()).fetchOne();
		if(planning == null){
			planning = projectPlanningService.createPlanning(todayDate.getYear(),todayDate.getWeekOfWeekyear());
		}
		response.setView(ActionView
				.define("Week"+planning.getWeek())
				.model(ProjectPlanning.class.getName())
				.add("form", "project-planning-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(planning.getId()))
				.context("_type", "team").map());
	}

	public void planningPreviousWeek(ActionRequest request, ActionResponse response) throws AxelorException{
		ProjectPlanning planning = request.getContext().asType(ProjectPlanning.class);
		int previousWeek = planning.getWeek() - 1;
		int year = planning.getYear();
		if(previousWeek < 1){
			previousWeek = 52;
			year--;
		}

		ProjectPlanning planningPreviousWeek = null;

		planningPreviousWeek = this.all().filter("self.year = ?1 AND self.week = ?2",year,previousWeek).fetchOne();

		if(planningPreviousWeek == null){

			planningPreviousWeek = projectPlanningService.createPlanning(year,previousWeek);
		}

		String type = request.getContext().get("_type").toString();
		if(type.contentEquals("user")){
			response.setView(ActionView
					.define("Week"+planningPreviousWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningPreviousWeek.getId()))
					.context("_type", "user").map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningPreviousWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningPreviousWeek.getId()))
					.context("_type", "team").map());
		}

	}

	public void planningNextWeek(ActionRequest request, ActionResponse response) throws AxelorException{
		ProjectPlanning planning = request.getContext().asType(ProjectPlanning.class);
		int nextWeek = planning.getWeek() + 1;
		int year = planning.getYear();
		if(nextWeek > 52){
			nextWeek = 1;
			year++;
		}
		ProjectPlanning planningNextWeek = null;

		planningNextWeek = this.all().filter("self.year = ?1 AND self.week = ?2",year,nextWeek).fetchOne();

		if(planningNextWeek == null){

			planningNextWeek = projectPlanningService.createPlanning(year,nextWeek);
		}
		String type = request.getContext().get("_type").toString();
		if(type.contentEquals("user")){
			response.setView(ActionView
					.define("Week"+planningNextWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningNextWeek.getId()))
					.context("_type", "user").map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningNextWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningNextWeek.getId()))
					.context("_type", "team").map());
		}
	}

	public void planningCurrentWeek(ActionRequest request, ActionResponse response) throws AxelorException{
		request.getContext().asType(ProjectPlanning.class);
		LocalDate currentDate = GeneralService.getTodayDate();
		int year = currentDate.getYear();
		int week = currentDate.getWeekOfWeekyear();
		ProjectPlanning planningCurrentWeek = null;

		planningCurrentWeek = this.all().filter("self.year = ?1 AND self.week = ?2",year,week).fetchOne();

		if(planningCurrentWeek == null){

			planningCurrentWeek = projectPlanningService.createPlanning(year,week);
		}

		String type = request.getContext().get("_type").toString();
		if(type.contentEquals("user")){
			response.setView(ActionView
					.define("Week"+planningCurrentWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningCurrentWeek.getId()))
					.context("_type", "user").map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningCurrentWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningCurrentWeek.getId()))
					.context("_type", "team").map());
		}
	}

	public void populate(ActionRequest request, ActionResponse response) throws AxelorException{
		User user = AuthUtils.getUser();
		GeneralService.getTodayDate();
		ProjectPlanning planning = request.getContext().asType(ProjectPlanning.class);
		String type = request.getContext().get("_type").toString();
		List<ProjectPlanningLine> projectPlanningLineList = null;
		if(type.contentEquals("user")){
			projectPlanningLineList = projectPlanningService.populateMyPlanning(planning, user);
		}
		else{
			projectPlanningLineList = projectPlanningService.populateMyTeamPlanning(planning, user.getActiveTeam());
		}
		response.setValue("$projectPlanningLineList", projectPlanningLineList);
	}

	@Transactional
	public void saveLines(ActionRequest request, ActionResponse response) throws AxelorException{

		List<ProjectPlanningLine> planningLineList =(List<ProjectPlanningLine>) request.getContext().get("$projectPlanningLineList");
		if(planningLineList != null){
			for (ProjectPlanningLine projectPlanningLine : planningLineList) {
				if(projectPlanningLine.getToSave()){
					projectPlanningLineRepository.save(projectPlanningLine);
				}
			}
		}


	}
}
