package com.axelor.apps.account.service.invoice.line.discount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.formula.call.ConditionDiscountCall;
import com.axelor.apps.base.service.formula.call.ConditionDiscountLineCall;
import com.axelor.apps.account.db.DiscountEngine;
import com.axelor.apps.account.db.DiscountEngineLine;
import com.axelor.apps.tool.DecimalTool;
import com.axelor.apps.tool.date.Period;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;

public class DiscountEngineService extends InvoiceLineGenerator {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(DiscountEngineService.class);
	
	private List<InvoiceLine> invoiceLines;
	private Unit amountUnit, percUnit;
	

	public DiscountEngineService(Invoice invoice, List<InvoiceLine> invoiceLines) { 
		super(invoice);
		this.invoiceLines = invoiceLines;
		this.amountUnit = Unit.all().filter("self.code = '€'").fetchOne();
		this.percUnit = Unit.all().filter("self.code = '%'").fetchOne();
	}
	
	protected List<DiscountEngine> discountEngines(){
		
		return DiscountEngine.all().filter("self.activeOk = ?1", true).fetch();
		
	}
	
	protected List<DiscountEngineLine> discountEngineLines( ){
		
		List<DiscountEngineLine> discountEngineLines = new ArrayList<DiscountEngineLine>();
		
		for ( DiscountEngine discountEngine : discountEngines() ) {
			
			if ( ConditionDiscountCall.condition().isRunnable(discountEngine.getCode(), invoice) ){
				
				discountEngineLines.addAll(discountEngine.getDiscountEngineLineList());
				
			}
			
		}
		
		return discountEngineLines;
	}
	
	protected List<DiscountEngineLine> discountEngineLines(  Partner partner, List<DiscountEngineLine> discountEngineLines ){
		
		List<DiscountEngineLine> discountEngineLines2 = new ArrayList<DiscountEngineLine>(discountEngineLines);
//		discountEngineLines2.addAll(partner.getDiscountEngineLineList());
		
		return _sortDiscounts(discountEngineLines2);
	}
	
	@Override
	public List<InvoiceLine> creates() throws AxelorException {
		
		LOG.debug("Obtention des lignes de remises");

		List<InvoiceLine> discountInvoiceLines = new ArrayList<InvoiceLine>();

		discountInvoiceLines.addAll( discountInvoiceLines( discountEngineLines() ) );
		
		LOG.debug("{} lignes de facture générées pour les remises", discountInvoiceLines.size());
		
		return discountInvoiceLines;
	}
	
	/**
	 * Fonction permettant de mettre à jour une liste de lignes de facture correspondant aux remises présentes dans un contrat.
	 * 
	 * @param contractLine
	 * @param invoiceLines
	 * 
	 */
	protected List<InvoiceLine> discountInvoiceLines (List<DiscountEngineLine> discountEngineLines){

		List<InvoiceLine> discountInvoiceLines = new ArrayList<InvoiceLine>();
//		Map<Partner, List<DiscountEngineLine>> map = new HashMap<Partner, List<DiscountEngineLine>>();
//		
//		LOG.debug("Application du moteur");
//		
//		for (InvoiceLine invoiceLine : invoiceLines){
//			
//			if (!map.containsKey(invoiceLine.getAmendment())) {  map.put( invoiceLine.getAmendment(), discountEngineLines(invoiceLine.getAmendment(), discountEngineLines) ); }
//			
//			discountInvoiceLines.addAll( discountInvoiceLines( map.get(invoiceLine.getAmendment()), invoiceLine ) );
//			
//		}
		
		return discountInvoiceLines;
	}
	
	/**
	 * Fonction permettant de mettre à jour une liste de lignes de facture correspondant aux remises présentes dans un contrat.
	 * 
	 * @param contractLine
	 * @param invoiceLines
	 * 
	 */
	protected List<InvoiceLine> discountInvoiceLines (List<DiscountEngineLine> discountEngineLines, InvoiceLine invoiceLine){

		List<InvoiceLine> discountInvoiceLines = new ArrayList<InvoiceLine>();
		BigDecimal initPrice = invoiceLine.getPrice(), discountPrice = initPrice, discount = BigDecimal.ZERO, garagedAmount = initPrice;
		boolean isPercentage = false, priceDiscount = false;
		
		for (DiscountEngineLine discountEngineLine : discountEngineLines){

			if (discountEngineLine.getActiveOk() && ConditionDiscountLineCall.condition().isRunnable(discountEngineLine.getCode(), discountEngineLine, invoiceLine)){
				
				LOG.debug("Application de la remise {} pour la ligne de facture {}", new Object[]{discountEngineLine.getCode(), invoiceLine});
				discount = discountEngineLine.getDiscountValue(); isPercentage = discountEngineLine.getDiscountTypeSelect() == 0; priceDiscount = discountEngineLine.getPriceDiscountOk();
				
				if ( isPercentage ) {
					
					if ( priceDiscount ){ garagedAmount = discountPrice;  }
					else { garagedAmount = initPrice; }
					
					discount = computeDiscount(discountEngineLine, invoiceLine, garagedAmount);
					
				}
				
				discountPrice = discountPrice.add(discount);
				
				if ( discountEngineLine.getNewLineOk() ) {
					
					if ( isPercentage ) { discountInvoiceLines.add( createDiscountInvoiceLine( discountEngineLine, invoiceLine, discount, garagedAmount ) ); }
					else { discountInvoiceLines.add( createDiscountInvoiceLine( discountEngineLine, invoiceLine, discount, amountUnit ) ); }
					
				}
				else { updateInvoiceLine (discountEngineLine, invoiceLine, discountPrice); }
				
				if ( discountEngineLine.getSingleApplicationOk() ){ singleApplication(discountEngineLine, invoiceLine); }
				
			}
			
		}
		
		return discountInvoiceLines;
	}
	
	protected void singleApplication ( DiscountEngineLine discountEngineLine, InvoiceLine invoiceLine ){
		
		if (invoiceLine.equals(invoiceLines.get(invoiceLines.size() - 1))){
			
//			if (discountEngineLine.getContractLineSet() == null){ discountEngineLine.setContractLineSet(new HashSet<ContractLine>()); }
//			discountEngineLine.getContractLineSet().add(invoiceLine.getAmendment().getContractLine());
			
		}
		
	}
	
	/**
	 * Fonction permettant de mettre à jour une ligne de facture avec la remise.
	 * 
	 * @param invoiceLine
	 * @param price
	 * @param contractDiscount
	 * 
	 */
	protected void updateInvoiceLine (DiscountEngineLine discountEngineLine, InvoiceLine invoiceLine, BigDecimal price){
		
		invoiceLine.setPrice(price);
		invoiceLine.setExTaxTotal(computeAmount(invoiceLine.getQty(),invoiceLine.getPrice()));
		invoiceLine.addDiscountEngineLineSetItem(discountEngineLine);
		
		LOG.debug("Mise à jour d'une ligne de facture (prix : {}, total : {})", new Object[] {price, invoiceLine.getExTaxTotal()});
		
	}
	
	/**
	 * Fonction permettant de créer une nouvelle ligne de facture. Cette ligne est négative en raison d'une remise.
	 * 
	 * @param invoiceLine
	 * @param price
	 * @param contractDiscount
	 * 
	 * @return la ligne de facture pour la remise.
	 */
	protected InvoiceLine createDiscountInvoiceLine (DiscountEngineLine discountEngineLine, InvoiceLine invoiceLine, BigDecimal price, BigDecimal garagedAmount){
				
		InvoiceLine invoiceLineDiscount = createDiscountInvoiceLine( discountEngineLine, invoiceLine, price, percUnit );
		
		return invoiceLineDiscount;
	}
	
	/**
	 * Fonction permettant de créer une nouvelle ligne de facture. Cette ligne est négative en raison d'une remise.
	 * 
	 * @param invoiceLine
	 * @param price
	 * @param contractDiscount
	 * 
	 * @return la ligne de facture pour la remise.
	 */
	protected InvoiceLine createDiscountInvoiceLine (DiscountEngineLine discountEngineLine, InvoiceLine invoiceLine, BigDecimal price, Unit displayUnit){
				
		InvoiceLine invoiceLineDiscount = JPA.copy(invoiceLine, false);
		
		if (!discountEngineLine.getSameProductOk() && discountEngineLine.getDiscountProduct() != null) {
			
			Product product = discountEngineLine.getDiscountProduct();
			invoiceLineDiscount.setProduct(product);
			invoiceLineDiscount.setProductName(product.getName());
			invoiceLineDiscount.setInvoiceLineType(product.getInvoiceLineType());
			
		}

		invoiceLineDiscount.setExcludeQtyBaseOk(true);
		invoiceLineDiscount.setDiscountOk(true);
		
		invoiceLineDiscount.setPrice( price );
		invoiceLineDiscount.setExTaxTotal(computeAmount(invoiceLineDiscount.getQty(),invoiceLineDiscount.getPrice()));
		
		invoiceLine.addDiscountEngineLineSetItem(discountEngineLine);
		invoiceLineDiscount.addDiscountEngineLineSetItem(discountEngineLine);

		LOG.debug("Création d'une nouvelle ligne de facture (prix : {}, total : {})", new Object[] {price, invoiceLine.getExTaxTotal()});
		
		return invoiceLineDiscount;
	}

	/**
	 * Fonction permettant de calculer le montant de la remise.
	 * Le taux est de la forme 19.6
	 * 
	 * @param value
	 * @param rate
	 * 
	 * @return le produit de valeur initial par le taux de remise.
	 */
	protected BigDecimal computeDiscount(DiscountEngineLine discountEngineLine, InvoiceLine invoiceLine, BigDecimal discount){
		
		BigDecimal discountPrice = DecimalTool.percent(discount, discountEngineLine.getDiscountValue(), 6);
		
		if (discountEngineLine.getReferenceDateSelect() == 1) { 
//			discountPrice =  prorataDiscount(
//					new Period(discountEngineLine.getFromDate(), discountEngineLine.getToDate()), 
//					new Period(invoiceLine.getFromDate(), invoiceLine.getToDate()), 
//					discountPrice
//			);
		}
		
		return discountPrice;
	}
	
	protected BigDecimal prorataDiscount(Period discountPeriod, Period invoiceLinePeriod, BigDecimal value){
		
		Period period = invoiceLinePeriod.prorata(discountPeriod);
		if (period != null){
			
			return DecimalTool.prorata(new BigDecimal(invoiceLinePeriod.getDays()), new BigDecimal(period.getDays()), value, 6);
			
		}
		
		return BigDecimal.ZERO;
		
	}
	
	/**
	 * Trier une liste de remise.
	 * 
	 * @param contractDiscounts
	 */
	private List<DiscountEngineLine> _sortDiscounts(List<DiscountEngineLine> discountEngineLines){
		
		Collections.sort(discountEngineLines, new Comparator<DiscountEngineLine>() {
			
			@Override
			public int compare(DiscountEngineLine o1, DiscountEngineLine o2) {
				if (o1.getDiscountEngineId().compareTo(o2.getDiscountEngineId()) == -1) { return -1; }
				if (o1.getDiscountEngineId().compareTo(o2.getDiscountEngineId()) == 1) { return 1; }
				return 0;
			}
			
		});
		
		return discountEngineLines;
	}

}
