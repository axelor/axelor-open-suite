/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.service.ConfiguratorCreatorService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ConfiguratorCreatorController {

	private ConfiguratorCreatorRepository configuratorCreatorRepo;
	private ConfiguratorCreatorService configuratorCreatorService;

	@Inject
	public ConfiguratorCreatorController(ConfiguratorCreatorRepository configuratorCreatorRepo,
										 ConfiguratorCreatorService configuratorCreatorService) {
		this.configuratorCreatorRepo = configuratorCreatorRepo;
		this.configuratorCreatorService = configuratorCreatorService;
	}

	/**
	 * Called from configurator creator form.
	 * call {@link ConfiguratorCreatorService#updateIndicators(ConfiguratorCreator)}
	 * then {@link ConfiguratorCreatorService#generateConfigurator(ConfiguratorCreator)}
	 * @param request
	 * @param response
	 */
	public void generateConfigurator(ActionRequest request, ActionResponse response) {
		
		ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
		
		creator = configuratorCreatorRepo.find(creator.getId());

		configuratorCreatorService.updateIndicators(creator);

		configuratorCreatorService.generateConfigurator(creator);
		
		response.setSignal("refresh-app", true);
		
		response.setFlash(I18n.get(IExceptionMessage.CONFIGURATOR_GENERATED));
		
		
	}
}
