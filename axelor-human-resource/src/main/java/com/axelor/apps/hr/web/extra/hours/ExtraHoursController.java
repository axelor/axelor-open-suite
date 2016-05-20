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
package com.axelor.apps.hr.web.extra.hours;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.repo.ExtraHoursRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;


public class ExtraHoursController {


	public void editExtraHours(ActionRequest request, ActionResponse response){
		List<ExtraHours> extraHoursList = Beans.get(ExtraHoursRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		if(extraHoursList.isEmpty()){
			response.setView(ActionView
									.define(I18n.get("Extra Hours"))
									.model(ExtraHours.class.getName())
									.add("form", "extra-hours-form")
									.map());
		}
		else if(extraHoursList.size() == 1){
			response.setView(ActionView
					.define(I18n.get("ExtraHours"))
					.model(ExtraHours.class.getName())
					.add("form", "extra-hours-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(extraHoursList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define(I18n.get("ExtraHours"))
					.model(Wizard.class.getName())
					.add("form", "popup-extra-hours-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
			  		.param("popup-save", "false")
					.map());
		}
	}

	public void allExtraHours(ActionRequest request, ActionResponse response){
		List<ExtraHours> extraHoursList = Beans.get(ExtraHoursRepository.class).all().filter("self.user = ?1 AND self.company = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> extraHoursListId = new ArrayList<Long>();
		for (ExtraHours extraHours : extraHoursList) {
			extraHoursListId.add(extraHours.getId());
		}

		String extraHoursListIdStr = "-2";
		if(!extraHoursListId.isEmpty()){
			extraHoursListIdStr = Joiner.on(",").join(extraHoursListId);
		}

		response.setView(ActionView.define(I18n.get("My Extra hours"))
				   .model(ExtraHours.class.getName())
				   .add("grid","extra-hours-grid")
				   .add("form","extra-hours-form")
				   .domain("self.id in ("+extraHoursListIdStr+")")
				   .map());
	}

	public void validateExtraHours(ActionRequest request, ActionResponse response) throws AxelorException{
		
		List<ExtraHours> extraHoursList = Lists.newArrayList();
				
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			extraHoursList = Query.of(ExtraHours.class).filter("self.company = ?1 AND self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			extraHoursList = Query.of(ExtraHours.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> extraHoursListId = new ArrayList<Long>();
		for (ExtraHours extraHours : extraHoursList) {
			extraHoursListId.add(extraHours.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null && !AuthUtils.getUser().getEmployee().getHrManager()){
			extraHoursList = Query.of(ExtraHours.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (ExtraHours extraHours : extraHoursList) {
			extraHoursListId.add(extraHours.getId());
		}
		String extraHoursListIdStr = "-2";
		if(!extraHoursListId.isEmpty()){
			extraHoursListIdStr = Joiner.on(",").join(extraHoursListId);
		}

		response.setView(ActionView.define(I18n.get("Extra hours to Validate"))
			   .model(ExtraHours.class.getName())
			   .add("grid","extra-hours-validate-grid")
			   .add("form","extra-hours-form")
			   .domain("self.id in ("+extraHoursListIdStr+")")
			   .map());
	}

	public void editExtraHoursSelected(ActionRequest request, ActionResponse response){
		Map extraHoursMap = (Map)request.getContext().get("extraHoursSelect");
		ExtraHours extraHours = Beans.get(ExtraHoursRepository.class).find(new Long((Integer)extraHoursMap.get("id")));
		response.setView(ActionView
				.define("Extra hours")
				.model(ExtraHours.class.getName())
				.add("form", "extra-hours-form")
				.param("forceEdit", "true")
				.domain("self.id = "+extraHoursMap.get("id"))
				.context("_showRecord", String.valueOf(extraHours.getId())).map());
	}

	public void historicExtraHours(ActionRequest request, ActionResponse response){
		
		List<ExtraHours> extraHoursList = Lists.newArrayList();
		
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			extraHoursList = Query.of(ExtraHours.class).filter("self.company = ?1 AND (self.statusSelect = 3 OR self.statusSelect = 4)", AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			extraHoursList = Query.of(ExtraHours.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> extraHoursListId = new ArrayList<Long>();
		for (ExtraHours extraHours : extraHoursList) {
			extraHoursListId.add(extraHours.getId());
		}

		String extraHoursListIdStr = "-2";
		if(!extraHoursListId.isEmpty()){
			extraHoursListIdStr = Joiner.on(",").join(extraHoursListId);
		}

		response.setView(ActionView.define(I18n.get("Historic colleague extra hours"))
				   .model(ExtraHours.class.getName())
				   .add("grid","extra-hours-grid")
				   .add("form","extra-hours-form")
				   .domain("self.id in ("+extraHoursListIdStr+")")
				   .map());
	}

	public void showSubordinateExtraHours(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1 AND self.activeCompany = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> extraHoursListId = new ArrayList<Long>();
		for (User user : userList) {
			List<ExtraHours> extraHoursList = Query.of(ExtraHours.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",user,AuthUtils.getUser().getActiveCompany()).fetch();
			for (ExtraHours extraHours : extraHoursList) {
				extraHoursListId.add(extraHours.getId());
			}
		}
		if(extraHoursListId.isEmpty()){
			response.setNotify(I18n.get("No extra hours to be validated by your subordinates"));
		}
		else{
			String extraHoursListIdStr = "-2";
			if(!extraHoursListId.isEmpty()){
				extraHoursListIdStr = Joiner.on(",").join(extraHoursListId);
			}

			response.setView(ActionView.define(I18n.get("Extra hours to be Validated by your subordinates"))
				   .model(ExtraHours.class.getName())
				   .add("grid","extra-hours-grid")
				   .add("form","extra-hours-form")
				   .domain("self.id in ("+extraHoursListIdStr+")")
				   .map());
		}
	}

}
