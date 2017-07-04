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

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.service.ConfiguratorService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import wslite.json.JSONException;

public class ConfiguratorController {

    private ConfiguratorRepository configuratorRepository;
    private ConfiguratorService configuratorService;

    @Inject
    ConfiguratorController(ConfiguratorRepository configuratorRepository,
                           ConfiguratorService configuratorService) {
        this.configuratorRepository = configuratorRepository;
        this.configuratorService = configuratorService;
    }

    public void updateIndicators(ActionRequest request, ActionResponse response) throws JSONException {
        Configurator configurator = request.getContext().asType(Configurator.class);
        configurator = configuratorRepository.find(configurator.getId());
        String indicators = configuratorService.updateIndicators(configurator);
        response.setValue("indicators", indicators);
    }
}
