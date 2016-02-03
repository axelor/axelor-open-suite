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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AnalyticDistributionLineServiceImpl;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;

public class AnalyticDistributionLineServiceSupplychainImpl extends AnalyticDistributionLineServiceImpl{
	
	@Override
	public BigDecimal computeAmount(AnalyticDistributionLine analyticDistributionLine){
		BigDecimal amount = BigDecimal.ZERO;
		if(analyticDistributionLine.getPurchaseOrderLine() != null){
			amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getPurchaseOrderLine().getExTaxTotal()
					.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		if(analyticDistributionLine.getSaleOrderLine() != null){
			amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getSaleOrderLine().getExTaxTotal()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		
		if(amount.compareTo(BigDecimal.ZERO) == 0){
			return super.computeAmount(analyticDistributionLine);
		}
		return amount;
	}
	
	@Override
	public BigDecimal chooseComputeWay(Context context, AnalyticDistributionLine analyticDistributionLine){
		if(analyticDistributionLine.getPurchaseOrderLine() == null && analyticDistributionLine.getSaleOrderLine() == null){
			if(context.getParentContext().getContextClass() == PurchaseOrderLine.class){
				analyticDistributionLine.setPurchaseOrderLine(context.getParentContext().asType(PurchaseOrderLine.class));
			}
			else if(context.getParentContext().getContextClass() == InvoiceLine.class){
				analyticDistributionLine.setInvoiceLine(context.getParentContext().asType(InvoiceLine.class));
			}
			else if(context.getParentContext().getContextClass() == MoveLine.class){
				analyticDistributionLine.setMoveLine(context.getParentContext().asType(MoveLine.class));
			}
			else{
				analyticDistributionLine.setSaleOrderLine(context.getParentContext().asType(SaleOrderLine.class));
			}
		}
		return this.computeAmount(analyticDistributionLine);
	}
}
