/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.invoice.generator;

import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.service.formula.call.VatFormulaCall;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * InvoiceLineTaxService est une classe implémentant l'ensemble des services
 * pour les lignes de taxes des factures.
 * 
 * @author Cédric Guerrier
 * 
 * @version 1.0
 */
public abstract class TaxGenerator extends InvoiceLineManagement {
	
	protected Invoice invoice;
	protected List<InvoiceLine> invoiceLines;

	protected  TaxGenerator (Invoice invoice, List<InvoiceLine> invoiceLines) {
		
		this.invoice = invoice;
		this.invoiceLines = invoiceLines;
		
	}
	
	/**
	 * Obtenir la TVA d'une taxe.
	 * 
	 * @param taxLine
	 * @param contractLine
	 * @return
	 * @throws AxelorException 
	 */
	public Vat vat(TaxLine taxLine) throws AxelorException {
		
		Vat vat = null;
		
		if (taxLine.getDefaultVat() != null) { vat = taxLine.getDefaultVat(); }
		else if (taxLine.getVatManagement() != null) {
			vat = VatFormulaCall.formula().compute(taxLine.getVatManagement().getCode(), taxLine.getVatManagement().getVatManagementLineList());
		}

		if (vat == null) { throw new AxelorException(String.format("Aucune TVA trouvé pour la ligne de taxe %s", taxLine.getCode()), IException.CONFIGURATION_ERROR); }
		else { return vat; }
		
		
	}

	/**
	 * Obtenir le taux de TVA d'une taxe.
	 * 
	 * @param taxLine
	 * @param contractLine
	 * @return
	 * @throws AxelorException 
	 */
	protected VatLine vatRate(TaxLine taxLine) throws AxelorException {

		for (VatLine vatLine : (vat(taxLine)).getVatLineList()) {
			
			if (DateTool.isBetween(vatLine.getStartDate(), vatLine.getEndDate(), invoice.getInvoiceDate())) {
				
				return vatLine;
				
			}
		}

		throw new AxelorException(String.format("Aucune TVA trouvé pour la ligne de taxe %s", taxLine.getCode()), IException.CONFIGURATION_ERROR);
		
	}
}