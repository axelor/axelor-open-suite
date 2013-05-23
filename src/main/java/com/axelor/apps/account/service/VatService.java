package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class VatService {

	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param vat
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public BigDecimal getVatRate(Vat vat, LocalDate localDate) throws AxelorException  {
		
		return this.getVatLine(vat, localDate).getValue();
	}
	
	
	/**
	 * Fonction permettant de récupérer le taux de TVA d'une TVA
	 * @param vat
	 * 			Une TVA
	 * @return
	 * 			Le taux de TVA
	 * @throws AxelorException
	 */
	public VatLine getVatLine(Vat vat, LocalDate localDate) throws AxelorException  {
		
		if (vat != null && vat.getVatLineList() != null && !vat.getVatLineList().isEmpty())  {
			
			for (VatLine vatLine : vat.getVatLineList()) {
				
				if (DateTool.isBetween(vatLine.getStartDate(), vatLine.getEndDate(), localDate)) {
					return vatLine;
				}
			}
		}
		
		throw new AxelorException(String.format("%s :\n Veuillez configurer une version de TVA pour la TVA %s",
			GeneralService.getExceptionAccountingMsg(), vat.getName()), IException.CONFIGURATION_ERROR);
	}

}