package com.axelor.apps.account.service.invoice.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.tool.date.Period;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

/**
 * Classe de création de ligne de facture abstraite.
 * 
 * @author guerrier
 *
 */
public abstract class InvoiceLineGenerator extends InvoiceLineManagement {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceLineGenerator.class);
	
	@Inject
	private AccountManagementService acs;
	
	
	
	protected Invoice invoice;
	protected int type;
	
	protected InvoiceLineGenerator() { }
	
	protected InvoiceLineGenerator(int type) {
		
		this.type = type;
		
	}
	
	protected InvoiceLineGenerator( Invoice invoice ) {

        this.invoice = invoice;
        
    }
	
	protected InvoiceLineGenerator( Invoice invoice, int type ) {

        this.invoice = invoice;
        this.type = type;
        
    }
	
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	abstract public List<InvoiceLine> creates() throws AxelorException ;
	
	protected InvoiceLine createLine(Company company, Product product, 
			BigDecimal qty, BigDecimal price, Unit unit, Unit pricingListUnit, 
			boolean isTaxInvoice, boolean isPurchase) throws AxelorException {
		
		return createLine(company, product, qty, price, unit, pricingListUnit, isTaxInvoice, type, isPurchase);
		
	}

	protected InvoiceLine createLine(Company company, Product product,  
			BigDecimal qty, BigDecimal price, Unit unit, Unit pricingListUnit, 
			boolean isTaxInvoice, int type, boolean isPurchase) throws AxelorException {
		
		InvoiceLine invoiceLine = new InvoiceLine();

		if (isTaxInvoice) { invoiceLine.setTaxInvoice(invoice); }
		else { invoiceLine.setInvoice(invoice); }

		invoiceLine.setProduct(product);
		invoiceLine.setProductName(product.getName());
		invoiceLine.setInvoiceLineType(product.getInvoiceLineType());
		invoiceLine.setQty(qty);
		invoiceLine.setPrice(price);
		invoiceLine.setPricingListUnit(pricingListUnit);
		invoiceLine.setExTaxTotal(computeAmount(qty, price));

		invoiceLine.setVatLine(acs.getVatLine(invoice.getInvoiceDate(), product, company, isPurchase));

		return invoiceLine;
	}
	
	/**
	 * Rembourser une ligne de facture.
	 * 
	 * @param invoice
	 * 		La facture concernée.
	 * @param invoiceLine
	 *      La ligne de facture.
	 * 
	 * @return 
	 * 			La ligne de facture de remboursement.
	 */
	protected InvoiceLine refundInvoiceLine(InvoiceLine invoiceLine, Period period, int typeSelect, boolean daysQty) {

		LOG.debug("Remboursement d'une ligne de facture (quantité = nb jour ? {}).", daysQty);
		
		BigDecimal qty = invoiceLine.getQty();
		
		InvoiceLine refundInvoiceLine = JPA.copy(invoiceLine, true);
		
		if (invoiceLine.getInvoice() != null) {	 refundInvoiceLine.setInvoice(invoice); }
		if (invoiceLine.getTaxInvoice() != null) {	 refundInvoiceLine.setTaxInvoice(invoice); }
		
		qty =  invoiceLine.getQty() ; 
		
		refundInvoiceLine.setQty( qty.negate() );
		
		LOG.debug( "Quantité remboursée : {}", refundInvoiceLine.getQty() );
		
		refundInvoiceLine.setExTaxTotal( computeAmount( refundInvoiceLine.getQty(), refundInvoiceLine.getPrice() ) );

		LOG.debug("Remboursement de la ligne de facture {} => montant HT: {}", new Object[] { invoiceLine.getId(), refundInvoiceLine.getExTaxTotal() });

		return refundInvoiceLine;
	}
	
	
	protected List<InvoiceLine> getInvoiceLines(Invoice invoice, boolean exclude, Integer... types){
		
		return getInvoiceLines(invoice.getInvoiceLineList(), exclude, false, types);
		
	}
	
	protected List<InvoiceLine> getInvoiceLines(List<InvoiceLine> invoiceLines, boolean exclude, boolean excludeDiscount, Integer... types){
		
		List<InvoiceLine> res = new ArrayList<InvoiceLine>();
		List<Integer> typeList = null;
		
		if (types != null) { typeList = Arrays.asList(types); }
		
		for (InvoiceLine invoiceLine : invoiceLines) {
			
//			if ( typeList == null || ( !exclude && typeList.contains( invoiceLine.getTypeSelect() ) ) || ( exclude && !typeList.contains( invoiceLine.getTypeSelect() ) ) ) {
//				
//				if ( excludeDiscount && invoiceLine.getDiscountOk() ) { continue; } 
//				else { res.add( invoiceLine ); }
//			}
			
		}
		
		return res;
		
	}
	
	protected InvoiceLine substractInvoiceLine(InvoiceLine invoiceLine1, InvoiceLine invoiceLine2){
		
		InvoiceLine substract = JPA.copy(invoiceLine1, false);
		
		substract.setQty(invoiceLine1.getQty().add(invoiceLine2.getQty()));
		substract.setExTaxTotal( computeAmount( substract.getQty(), substract.getPrice() ) );

		LOG.debug("Soustraction de deux lignes de factures: {}", substract);
		
		return substract;
		
	}

	/**
	 * Convertir le prix d'une unité de départ vers une unité d'arrivée.
	 * 
	 * @param price
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le prix converti
	 */
	protected BigDecimal convertPrice(BigDecimal price, Unit startUnit, Unit endUnit) {

		BigDecimal convertPrice = convert(startUnit, endUnit, price);

		LOG.debug("Conversion du prix {} {} : {} {}", new Object[] { price, startUnit, convertPrice, endUnit });

		return convertPrice;
	}

	/**
	 * Récupérer la bonne unité.
	 * 
	 * @param unit
	 * 		Unité de base.
	 * @param unitDisplay
	 * 		Unité à afficher.
	 * 
	 * @return  L'unité à utiliser.
	 */
	protected Unit unit(Unit unit, Unit displayUnit) {

		Unit resUnit = unit;

		if (displayUnit != null) { resUnit = displayUnit; }

		LOG.debug("Obtention de l'unité : Unité {}, Unité affichée {} : {}", new Object[] { unit, displayUnit, resUnit });

		return resUnit;

	}
	
// HELPER
	
	/**
	 * Convertir le prix d'une unité de départ version une unité d'arrivée.
	 * 
	 * @param price
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le prix converti
	 */
	protected BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value) {
		 
		if (value == null || startUnit == null || endUnit == null || startUnit.equals(endUnit)) { return value; }
		else { return value.multiply(convertCoef(startUnit, endUnit)).setScale(6, RoundingMode.HALF_EVEN); }
		
	}

	/**
	 * Obtenir le coefficient de conversion d'une unité de départ vers une unité d'arrivée.
	 * 
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le coefficient de conversion.
	 */
	protected BigDecimal convertCoef(Unit startUnit, Unit endUnit){
		
		UnitConversion unitConversion = UnitConversion.all().filter("self.startUnit = ?1 AND self.endUnit = ?2", startUnit, endUnit).fetchOne();
		
		if (unitConversion != null){ return unitConversion.getCoef(); }
		else { return BigDecimal.ONE; }
		
	}
	
	
	
	
	
	protected void addAlarm( Alarm alarm, Partner partner ) {
		
		if ( alarm != null ) {
			
			alarm.setInvoice(invoice);
			alarm.setPartner(partner);
			
		}
		
	}
	
}