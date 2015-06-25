package com.axelor.apps.project.web;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.service.ProjectPlanningService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectPlanningController extends ProjectPlanningRepository{

	@Inject
	protected ProjectPlanningService projectPlanningService;

	public void myPlanning(ActionRequest request, ActionResponse response) throws AxelorException{
		User user = AuthUtils.getUser();
		LocalDate todayDate = GeneralService.getTodayDate();
		ProjectPlanning planning = this.all().filter("self.user = ?1 AND self.year = ?2 AND self.week = ?3", user,todayDate.getYear(),todayDate.getWeekOfWeekyear()).fetchOne();
		if(planning == null){
			planning = projectPlanningService.createMyPlanning(user,todayDate.getYear(),todayDate.getWeekOfWeekyear());
			response.setView(ActionView
					.define("Week"+planning.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planning.getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planning.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planning.getId())).map());
		}
	}

	public void myTeamPlanning(ActionRequest request, ActionResponse response) throws AxelorException{
		User user = AuthUtils.getUser();
		LocalDate todayDate = GeneralService.getTodayDate();
		Team team = user.getActiveTeam();
		ProjectPlanning planning = this.all().filter("self.team = ?1 AND self.year = ?2 AND self.week = ?3", team,todayDate.getYear(),todayDate.getWeekOfWeekyear()).fetchOne();
		if(planning == null){
			planning = projectPlanningService.createMyTeamPlanning(team,todayDate.getYear(),todayDate.getWeekOfWeekyear());
			response.setView(ActionView
					.define("Week"+planning.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planning.getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planning.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planning.getId())).map());
		}
	}

	public void planningPreviousWeek(ActionRequest request, ActionResponse response) throws AxelorException{
		ProjectPlanning planning = request.getContext().asType(ProjectPlanning.class);
		int previousWeek = planning.getWeek() - 1;
		int year = planning.getYear();
		if(previousWeek < 1){
			previousWeek = 52;
			year--;
		}
		User user = planning.getUser();
		Team team = planning.getTeam();
		ProjectPlanning planningPreviousWeek = null;
		if(user == null){
			planningPreviousWeek = this.all().filter("self.team = ?1 AND self.year = ?2 AND self.week = ?3", team,year,previousWeek).fetchOne();

		}
		else{
			planningPreviousWeek = this.all().filter("self.user = ?1 AND self.year = ?2 AND self.week = ?3", user,year,previousWeek).fetchOne();
		}
		if(planningPreviousWeek == null){
			if(user == null){
				planningPreviousWeek = projectPlanningService.createMyTeamPlanning(team,year,previousWeek);
			}
			else{
				planningPreviousWeek = projectPlanningService.createMyPlanning(user,year,previousWeek);
			}

			response.setView(ActionView
					.define("Week"+planningPreviousWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningPreviousWeek.getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningPreviousWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningPreviousWeek.getId())).map());
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
		User user = planning.getUser();
		Team team = planning.getTeam();
		ProjectPlanning planningNextWeek = null;
		if(user == null){
			planningNextWeek = this.all().filter("self.team = ?1 AND self.year = ?2 AND self.week = ?3", team,year,nextWeek).fetchOne();

		}
		else{
			planningNextWeek = this.all().filter("self.user = ?1 AND self.year = ?2 AND self.week = ?3", user,year,nextWeek).fetchOne();
		}
		if(planningNextWeek == null){
			if(user == null){
				planningNextWeek = projectPlanningService.createMyTeamPlanning(team,year,nextWeek);
			}
			else{
				planningNextWeek = projectPlanningService.createMyPlanning(user,year,nextWeek);
			}

			response.setView(ActionView
					.define("Week"+planningNextWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningNextWeek.getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningNextWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningNextWeek.getId())).map());
		}
	}

	public void planningCurrentWeek(ActionRequest request, ActionResponse response) throws AxelorException{
		ProjectPlanning planning = request.getContext().asType(ProjectPlanning.class);
		LocalDate currentDate = GeneralService.getTodayDate();
		int year = currentDate.getYear();
		int week = currentDate.getWeekOfWeekyear();
		User user = planning.getUser();
		Team team = planning.getTeam();
		ProjectPlanning planningCurrentWeek = null;
		if(user == null){
			planningCurrentWeek = this.all().filter("self.team = ?1 AND self.year = ?2 AND self.week = ?3", team,year,week).fetchOne();

		}
		else{
			planningCurrentWeek = this.all().filter("self.user = ?1 AND self.year = ?2 AND self.week = ?3", user,year,week).fetchOne();
		}
		if(planningCurrentWeek == null){
			if(user == null){
				planningCurrentWeek = projectPlanningService.createMyTeamPlanning(team,year,week);
			}
			else{
				planningCurrentWeek = projectPlanningService.createMyPlanning(user,year,week);
			}

			response.setView(ActionView
					.define("Week"+planningCurrentWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningCurrentWeek.getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Week"+planningCurrentWeek.getWeek())
					.model(ProjectPlanning.class.getName())
					.add("form", "project-planning-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(planningCurrentWeek.getId())).map());
		}
	}
}
