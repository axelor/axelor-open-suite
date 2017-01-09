package com.axelor.apps.base.web;

import java.util.List;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.common.Inflector;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AppController {
	
	@Inject
	private AppService appService;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	public void importDataDemo(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);

		response.setFlash(appService.importDataDemo(app));
		response.setReload(true);
	}
	
	public void importDataInit(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);
		
		if (!app.getInitDataLoaded()) {
			appService.importDataInit(app);
			response.setValue("initDataLoaded", true);
		}
	}
	
	public void configure(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);
		
		String type = app.getTypeSelect();
		String appName = Inflector.getInstance().camelize(type);
		String viewName = "app-" + type + "-config-form";
		
		if (metaViewRepo.findByName(viewName) == null) {
			response.setFlash(I18n.get("No configuraiton required"));
		}
		else {
			response.setView(ActionView.define(I18n.get("Configure: ") + app.getName())
				.add("form", viewName)
				.model("com.axelor.apps.base.db.App" + appName)
				.context("_showRecord", app.getId())
				.map());
		}
	}
	
	public void checkParent(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);
		
		List<App> depends = appService.getDepends(app, false);
		if (!depends.isEmpty()) {
			List<String> parents = appService.getNames(depends);
			response.setAlert(String.format(I18n.get("Following apps will be installed %s"), parents));
		}
	}
	
	public void installParent(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);
		
		appService.installParent(app);
	}
	
	public void checkChildren(ActionRequest request, ActionResponse response) {
		
		App app = request.getContext().asType(App.class);
		
		List<App> children = appService.getChildren(app, true);
		if (!children.isEmpty()) {
			List<String> childrenNames = appService.getNames(children);
			response.setFlash(String.format(I18n.get("This app is used by %s. Please deactivate them before continue."), childrenNames));
			response.setValue("active", true);
		}
	}
}
