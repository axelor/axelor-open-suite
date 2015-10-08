package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.rpc.Context;

public class AnalyticDistributionLineServiceImpl implements AnalyticDistributionLineService{
	
	@Override
	public BigDecimal computeAmount(AnalyticDistributionLine analyticDistributionLine){
		BigDecimal amount = BigDecimal.ZERO;
		if(analyticDistributionLine.getInvoiceLine() != null){
			amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getInvoiceLine().getExTaxTotal()
					.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
		}
		if(analyticDistributionLine.getMoveLine() != null){
			if(analyticDistributionLine.getMoveLine().getCredit().compareTo(BigDecimal.ZERO) != 0){
				amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getMoveLine().getCredit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
			else{
				amount = analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getMoveLine().getDebit()
						.divide(new BigDecimal(100),2,RoundingMode.HALF_UP));
			}
		}
		return amount;
	}
	
	@Override
	public BigDecimal chooseComputeWay(Context context, AnalyticDistributionLine analyticDistributionLine){
		if(analyticDistributionLine.getInvoiceLine() == null && analyticDistributionLine.getMoveLine() == null){
			if(context.getParentContext().getContextClass() == InvoiceLine.class){
				analyticDistributionLine.setInvoiceLine(context.getParentContext().asType(InvoiceLine.class));
			}
			else{
				analyticDistributionLine.setMoveLine(context.getParentContext().asType(MoveLine.class));
			}
		}
		return this.computeAmount(analyticDistributionLine);
	}

}
