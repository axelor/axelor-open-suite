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
package com.axelor.apps.suppliermanagement.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.suppliermanagement.db.IPurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.PurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.repo.PurchaseOrderSupplierLineRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderSupplierLineService {
	
	@Inject
	PurchaseOrderSupplierLineRepository poSupplierLineRepo;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void accept(PurchaseOrderSupplierLine purchaseOrderSupplierLine)  {
		
		PurchaseOrderLine purchaseOrderLine = purchaseOrderSupplierLine.getPurchaseOrderLine();

		purchaseOrderLine.setEstimatedDelivDate(purchaseOrderSupplierLine.getEstimatedDelivDate());
		purchaseOrderLine.setSupplierPartner(purchaseOrderSupplierLine.getSupplierPartner());
		
		purchaseOrderLine.setPrice(purchaseOrderSupplierLine.getPrice());
		purchaseOrderLine.setExTaxTotal(PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLine.getPrice()));
		
		purchaseOrderSupplierLine.setStateSelect(IPurchaseOrderSupplierLine.ACCEPTED);
		
		poSupplierLineRepo.save(purchaseOrderSupplierLine);
		
	}
	
	
	public PurchaseOrderSupplierLine create(Partner supplierPartner, BigDecimal price)  {
		
		return new PurchaseOrderSupplierLine(price, IPurchaseOrderSupplierLine.REQUESTED, supplierPartner);
	}
	
	
	
}
