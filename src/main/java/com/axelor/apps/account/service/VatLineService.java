package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatLine;

public class VatLineService {

	public VatLine createDefault(Vat vat){
		 
		VatLine vatLine = new VatLine();
		vatLine.setVat(vat);
		vatLine.setStartDate(new LocalDate());
		vatLine.setEndDate((new LocalDate()).plusYears(100));
		vatLine.setValue(new BigDecimal("0.196"));
		return vatLine;
	}

	
}