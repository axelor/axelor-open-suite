/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.stock.db.IStockMove;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class StockMoveLineServiceAccountOrganisationImpl extends StockMoveLineServiceImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveLineServiceAccountOrganisationImpl.class); 

	@Inject
	private LocationLineServiceAccountOrganisationImpl locationLineServiceAccountOrganisationImpl;
	
	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList, 
			LocalDate lastFutureStockMoveDate, Project businessProject, boolean realQty) throws AxelorException  {
		
		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			
			Unit productUnit = stockMoveLine.getProduct().getUnit();
			Unit stockMoveLineUnit = stockMoveLine.getUnit();
			
			BigDecimal qty = null;
			if(realQty)  {
				qty = stockMoveLine.getRealQty();
			}
			else  {
				qty = stockMoveLine.getQty();
			}
			
			if(!productUnit.equals(stockMoveLineUnit))  {
				qty = new UnitConversionService().convert(stockMoveLineUnit, productUnit, qty);
			}
			
			this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus, 
					lastFutureStockMoveDate, stockMoveLine.getTrackingNumber(), businessProject);
			
		}
		
	}
	
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate 
			lastFutureStockMoveDate, TrackingNumber trackingNumber, Project businessProject) throws AxelorException  {
		
		switch(fromStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineServiceAccountOrganisationImpl.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber, businessProject);
				locationLineServiceAccountOrganisationImpl.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber, businessProject);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineServiceAccountOrganisationImpl.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber, businessProject);
				locationLineServiceAccountOrganisationImpl.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber, businessProject);
				break;
			
			default:
				break;
		}
		
		switch(toStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineServiceAccountOrganisationImpl.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber, businessProject);
				locationLineServiceAccountOrganisationImpl.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber, businessProject);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineServiceAccountOrganisationImpl.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber, businessProject);
				locationLineServiceAccountOrganisationImpl.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber, businessProject);
				break;
			
			default:
				break;
		}
		
	}


	
	
	
	
}
