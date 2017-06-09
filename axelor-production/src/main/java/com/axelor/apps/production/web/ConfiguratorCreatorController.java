package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ConfiguratorCreator;
import com.axelor.apps.production.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.ConfiguratorCreatorService;
import com.axelor.i18n.I18n;
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
		
		configuratorCreatorService.generateConfigurator(creator);
		
		response.setSignal("refresh-app", true);
		
		response.setFlash(I18n.get(IExceptionMessage.CONFIGURATOR_GENERATED));
		
		
	}
}
