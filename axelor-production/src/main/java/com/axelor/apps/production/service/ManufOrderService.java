/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ManufOrderService {

	public static int DEFAULT_PRIORITY = 10;
	public static int DEFAULT_PRIORITY_INTERVAL = 10;
	public static boolean IS_TO_INVOICE = false;
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ManufOrder generateManufOrder(Product product, BigDecimal qtyRequested, int priority, boolean isToInvoice, 
			BillOfMaterial billOfMaterial, LocalDateTime plannedStartDateT) throws AxelorException;
	
	
	public void createToConsumeProdProductList(ManufOrder manufOrder);
	
	
	public void createToProduceProdProductList(ManufOrder manufOrder);
		
	
	public ManufOrder createManufOrder(Product product, BigDecimal qty, int priority, boolean isToInvoice, Company company,
			BillOfMaterial billOfMaterial, LocalDateTime plannedStartDateT) throws AxelorException;
	
	@Transactional
	public void preFillOperations(ManufOrder manufOrder) throws AxelorException;
	
	
	public String getManufOrderSeq() throws AxelorException;
	
	public boolean isManagedConsumedProduct(BillOfMaterial billOfMaterial);
	
	public String getLanguageToPrinting(ManufOrder manufOrder);

}
