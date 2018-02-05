/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import java.math.RoundingMode;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineServiceImpl;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;

public class AnalyticMoveLineServiceSupplychainImpl extends AnalyticMoveLineServiceImpl{
	
	public AnalyticMoveLineServiceSupplychainImpl(GeneralService generalService, AnalyticMoveLineRepository analyticMoveLineRepository) {
		super(generalService, analyticMoveLineRepository);
	}

	@Override
	public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine){
		BigDecimal amount = BigDecimal.ZERO;
		if(analyticMoveLine.getPurchaseOrderLine() != null){
			amount = analyticMoveLine.getPercentage().multiply(analyticMoveLine.getPurchaseOrderLine().getExTaxTotal()
					.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		if(analyticMoveLine.getSaleOrderLine() != null){
			amount = analyticMoveLine.getPercentage().multiply(analyticMoveLine.getSaleOrderLine().getExTaxTotal()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		
		if(amount.compareTo(BigDecimal.ZERO) == 0){
			return super.computeAmount(analyticMoveLine);
		}
		return amount;
	}
	
	@Override
	public BigDecimal chooseComputeWay(Context context, AnalyticMoveLine analyticMoveLine){
		if(analyticMoveLine.getPurchaseOrderLine() == null && analyticMoveLine.getSaleOrderLine() == null){
			if(context.getParentContext().getContextClass() == PurchaseOrderLine.class){
				analyticMoveLine.setPurchaseOrderLine(context.getParentContext().asType(PurchaseOrderLine.class));
			}
			else if(context.getParentContext().getContextClass() == InvoiceLine.class){
				analyticMoveLine.setInvoiceLine(context.getParentContext().asType(InvoiceLine.class));
			}
			else if(context.getParentContext().getContextClass() == MoveLine.class){
				analyticMoveLine.setMoveLine(context.getParentContext().asType(MoveLine.class));
			}
			else{
				analyticMoveLine.setSaleOrderLine(context.getParentContext().asType(SaleOrderLine.class));
			}
		}
		return this.computeAmount(analyticMoveLine);
	}
}
