/**
 * Axelor Business Solutions
 * <p>
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.repo.ConfiguratorBOMRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.ConfiguratorBomService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;

public class ConfiguratorController {

    ConfiguratorBomService configuratorBomService;

    @Inject
    ConfiguratorController(ConfiguratorBomService configuratorBomService) {
        this.configuratorBomService = configuratorBomService;
    }

    /**
     * Called from configurator view.
     * Call {@link ConfiguratorBomService#generateBillOfMaterial}
     * @param request
     * @param response
     */
    public void generateBillOfMaterial(ActionRequest request,
                                       ActionResponse response) {
        Configurator configurator = request.getContext().asType(Configurator.class);
        JsonContext jsonAttributes = (JsonContext)
                request.getContext().get("$attributes");
        ConfiguratorBOM configuratorBOM = configurator.getConfiguratorBom();
        configuratorBOM = Beans.get(ConfiguratorBOMRepository.class)
                .find(configuratorBOM.getId());
        try {
            BillOfMaterial billOfMaterial = configuratorBomService
                    .generateBillOfMaterial(configuratorBOM, jsonAttributes);
            if (billOfMaterial != null) {
                response.setAlert(String.format(
                        I18n.get(IExceptionMessage.BILL_OF_MATERIAL_GENERATED),
                        billOfMaterial.getName()
                        )
                );
            }
        } catch (AxelorException e) {
            TraceBackService.trace(e);
            response.setError(e.getMessage());
        }
    }
}
