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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TrackingNumberService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TrackingNumberService.class); 

	@Inject
	private SequenceService sequenceService;
	
	private String exceptionMsg;
	
	@Inject
	public TrackingNumberService() {

		this.exceptionMsg = GeneralService.getExceptionAccountingMsg();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public TrackingNumber getTrackingNumber(Product product, BigDecimal sizeOfLot, Company company, LocalDate date) throws AxelorException  {
		
		TrackingNumber trackingNumber = TrackingNumber.filter("self.product = ?1 AND self.counter < ?2", product, sizeOfLot).fetchOne();
	
		if(trackingNumber == null)  {
			trackingNumber = this.createTrackingNumber(product, company, date).save();
		}
		
		trackingNumber.setCounter(trackingNumber.getCounter().add(sizeOfLot));
		
		return trackingNumber;
		
	}
	
	
	
	public String getOrderMethod(TrackingNumberConfiguration trackingNumberConfiguration)  {
		switch (trackingNumberConfiguration.getSaleAutoTrackingNbrOrderSelect()) {
			case IProduct.SALE_TRACKING_ORDER_FIFO:
				return " ORDER BY self.trackingNumber ASC";
				
			case IProduct.SALE_TRACKING_ORDER_LIFO:
				return " ORDER BY self.trackingNumber DESC";
	
			default:
				return "";
		}
	}
	
	

	public TrackingNumber createTrackingNumber(Product product, Company company, LocalDate date) throws AxelorException  {
		
		TrackingNumber trackingNumber = new TrackingNumber();
		
		if(product.getIsPerishable())  {
			trackingNumber.setPerishableExpirationDate(date.plusMonths(product.getPerishableNbrOfMonths()));
		}
		if(product.getHasWarranty())  {
			trackingNumber.setWarrantyExpirationDate(date.plusMonths(product.getWarrantyNbrOfMonths()));
		}
		
		trackingNumber.setProduct(product);
		trackingNumber.setCounter(BigDecimal.ZERO);
		
		TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
		
		if (trackingNumberConfiguration.getSequence() == null)  {
			throw new AxelorException(String.format("%s Aucune séquence configurée pour les Numéros de suivi pour le produit %s ",
					exceptionMsg, company.getName(), product.getCode()), IException.CONFIGURATION_ERROR);
		}
		
		String seq = sequenceService.getSequenceNumber(trackingNumberConfiguration.getSequence(), false);
		
		trackingNumber.setTrackingNumberSeq(seq);
		
		return trackingNumber;
	}
	
	
	
	
	
}
