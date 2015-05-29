package com.axelor.apps.hr.web.leave;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.hr.db.Leave;
import com.axelor.apps.hr.db.repo.LeaveRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;

public class LeaveController {
	public void editLeave(ActionRequest request, ActionResponse response){
		List<Leave> leaveList = Beans.get(LeaveRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		if(leaveList.isEmpty()){
			response.setView(ActionView
									.define("Leave")
									.model(Leave.class.getName())
									.add("form", "leave-form")
									.context("","").map());
		}
		else if(leaveList.size() == 1){
			response.setView(ActionView
					.define("Leave")
					.model(Leave.class.getName())
					.add("form", "leave-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(leaveList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Leave")
					.model(Wizard.class.getName())
					.add("form", "popup-leave-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
					.map());
		}
	}
	
	public void editLeaveSelected(ActionRequest request, ActionResponse response){
		Map leaveMap = (Map)request.getContext().get("leaveSelect");
		Leave leave = Beans.get(LeaveRepository.class).find(new Long((Integer)leaveMap.get("id")));
		response.setView(ActionView
				.define("Leave")
				.model(Leave.class.getName())
				.add("form", "leave-form")
				.param("forceEdit", "true")
				.domain("self.id = "+leaveMap.get("id"))
				.context("_showRecord", String.valueOf(leave.getId())).map());
	}
	
	public void allLeave(ActionRequest request, ActionResponse response){
		List<Leave> leaveList = Beans.get(LeaveRepository.class).all().filter("self.user = ?1 AND self.company = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> leaveListId = new ArrayList<Long>();
		for (Leave leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		
		String leaveListIdStr = "-2";
		if(!leaveListId.isEmpty()){
			leaveListIdStr = Joiner.on(",").join(leaveListId);
		}
		
		response.setView(ActionView.define("My Leaves")
				   .model(Leave.class.getName())
				   .add("grid","leave-grid")
				   .add("form","leave-form")
				   .domain("self.id in ("+leaveListIdStr+")")
				   .map());
	}
	
	public void validateLeave(ActionRequest request, ActionResponse response){
		List<Leave> leaveList = Query.of(Leave.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> leaveListId = new ArrayList<Long>();
		for (Leave leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null){
			leaveList = Query.of(Leave.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2 ",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (Leave leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		String leaveListIdStr = "-2";
		if(!leaveListId.isEmpty()){
			leaveListIdStr = Joiner.on(",").join(leaveListId);
		}
		
		response.setView(ActionView.define("Leaves to Validate")
			   .model(Leave.class.getName())
			   .add("grid","leave-validate-grid")
			   .add("form","leave-form")
			   .domain("self.id in ("+leaveListIdStr+")")
			   .map());
	}
	
	public void historicLeave(ActionRequest request, ActionResponse response){
		List<Leave> leaveList = Beans.get(LeaveRepository.class).all().filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 3 OR self.statusSelect = 4",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> leaveListId = new ArrayList<Long>();
		for (Leave leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		
		String leaveListIdStr = "-2";
		if(!leaveListId.isEmpty()){
			leaveListIdStr = Joiner.on(",").join(leaveListId);
		}
		
		response.setView(ActionView.define("Colleague Leaves")
				.model(Leave.class.getName())
				   .add("grid","leave-grid")
				   .add("form","leave-form")
				   .domain("self.id in ("+leaveListIdStr+")")
				   .map());
	}
	
	
	public void showSubordinateLeaves(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1",AuthUtils.getUser()).fetch();
		List<Long> leaveListId = new ArrayList<Long>();
		for (User user : userList) {
			List<Leave> leaveList = Query.of(Leave.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",user,AuthUtils.getUser().getActiveCompany()).fetch();
			for (Leave leave : leaveList) {
				leaveListId.add(leave.getId());
			}
		}
		if(leaveListId.isEmpty()){
			response.setNotify(I18n.get("No leave to be validated by your subordinates"));
		}
		else{
			String leaveListIdStr = "-2";
			if(!leaveListId.isEmpty()){
				leaveListIdStr = Joiner.on(",").join(leaveListId);
			}
			
			response.setView(ActionView.define("Leaves to be Validated by your subordinates")
				   .model(Leave.class.getName())
				   .add("grid","leave-grid")
				   .add("form","leave-form")
				   .domain("self.id in ("+leaveListIdStr+")")
				   .map());
		}
	}
	
}
