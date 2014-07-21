/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.stock.db.IMinStockRules;
import com.axelor.apps.stock.service.LocationLineServiceImpl;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class LocationLineServiceAccountOrganisationImpl extends LocationLineServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationLineServiceAccountOrganisationImpl.class); 
	
	@Inject
	private MinStockRulesServiceAccountOrganisationImpl minStockRulesServiceAccountOrganisationImpl;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, Project businessProject) throws AxelorException  {
		
		this.updateLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, businessProject);
		
		if(trackingNumber != null)  {
			this.updateDetailLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, Project businessProject) throws AxelorException  {
		
		LocationLine locationLine = this.getLocationLine(location, product);
		
		LOG.debug("Mise à jour du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate });
		
		if(!isIncrement)  {
			this.minStockRules(product, qty, locationLine, businessProject, current, future);
		}
		
		locationLine = this.updateLocation(locationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		this.checkStockMin(locationLine, false);
		
		locationLine.save();
		
	}
	
	
	public void minStockRules(Product product, BigDecimal qty, LocationLine locationLine, Project businessProject, boolean current, boolean future) throws AxelorException  {
		
		minStockRulesServiceAccountOrganisationImpl.setProject(businessProject);
		
		if(current)  {
			minStockRulesServiceAccountOrganisationImpl.generatePurchaseOrder(product, qty, locationLine, IMinStockRules.TYPE_CURRENT);			
		}
		if(future)  {
			minStockRulesServiceAccountOrganisationImpl.generatePurchaseOrder(product, qty, locationLine, IMinStockRules.TYPE_FUTURE);
		}
		
	}
	
	
	
	
	
	
	
	
		
}
