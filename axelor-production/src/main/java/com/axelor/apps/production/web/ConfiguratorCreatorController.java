package com.axelor.apps.production.web;

import com.axelor.apps.production.db.Configurator;
import com.axelor.apps.production.db.ConfiguratorCreator;
import com.axelor.apps.production.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.production.service.ConfiguratorCreatorService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ConfiguratorCreatorController {
	
	@Inject
	private ConfiguratorCreatorRepository configuratorCreatorRepo;
	
	@Inject
	private ConfiguratorCreatorService configuratorCreatorService;
	
	public void generateConfigurator(ActionRequest request, ActionResponse response) {
		
		ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
		
		creator = configuratorCreatorRepo.find(creator.getId());
		
		Configurator configurator = configuratorCreatorService.generateConfigurator(creator);
		
		response.setView(ActionView.define(I18n.get("Configurator"))
				.add("form", "configurator-form")
				.add("grid", "configurator-grid")
				.model("com.axelor.apps.production.db.Configurator")
				.context("_showRecord", configurator.getId())
				.map());
		
		
	}
}
