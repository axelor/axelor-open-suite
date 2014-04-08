/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
		
		TrackingNumber trackingNumber = TrackingNumber.all().filter("self.product = ?1 AND self.counter < ?2", product, sizeOfLot).fetchOne();
	
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
		
		String seq = sequenceService.getSequence(IAdministration.PRODUCT_TRACKING_NUMBER, product, company, false);
		if (seq == null)  {
			throw new AxelorException(String.format("%s Aucune séquence configurée pour les Numéros de suivi pour la société %s et le produit %s ",
					exceptionMsg, company.getName(), product.getCode()), IException.CONFIGURATION_ERROR);
		}
		
		trackingNumber.setTrackingNumberSeq(seq);
		
		return trackingNumber;
	}
	
	
	
	
	
}
