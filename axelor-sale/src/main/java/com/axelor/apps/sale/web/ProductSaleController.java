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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.service.ConfiguratorService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProductSaleController {

    private ConfiguratorService configuratorService;

    @Inject
    ProductSaleController(ConfiguratorService configuratorService) {
        this.configuratorService = configuratorService;
    }

    public void showConfigurator (ActionRequest request, ActionResponse response) {
        Product product = request.getContext().asType(Product.class);
        Configurator configurator = configuratorService.getConfiguratorFromProduct(product);
        if (configurator == null) {
            response.setAlert(I18n.get(IExceptionMessage.CONFIGURATOR_NOT_FOUND));
        }
        else {
            response.setView(ActionView
                    .define(I18n.get("Configurator"))
                    .model("com.axelor.apps.sale.db.Configurator")
                    .add("form", "configurator-form")
                    .context("_showRecord", configurator.getId())
                    .map()
            );
        }
    }
}
