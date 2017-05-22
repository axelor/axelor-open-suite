package com.axelor.apps.project.web;

import java.util.Collection;
import java.util.Map;

import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class ProjectPlanningController {
	
	public void showPlanning(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		
		Collection<Map<String, Object>> users = (Collection<Map<String, Object>>) context.get("userSet");
		
		String userIds = "";
		if (users != null) {
			for (Map<String, Object> user : users) {
				if (userIds.isEmpty()){
					userIds = user.get("id").toString();
				}
				else {
					userIds += "," + user.get("id").toString();
				}
			}
		}
		
		ActionViewBuilder builder = ActionView.define(I18n.get("Project Planning"))
				.model(ProjectPlanning.class.getName());
		String url = "studio/planning";
		
		if (!userIds.isEmpty()) {
			url += "?userIds=" + userIds;
			builder.domain("self.user.id in (:userIds)");
			builder.context("userIds", userIds);
		}
		
		builder.add("html", url);
		response.setView(builder.map());
	}
}
