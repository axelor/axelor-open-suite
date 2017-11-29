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
package com.axelor.apps.supplychain.service.config;

import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class StockConfigSupplychainService extends StockConfigService {

    public Location getReceiptLocation(StockConfig stockConfig) throws AxelorException {

        if (stockConfig.getReceiptDefaultLocation() == null) {
            throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_SUPPLYCHAIN_RECEIPT), stockConfig.getCompany().getName());
        }

        return stockConfig.getReceiptDefaultLocation();

    }

    public Location getPickupLocation(StockConfig stockConfig) throws AxelorException {
        if (stockConfig.getPickupDefaultLocation() == null) {
            throw new AxelorException(stockConfig, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_CONFIG_SUPPLYCHAIN_PICKUP), stockConfig.getCompany().getName());
        }
        return stockConfig.getPickupDefaultLocation();
    }

}
