package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaskService {

	public BigDecimal computeMargin(BigDecimal ca, BigDecimal cost) {
		
		BigDecimal marginBrut = ca.subtract(cost);
		
		if(marginBrut.compareTo(BigDecimal.ZERO) > 0) {
			
			return marginBrut.multiply(new BigDecimal(100)).setScale(6, RoundingMode.HALF_EVEN).divide(ca, 6, RoundingMode.HALF_EVEN);
		}
		else if(marginBrut.compareTo(BigDecimal.ZERO) == 0)
			return marginBrut;
		else {
			BigDecimal absMarginBrut = marginBrut.abs();
			BigDecimal absMargin = absMarginBrut.multiply(new BigDecimal(100)).setScale(6, RoundingMode.HALF_EVEN).divide(cost, 6, RoundingMode.HALF_EVEN);
			return absMargin.negate();
		}
	}
}
