/*
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
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.script.ScriptBindings;
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
     * Called from the sale order generate configurator wizard form.
     * @param request
     * @param response
     */
    public void createWizardDomain(ActionRequest request, ActionResponse response) {
        response.setAttr(
                "configuratorCreator",
                "domain",
                configuratorCreatorService.getConfiguratorCreatorDomain()
        );
    }

    /**
     * Called from the configurator creator form on attributes changes
     * @param request
     * @param response
     */
    public void updateAttributes(ActionRequest request, ActionResponse response) {
        ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
        creator = configuratorCreatorRepo.find(creator.getId());
        response.setSignal("refresh-app", true);
    }

    /**
     * Called from the configurator creator form on formula changes
     * @param request
     * @param response
     */
    public void updateAndActivate(ActionRequest request, ActionResponse response) {
        ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
        creator = configuratorCreatorRepo.find(creator.getId());
        configuratorCreatorService.updateAttributes(creator);
        configuratorCreatorService.updateIndicators(creator);
        configuratorCreatorService.activate(creator);
        response.setSignal("refresh-app", true);
    }
    

    /**
     * Called from the configurator creator form on new
     * @param request
     * @param response
     */
    public void authorizeCurrentUser(ActionRequest request, ActionResponse response) {
        ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
        creator = configuratorCreatorRepo.find(creator.getId());
        User currentUser = AuthUtils.getUser();
		configuratorCreatorService.authorizeUser(creator, currentUser);
        response.setReload(true);
    }
    
}
