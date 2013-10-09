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
package com.axelor.apps.account.service.invoice.generator.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTaxHistory;
import com.axelor.apps.account.db.PricingList;
import com.axelor.apps.account.db.PricingListVersion;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.apps.base.service.formula.Formula3Lvl;
import com.axelor.apps.base.service.formula.call.CalculationRuleTaxCall;
import com.axelor.apps.base.service.formula.call.ConditionTaxCall;
import com.axelor.apps.tool.DecimalTool;
import com.axelor.apps.tool.date.Period;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.apps.account.db.TaxLine;

/**
 * InvoiceLineTaxTransitionService est une classe implémentant l'ensemble des
 * services pour l'historisation des lignes de taxes d'une facture.
 * 
 */
public class TaxHistoryLine extends TaxGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(TaxHistoryLine.class);

	protected Formula3Lvl<BigDecimal, PricingListVersion, InvoiceLine, BigDecimal> formula3Lvl;

	public TaxHistoryLine(Invoice invoice, List<InvoiceLine> invoiceLines) {

		super(invoice, invoiceLines);
		
		this.formula3Lvl = CalculationRuleTaxCall.newInstance();
		
	}

//	@Override
//	public List<InvoiceLineTaxHistory> creates() throws AxelorException {
//
//		LOG.debug("Obtention des lignes de taxes => lignes de facture: {}, ajustement: {}", new Object[] { invoiceLines.size() });
//
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//		for (InvoiceLine invoiceLine : invoiceLines) {
//			
//			if (invoiceLine.getProduct() != null){
//				
//				int type = IInvoice.NONE;
//				if (invoiceLine.getTypeSelect() == IInvoice.FIX_ADP_TYPE) { type = IInvoice.FIX_ADP_TYPE; }
//				
//				Map<Tax, BigDecimal> exemptionTax = this.getExemptionTaxes(invoiceLine);
//				
//				for (Tax tax : getTaxes(invoice)) {
//	
//					LOG.debug("Test de la taxe {} => contient le produit: {}", new Object[] { tax.getName(), invoiceLine.getProduct().getCode() });
//	
//					if (tax.getConstituentSet().contains(invoiceLine.getConstituent())) {
//						
//						Period period = new Period();
//						if (invoiceLine.getTypeSelect() == IInvoice.HTA_TYPE || invoiceLine.getTypeSelect() == IInvoice.HTA_REF_TYPE){
//							period.setFrom(invoiceLine.getSpecificCommitmentLine().getFromDate());period.setTo(invoiceLine.getSpecificCommitmentLine().getToDate());
//						}
//						else {
//							period.setFrom(invoiceLine.getFromDate());period.setTo(invoiceLine.getToDate());
//						}
//						
//						invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(tax.getTaxLineList(), exemptionTax, invoiceLine, period, type));
//					}
//				}
//			}
//		}
//		
//		invoiceLineTaxTransitions.addAll(this.getAdjustments(invoice, invoice.getContractLine()));
//		
//		return invoiceLineTaxTransitions;
//	}

//	public List<InvoiceLineTaxHistory> getInvoiceTaxLineTransitions(List<TaxLine> taxeLines, 
//			Map<Tax, BigDecimal> exemptionTax, InvoiceLine invoiceLine, Period period, int type) throws AxelorException {
//
//		LOG.debug("Obtention des lignes de taxes => exonérations: {}, période: {}, type: {}", new Object[] { exemptionTax.size(), period, type });
//
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//		String baseOn = null;
//		
//		for (TaxLine taxLine : taxeLines) {
//
//			baseOn = taxLine.getCalculationRule().getBase().getBaseOnSelect();
//			
//			if (!invoiceLine.getExcludeQtyBaseOk() || (baseOn.equals("amount"))) {
//				invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(taxLine, exemptionTax, invoiceLine, period, baseOn, type));
//			}
//		}
//		
//		return invoiceLineTaxTransitions;
//	}

//	public List<InvoiceLineTaxHistory> getInvoiceTaxLineTransitions(TaxLine taxLine,
//			Map<Tax, BigDecimal> exemptionTax, InvoiceLine invoiceLine, Period period, String baseOn, int type) throws AxelorException {
//		
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//
//		LOG.debug("Obtention des lignes de taxes => ligne de taxe: {}, exonérations: {}, période: {}, basé sur: {}, type: {}", 
//			new Object[] { taxLine.getCode(), exemptionTax.size(), period, baseOn, type });
//		
//		Period periodProrata = period.prorata(taxLine.getFromDate(), taxLine.getToDate());
//		
//		if (periodProrata != null) {
//
//			LOG.debug("Condition sur la ligne de taxe {} : {}", new Object[] { taxLine.getCode(), taxLine.getCondition() });
//			
//			if (ConditionCalculationRuleTaxCall.condition().isRunnable(taxLine.getCode(), invoiceLine)) {
//				
//				LOG.debug("Test de la présence de la composante dans l'assiette {} => composante: {}", 
//					new Object[] { taxLine.getCalculationRule().getBase().getCode(), invoiceLine.getConstituent().getCode() });
//				
//				if (taxLine.getCalculationRule().getBase().getConstituentSet().contains(invoiceLine.getConstituent())) {
//					
//					invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(invoiceLine,
//							taxLine, taxLine.getPricingList().getPricingListVersionList(), exemptionTax, periodProrata, baseOn, taxLine.getProrata(), type));
//				}
//			}
//		}
//		
//		return invoiceLineTaxTransitions;
//	}

//	public List<InvoiceLineTaxHistory> getInvoiceTaxLineTransitions(InvoiceLine invoiceLine, TaxLine taxLine, List<PricingListVersion> pricingListVersions,
//			Map<Tax, BigDecimal> exemptionTax, Period period, String baseOn, boolean prorata, int type) throws AxelorException {
//
//		LOG.debug("Obtention des lignes de taxes => ligne de taxe: {}, versions de barème: {}, exonérations: {}, période: {}, basé sur: {}, proratisable: {}, type: {}", 
//			new Object[] { taxLine.getCode(), pricingListVersions.size(), exemptionTax.size(), period, baseOn, prorata, type });
//
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//		
//		for (PricingListVersion pricingListVersion : pricingListVersions) {
//			
//			if (pricingListVersion.getActiveOk()) {
//				
//				if (prorata) {
//					invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(invoiceLine,
//							taxLine, pricingListVersion, exemptionTax, period, baseOn, type));
//				} 
//				else if (period.toBetween(pricingListVersion.getFromDate(), pricingListVersion.getToDate())) {
//						
//					BigDecimal base = this.getBase(invoiceLine, taxLine,exemptionTax, period, baseOn);
//					InvoiceLineTaxHistory invoiceLineTaxTransition = this.createInvoiceTaxLineTransition(invoiceLine, taxLine,pricingListVersion, base, period.getFrom(), period.getTo(), baseOn, type);
//					
//					if (invoiceLineTaxTransition != null) {
//						invoiceLineTaxTransitions.add(invoiceLineTaxTransition);
//					}
//				}
//			}
//		}
//		
//		return invoiceLineTaxTransitions;
//	}

//	public List<InvoiceLineTaxHistory> getInvoiceTaxLineTransitions(InvoiceLine invoiceLine, TaxLine taxLine,
//			PricingListVersion pricingListVersion, Map<Tax, BigDecimal> exemptionTax, Period period, String baseOn, int type) throws AxelorException {
//		
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//
//		LOG.debug("Obtention des lignes de taxes => ligne de taxe: {}, version de barème: {}, exonérations: {}, période: {}, basé sur: {}, type: {}", 
//			new Object[] { taxLine.getCode(), pricingListVersion.getFullName(), exemptionTax.size(), period, baseOn, type });
//		
//		Period periodProrata = period.prorata(pricingListVersion.getFromDate(), pricingListVersion.getToDate());
//		
//		if (periodProrata != null) {
//			
//			BigDecimal base = this.getBase(invoiceLine, taxLine, exemptionTax, periodProrata, baseOn);
//						
//			InvoiceLineTaxHistory invoiceLineTaxTransition = this.createInvoiceTaxLineTransition(invoiceLine, taxLine,
//					pricingListVersion, base, periodProrata.getFrom(), periodProrata.getTo(), baseOn, type);
//			
//			if (invoiceLineTaxTransition != null) {
//				invoiceLineTaxTransitions.add(invoiceLineTaxTransition);
//			}
//			
//		}
//		
//		return invoiceLineTaxTransitions;
//	}

//	public InvoiceLineTaxHistory createInvoiceTaxLineTransition(InvoiceLine invoiceLine, TaxLine taxLine,
//			PricingListVersion pricingListVersion, BigDecimal base, LocalDate from, LocalDate to, String baseOn, int type) throws AxelorException {
//
//		LOG.debug("Création d'une ligne de taxe => ligne de taxe: {}, version de barème: {}, assiette: {}, période: {} - {}, basé sur: {}, type: {}", 
//			new Object[] { taxLine.getCode(), pricingListVersion.getFullName(), base,  from, to, baseOn, type });
//		
//		BigDecimal rateTax = formula3Lvl.compute(taxLine.getCalculationRule().getCode(), pricingListVersion, invoiceLine, base);
//
//		if (rateTax != null) {
//			
//			InvoiceLineTaxHistory invoiceLineTaxTransition = new InvoiceLineTaxHistory();
//			invoiceLineTaxTransition.setInvoice(invoice);
//			invoiceLineTaxTransition.setInvoiceLine(invoiceLine);
//			invoiceLineTaxTransition.setTypeSelect(type);
//			invoiceLineTaxTransition.setFromDate(from);
//			invoiceLineTaxTransition.setToDate(to);
//			invoiceLineTaxTransition.setTax(taxLine.getTax());
//			invoiceLineTaxTransition.setBase(base);
//			invoiceLineTaxTransition.setRateTax(rateTax);
//			invoiceLineTaxTransition.setVatLine( vatRate(taxLine, invoiceLine.getAmendment()) );
//			invoiceLineTaxTransition.setBaseOnSelect(baseOn);
//			invoiceLineTaxTransition.setPricingListVersion(pricingListVersion);
//				
//			invoiceLineTaxTransition.setUnit(taxLine.getCalculationRule().getUnit());
//			invoiceLineTaxTransition.setTaxLine(taxLine);
//			
//			formula3Lvl.resetDetails();
//			
//			return invoiceLineTaxTransition;
//			
//		} 
//		else { return null; }
//		
//	}

// Ajustement	
	
//	public List<InvoiceLineTaxHistory> getAdjustments(Invoice invoice, ContractLine contractLine) throws AxelorException {
//
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//		Invoice last = contractLine.getLastInvoice();
//		
//		if (last != null && last.getSubscriptionToDate() != null) {
//
//			LOG.debug("Obtention des lignes de facture d'ajustement dans le cas de l'ADP pour régularisation (Facture à régulariser : {})", last.getInvoiceName());
//				
//			List<InvoiceLineTaxHistory> adjustmentInvoiceLines = _adpTaxLines(last);
//			
//			if (!adjustmentInvoiceLines.isEmpty()){
//				
//				invoiceLineTaxTransitions.addAll(this.getAdjustments(invoice, last, adjustmentInvoiceLines, false));
//				
//			}
//				
//		}
//		
//		LOG.debug("{} lignes de taxes générées pour l'ajustement", invoiceLineTaxTransitions.size());
//		
//		return invoiceLineTaxTransitions;
//	}

//	public List<InvoiceLineTaxHistory> getAdjustments(Invoice invoice, Invoice last, List<InvoiceLineTaxHistory> adjustmentInvoiceLines, boolean adjustAgain) throws AxelorException {
//
//		LOG.debug("Obtention des lignes de taxes pour ajustement => dernière facture: {}", last.getInvoiceName());
//
//		List<InvoiceLineTaxHistory> invoiceLineTaxTransitions = new ArrayList<InvoiceLineTaxHistory>();
//		List<TaxLine> taxLines = new ArrayList<TaxLine>();
//		List<PricingListVersion> pricingListVersions = new ArrayList<PricingListVersion>();
//		Map<Tax, BigDecimal> exemptionTaxes = new HashMap<Tax, BigDecimal>();
//		
//		Period period = null;
//		
//		for (InvoiceLineTaxHistory adpTransition : adjustmentInvoiceLines) {
//			
//			period = new Period(adpTransition.getFromDate(), adpTransition.getToDate());
//			exemptionTaxes.clear();
//			exemptionTaxes.putAll(this.getExemptionTaxes(adpTransition.getInvoiceLine()));
//			
//			taxLines.clear();
//			taxLines.addAll( getUpdates(adpTransition.getTax(), period) );
//			
//			LOG.debug("Nombre de mise à jour de la taxe {} : {}", new Object[] { adpTransition.getTax().getCode(), taxLines.size() });
//			
//			if (_isUpdate(taxLines, adpTransition.getTaxLine())) {
//				
//				invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(taxLines, exemptionTaxes, adpTransition.getInvoiceLine(), period, IInvoice.FIX_ADP_TYPE));
//				invoiceLineTaxTransitions.add(this.refundInvoiceTaxLineTransition(adpTransition, invoice));
//				
//			}
//			else {
//				
//				pricingListVersions.clear();
//				pricingListVersions.addAll(getUpdates(adpTransition.getTaxLine().getPricingList(), period));
//	
//				LOG.debug("Nombre de mise à jour de la version de barème {} : {}", new Object[] {adpTransition.getTaxLine().getPricingList().getName(), pricingListVersions.size()});
//				
//				if (_isUpdate(pricingListVersions, adpTransition.getInvoiceLine().getPricingListVersion())) {
//						
//					invoiceLineTaxTransitions.addAll(this.getInvoiceTaxLineTransitions(adpTransition.getInvoiceLine(), 
//							adpTransition.getTaxLine(), pricingListVersions, exemptionTaxes, period, adpTransition.getTaxLine().getCalculationRule().getBase().getBaseOnSelect(), adpTransition.getTaxLine().getProrata(), IInvoice.FIX_ADP_TYPE));
//					
//					invoiceLineTaxTransitions.add(this.refundInvoiceTaxLineTransition(adpTransition, invoice));
//				}
//			}
//		}
//		
//		return invoiceLineTaxTransitions;
//	}
	
	
	private <T extends Model> boolean _isUpdate(List<T> list, T bean){
		
		if (list.size() > 1 || (list.size() == 1 && list.get(0).getId() != bean.getId())){
			return true;
		}
		else {return false;}
		
	}

	/**
	 * Rembourser une ligne d'historique de taxe d'une facture.
	 * 
	 * @param invoiceLineTaxTransition
	 *            La ligne d'historique de taxe.
	 * 
	 * @param invoice
	 *            La facture.
	 * 
	 * @return La ligne de taxe historisée.
	 */
	public InvoiceLineTaxHistory refundInvoiceTaxLineTransition(InvoiceLineTaxHistory invoiceLineTaxTransition, Invoice invoice) {

		InvoiceLineTaxHistory refundInvoiceLineTaxTransition = JPA.copy(invoiceLineTaxTransition, false);
		refundInvoiceLineTaxTransition.setInvoice(invoice);
		refundInvoiceLineTaxTransition.setRateTax(invoiceLineTaxTransition.getRateTax().negate());

		LOG.debug("Remboursement d'une ligne de taxe => ligne de taxe: {}, remboursé: {}", new Object[] {invoiceLineTaxTransition.getId(), refundInvoiceLineTaxTransition.getRateTax()});

		return refundInvoiceLineTaxTransition;
	}

	/**
	 * Calculer la valeur d'exonération d'une taxe.
	 * 
	 * @param tax
	 *            La taxe.
	 * 
	 * @param exemptionTax
	 *            Le dictionnaire contenant les taxes et leur valeur
	 *            d'exonérations associée.
	 * 
	 * @param value
	 *            La valeur à exonérée.
	 * 
	 * @return La valeur exonéré du pourcentage d'exonération.
	 */
	public BigDecimal computeExemptionValue(Tax tax, Map<Tax, BigDecimal> exemptionTax, BigDecimal value) {

		BigDecimal exemptionValue = value.setScale(2, RoundingMode.HALF_EVEN);

		if (exemptionTax.containsKey(tax)) { exemptionValue = exemptionValue.add( DecimalTool.percent(value, exemptionTax.get(tax), 2) ); }

		LOG.debug("Calcul de la valeur d'exonération de la taxe {} => valeur: {}, valeur exonérée: {}", new Object[] { tax.getCode(), value, exemptionValue });

		return exemptionValue;
	}

	/**
	 * Obtenir l'assiette d'une ligne de taxe.
	 * 
	 * @param invoiceLine
	 *            La ligne de facture.
	 * 
	 * @param taxLine
	 *            La ligne de taxe.
	 * 
	 * @param exemptionTax
	 *            Le dictionnaire contenant les taxes et leur valeur
	 *            d'exonérations associée.
	 * 
	 * @param from
	 *            De.
	 * 
	 * @param to
	 *            A.
	 * 
	 * @param baseOn
	 *            Ceux sur quoi est basé la taxe (la quantitué : 'quantity' ou
	 *            le montant : 'amount').
	 * 
	 * @return La valeur de l'assiette.
	 */
//	public BigDecimal getBase(InvoiceLine invoiceLine, TaxLine taxLine, Map<Tax, BigDecimal> exemptionTax, Period period, String baseOnSelect) {
//
//		BigDecimal value = BigDecimal.ZERO;
//		
//		if (invoiceLine.getConstituent().getTypeSelect() == IPricing.FIX_TYPE){
//			period.setDays360(months30days);
//		}
//			
//		BigDecimal days = new BigDecimal(period.getDays());
//		
//		if (baseOnSelect.equals("amount")) {
//			
//			value = DecimalTool.prorata(invoiceLine.getDaysNbr(), days, invoiceLine.getExTaxTotal()).setScale(2, RoundingMode.HALF_EVEN);
//			
//		} else if (baseOnSelect.equals("quantity")) {
//			
//			value = DecimalTool.prorata(invoiceLine.getDaysNbr(), days, invoiceLine.getQty()).setScale(0, RoundingMode.HALF_EVEN);
//			
//		} else if (baseOnSelect.equals("displayQuantity")) {
//			
//			value = DecimalTool.prorata(invoiceLine.getDaysNbr(), days, invoiceLine.getDisplayQty()).setScale(0, RoundingMode.HALF_EVEN);
//			
//		} else if (baseOnSelect.equals("hta") && invoiceLine.getSpecificCommitmentLine() != null) {
//			
//			BigDecimal totalDays = new BigDecimal( new Period( invoiceLine.getSpecificCommitmentLine().getFromDate(), invoiceLine.getSpecificCommitmentLine().getToDate() ).getDays() );
//			value = DecimalTool.prorata(totalDays, days, invoiceLine.getSpecificCommitmentLine().getQty()).setScale(0, RoundingMode.HALF_EVEN);
//			
//		}
//		
//		if (!exemptionTax.isEmpty()) { value = this.computeExemptionValue(taxLine.getTax(), exemptionTax, value); }
//
//		LOG.debug("Obtention de l'assiette de la ligne de taxe {} => basé sur: {}, assiette: {}, période: {}",
//			new Object[] { taxLine.getCode(), baseOnSelect, value, period });
//
//		return value;
//	}

	/**
	 * Obtenir les exonérations de taxes.
	 * 
	 * @param invoiceLine
	 *            La ligne de facture.
	 * 
	 * @return Les taxes et leur valeur d'exonération associée.
	 */
//	public Map<Tax, BigDecimal> getExemptionTaxes(InvoiceLine invoiceLine) {
//		
//		LOG.debug("Obtention des exonérations de taxes");
//		
//		Map<Tax, BigDecimal> exemptionTax = new HashMap<Tax, BigDecimal>();
//		
//		if (invoiceLine.getAmendment() != null && invoiceLine.getAmendment().getTaxExemptionOk()
//				&& invoiceLine.getAmendment().getExemptionFromTaxList() != null && !invoiceLine.getAmendment().getExemptionFromTaxList().isEmpty()){
//			
//			for (ExemptionFromTax exemption : invoiceLine.getAmendment().getExemptionFromTaxList()) {
//				exemptionTax.put(exemption.getTax(), exemption.getPercentage());
//			}
//			
//		}
//		
//		return exemptionTax;
//	}
	
	/**
	 * Obtenir les taxes applicables.
	 * 
	 * @param invoice
	 * 		La facture à taxer.
	 * 
	 * @return La liste des taxes applicables.
	 */
	protected List<Tax> getTaxes(Invoice invoice){
		
		List<Tax> taxes = new ArrayList<Tax>();
		
		for (Tax tax : Tax.all().fetch()){
			
			if (ConditionTaxCall.condition().isRunnable(tax.getCode(), invoice)) {
				
				LOG.debug("Taxe {} applicable", tax.getName());
				taxes.add(tax);
				
			}
			
		}
		
		return taxes;
	}

	protected List<PricingListVersion> getUpdates(PricingList pricingList, Period period) {
		
		List<PricingListVersion> pricingListVersions = new ArrayList<PricingListVersion>();
		
		if (pricingList.getPricingListVersionList().size() > 1){
			
			String sql = "pricingList = ?1 AND activeOk = true AND ((toDate = null AND fromDate >= ?2 AND fromDate < ?3) OR (toDate != null AND toDate > ?2 AND toDate <= ?3))";
			pricingListVersions.addAll(PricingListVersion.all().filter(sql,	pricingList, period.getFrom(), period.getTo()).fetch());
			
		}
		
		return pricingListVersions;
	}
	
	public List<TaxLine> getUpdates(Tax tax, Period period){
		
		List<TaxLine> taxLines = new ArrayList<TaxLine>();
		
		if (tax.getTaxLineList().size() > 1){
			
			String sql = "tax = ?1 AND ((toDate = null AND fromDate >= ?2 AND fromDate < ?3) OR (toDate != null AND toDate > ?2 AND toDate <= ?3))"; 
			taxLines.addAll(TaxLine.all().filter(sql, tax, period.getFrom(), period.getTo()).fetch());
			
		}
		
		return taxLines;

	}

	@Override
	public List<?> creates() throws AxelorException {
		// TODO Auto-generated method stub
		return null;
	}
	
}