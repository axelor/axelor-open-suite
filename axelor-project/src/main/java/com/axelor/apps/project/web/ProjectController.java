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
package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectController {

	public void generateProjectFromPartner(ActionRequest request, ActionResponse response){
		Partner partner = Beans.get(PartnerRepository.class).find(Long.valueOf(request.getContext().get("_idPartner").toString()));
		User user = AuthUtils.getUser();
		Project project = Beans.get(ProjectService.class).generateProject(null, partner.getName()+" project", user, user.getActiveCompany(), partner);
		response.setValues(Mapper.toMap(project));
	}

	public void setDuration(ActionRequest request, ActionResponse response){
		Project project = request.getContext().asType(Project.class);
		long diffInDays = ChronoUnit.DAYS.between(project.getFromDate(),project.getToDate());
		BigDecimal duration = new BigDecimal(diffInDays);
		response.setValue("$visibleDuration", duration);
		response.setValue("duration", duration);
	}
	
	public void createPlanning(ActionRequest request, ActionResponse response){
		
		Project project = request.getContext().asType(Project.class);
		
		List<ProjectPlanning> projectPlannings = Beans.get(ProjectService.class).createPlanning(project);
		
		response.setView(ActionView.define(I18n.get("Project Planning"))
				.model(ProjectPlanning.class.getName())
				.add("calendar", "project-planning-calendar")
				.add("grid", "project-planning-grid")
				.add("form", "project-planning-form")
				.domain("self.id in :_planningIds")
				.context("_planningIds", projectPlannings.stream().map(it->it.getId()).collect(Collectors.toList()))
				.map());
				
	}

	public void generateQuotation(ActionRequest request, ActionResponse response) {
		Project project = request.getContext().asType(Project.class);
		try {
			SaleOrder order = Beans.get(ProjectService.class).generateQuotation(project);
			response.setView(ActionView
					.define("Sale Order")
					.model(SaleOrder.class.getName())
					.add("form", "sale-order-form")
					.context("_showRecord", String.valueOf(order.getId())).map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void selectTeam(ActionRequest request, ActionResponse response) {
	    Project project = request.getContext().asType(Project.class);
		project = Beans.get(ProjectRepository.class).find(project.getId());
		try {
			Beans.get(ProjectService.class).cascadeUpdateTeam(project, project.getTeam(), project.getSynchronisable());
			response.setReload(true);
		} catch (Exception e) {
		    TraceBackService.trace(response, e);
		}
	}

}
