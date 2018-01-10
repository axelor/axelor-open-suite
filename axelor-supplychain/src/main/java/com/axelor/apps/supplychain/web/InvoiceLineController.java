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
package com.axelor.apps.supplychain.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoiceLineController {
	
	@Inject
	private InvoiceLineRepository invoiceLineRepo;
	
	public void getProductPrice(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
		Integer parentPackPriceSelect = (Integer) context.getParent().get("packPriceSelect");
		
		if(invoiceLine.getPackPriceSelect() == InvoiceLineRepository.SUBLINE_PRICE_ONLY && invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_PACK) {
			response.setValue("price", 0.00);
		} else if (invoiceLine.getParentLine() != null || parentPackPriceSelect != null){
			if(parentPackPriceSelect != null) {
				if (parentPackPriceSelect != InvoiceLineRepository.SUBLINE_PRICE_ONLY && !invoiceLine.getIsSubLine()) {
					response.setValue("price", 0.00);
				}
			} else {
				response.setValue("price", 0.00);
			}
		} 
	}
	
	@Transactional
	public List<InvoiceLine> updateQty(List<InvoiceLine> invoiceLines, BigDecimal oldKitQty, BigDecimal newKitQty) {
		
		BigDecimal qty = BigDecimal.ZERO;
		
		if(invoiceLines != null) {
			if(newKitQty.compareTo(BigDecimal.ZERO) != 0) {
				for(InvoiceLine line : invoiceLines) {
					qty = (line.getQty().divide(oldKitQty, 2, RoundingMode.HALF_EVEN)).multiply(newKitQty);
					line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
				}
			} else {
				for(InvoiceLine line : invoiceLines) {
					line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
				}
			}
		}
		
		return invoiceLines;
	}
	
	public void updateSubLineQty(ActionRequest request, ActionResponse response) {
		
		InvoiceLine packLine = request.getContext().asType(InvoiceLine.class);
		BigDecimal oldKitQty = BigDecimal.ONE;
		BigDecimal newKitQty = BigDecimal.ZERO;
		List<InvoiceLine> subLines = null;
		
		if(packLine.getOldQty().compareTo(BigDecimal.ZERO) == 0) {
			if(packLine.getId() !=null) {
				InvoiceLine line = invoiceLineRepo.find(packLine.getId());
				if(line.getQty().compareTo(BigDecimal.ZERO) != 0) {
					oldKitQty = line.getQty();
				}
			}
		} else {	
			oldKitQty = packLine.getOldQty();
		}
			
		if(packLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
			newKitQty = packLine.getQty();
		} 
		
		if(packLine.getTypeSelect() == InvoiceLineRepository.TYPE_PACK) {
			subLines = this.updateQty(packLine.getSubLineList(), oldKitQty, newKitQty);
		}
		
		response.setValue("oldQty", newKitQty);
		response.setValue("subLineList", subLines);
	}
	
	public void resetSubLines(ActionRequest request, ActionResponse response) {
		
		InvoiceLine packLine = request.getContext().asType(InvoiceLine.class);
		List<InvoiceLine> subLines = packLine.getSubLineList();
		
		if(subLines != null) {
			for(InvoiceLine line : subLines) {
				line.setPrice(BigDecimal.ZERO);
				line.setPriceDiscounted(BigDecimal.ZERO);
				line.setExTaxTotal(BigDecimal.ZERO);
				line.setInTaxTotal(BigDecimal.ZERO);
			}
		}
		response.setValue("subLineList", subLines);
	}
}
