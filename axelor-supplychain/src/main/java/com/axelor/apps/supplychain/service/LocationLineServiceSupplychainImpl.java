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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.LocationLineServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LocationLineServiceSupplychainImpl extends LocationLineServiceImpl {

    public void checkStockMin(LocationLine locationLine, boolean isDetailLocationLine) throws AxelorException {
        super.checkStockMin(locationLine, isDetailLocationLine);
        if (!isDetailLocationLine && locationLine.getCurrentQty().compareTo(locationLine.getReservedQty()) < 0 && locationLine.getLocation().getTypeSelect() != LocationRepository.TYPE_VIRTUAL) {
            throw new AxelorException(locationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_1), locationLine.getProduct().getName(), locationLine.getProduct().getCode());

        } else if (isDetailLocationLine && locationLine.getCurrentQty().compareTo(locationLine.getReservedQty()) < 0
                && ((locationLine.getLocation() != null && locationLine.getLocation().getTypeSelect() != LocationRepository.TYPE_VIRTUAL)
                || (locationLine.getDetailsLocation() != null && locationLine.getDetailsLocation().getTypeSelect() != LocationRepository.TYPE_VIRTUAL))) {

            String trackingNumber = "";
            if (locationLine.getTrackingNumber() != null) {
                trackingNumber = locationLine.getTrackingNumber().getTrackingNumberSeq();
            }

            throw new AxelorException(locationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_2), locationLine.getProduct().getName(), locationLine.getProduct().getCode(), trackingNumber);
        }
    }

    public LocationLine updateLocation(LocationLine locationLine, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
                                       LocalDate lastFutureStockMoveDate, BigDecimal reservedQty) {

        locationLine = super.updateLocation(locationLine, qty, current, future, isIncrement, lastFutureStockMoveDate, reservedQty);
        if (current) {
            if (isIncrement) {
                locationLine.setReservedQty(locationLine.getReservedQty().add(reservedQty));
            } else {
                locationLine.setReservedQty(locationLine.getReservedQty().subtract(reservedQty));
            }
        }
		if(future)  {
			if(isIncrement)  {
				locationLine.setReservedQty(locationLine.getReservedQty().subtract(reservedQty));
			} else {
				locationLine.setReservedQty(locationLine.getReservedQty().add(reservedQty));
			}
			locationLine.setLastFutureStockMoveDate(lastFutureStockMoveDate);
		}
        return locationLine;
    }

    public void checkIfEnoughStock(Location location, Product product, BigDecimal qty) throws AxelorException{
        super.checkIfEnoughStock(location, product, qty);

        if (Beans.get(AppSupplychainService.class).getAppSupplychain().getManageStockReservation()) {
            LocationLine locationLine = this.getLocationLine(location.getLocationLineList(), product);
            if (locationLine != null && locationLine.getCurrentQty().subtract(locationLine.getReservedQty()).compareTo(qty) < 0) {
                throw new AxelorException(locationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_1), locationLine.getProduct().getName(), locationLine.getProduct().getCode());
            }
        }
    }
}
