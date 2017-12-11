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
package com.axelor.apps.production.service.config;

import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.config.StockConfigSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class StockConfigProductionService extends StockConfigSupplychainService {

    public StockLocation getProductionVirtualLocation(StockConfig stockConfig) throws AxelorException {

        if (stockConfig.getProductionVirtualLocation() == null) {
            throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PRODUCTION_CONFIG_2), stockConfig.getCompany().getName());
        }

        return stockConfig.getProductionVirtualLocation();

    }

    public StockLocation getWasteLocation(StockConfig stockConfig) throws AxelorException {
        if (stockConfig.getWasteLocation() == null) {
            throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PRODUCTION_CONFIG_3), stockConfig.getCompany().getName());
        }
        return stockConfig.getWasteLocation();
    }

}
