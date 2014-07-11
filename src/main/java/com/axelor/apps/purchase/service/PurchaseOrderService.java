/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.supplychain.db.SupplychainConfig;
import com.axelor.apps.supplychain.service.config.SupplychainConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderService.class); 

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PurchaseOrderLineTaxService purchaseOrderLineVatService;
	
	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveLineService stockMoveLineService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private SupplychainConfigService supplychainConfigService;
	
	
	public PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		if(purchaseOrder.getPurchaseOrderLineList() != null)  {
			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
				purchaseOrderLine.setExTaxTotal(purchaseOrderLineService.computePurchaseOrderLine(purchaseOrderLine));
				purchaseOrderLine.setCompanyExTaxTotal(purchaseOrderLineService.getCompanyExTaxTotal(purchaseOrderLine.getExTaxTotal(), purchaseOrder));
			}
		}
		
		return purchaseOrder;
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		this.initPurchaseOrderLineVats(purchaseOrder);
		
		this._computePurchaseOrderLines(purchaseOrder);
		
		this._populatePurchaseOrder(purchaseOrder);
		
		this._computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.save();
	}
	
	/**
	 * Peupler une commande.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'une commande à partir des lignes de factures passées en paramètres. 
	 * </p>
	 * 
	 * @param purchaseOrder
	 * 
	 * @throws AxelorException
	 */
	public void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de devis: {} ", new Object[] { purchaseOrder.getPurchaseOrderLineList().size() });
		
		// create Tva lines
		purchaseOrder.getPurchaseOrderLineTaxList().addAll(purchaseOrderLineVatService.createsPurchaseOrderLineTax(purchaseOrder, purchaseOrder.getPurchaseOrderLineList()));
		
	}
	
	/**
	 * Calculer le montant d'une commande.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param purchaseOrder
	 * @throws AxelorException 
	 */
	public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		purchaseOrder.setExTaxTotal(BigDecimal.ZERO);
		purchaseOrder.setTaxTotal(BigDecimal.ZERO);
		purchaseOrder.setInTaxTotal(BigDecimal.ZERO);
		
		for (PurchaseOrderLineTax purchaseOrderLineVat : purchaseOrder.getPurchaseOrderLineTaxList()) {
			
			// Dans la devise de la comptabilité du tiers
			purchaseOrder.setExTaxTotal(purchaseOrder.getExTaxTotal().add( purchaseOrderLineVat.getExTaxBase() ));
			purchaseOrder.setTaxTotal(purchaseOrder.getTaxTotal().add( purchaseOrderLineVat.getTaxTotal() ));
			purchaseOrder.setInTaxTotal(purchaseOrder.getInTaxTotal().add( purchaseOrderLineVat.getInTaxTotal() ));
			
		}
		
		purchaseOrder.setAmountRemainingToBeInvoiced(purchaseOrder.getInTaxTotal());
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { purchaseOrder.getExTaxTotal(), purchaseOrder.getTaxTotal(), purchaseOrder.getInTaxTotal() });
		
	}

	
	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param purchaseOrder
	 * 			Une commande.
	 */
	public void initPurchaseOrderLineVats(PurchaseOrder purchaseOrder) {
		
		if (purchaseOrder.getPurchaseOrderLineTaxList() == null) { purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<PurchaseOrderLineTax>()); }
		
		else { purchaseOrder.getPurchaseOrderLineTaxList().clear(); }
		
	}
	
	
	
	public PurchaseOrder createPurchaseOrder(Project project, UserInfo buyerUserInfo, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate, PriceList priceList, Partner supplierPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });
		
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.setProject(project);
		purchaseOrder.setBuyerUserInfo(buyerUserInfo);
		purchaseOrder.setCompany(company);
		purchaseOrder.setContactPartner(contactPartner);
		purchaseOrder.setCurrency(currency);
		purchaseOrder.setDeliveryDate(deliveryDate);
		purchaseOrder.setInternalReference(internalReference);
		purchaseOrder.setExternalReference(externalReference);
		purchaseOrder.setInvoicingTypeSelect(invoicingTypeSelect);
		purchaseOrder.setLocation(location);
		purchaseOrder.setOrderDate(orderDate);
		purchaseOrder.setPriceList(priceList);
		purchaseOrder.setPurchaseOrderLineList(new ArrayList<PurchaseOrderLine>());
		
		purchaseOrder.setPurchaseOrderSeq(this.getSequence(company));
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
		purchaseOrder.setSupplierPartner(supplierPartner);
		
		return purchaseOrder;
	}
	
	
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.PURCHASE_ORDER, company);
		if (seq == null)  {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les commandes fournisseur", company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	
	
	/**
	 * Méthode permettant de créer un StockMove à partir d'un PurchaseOrder.
	 * @param purchaseOrder une commande
	 * @throws AxelorException Aucune séquence de StockMove n'a été configurée
	 */
	public void createStocksMoves(PurchaseOrder purchaseOrder) throws AxelorException {
		
		if(purchaseOrder.getPurchaseOrderLineList() != null && purchaseOrder.getCompany() != null) {

			Company company = purchaseOrder.getCompany();
			
			SupplychainConfig supplychainConfig = supplychainConfigService.getSupplychainConfig(company);
			
			Location startLocation = Location.findByPartner(purchaseOrder.getSupplierPartner());
			
			if(startLocation == null)  {
				startLocation = supplychainConfigService.getSupplierVirtualLocation(supplychainConfig);
			}
			if(startLocation == null)  {
				throw new AxelorException(String.format("%s Veuillez configurer un entrepot virtuel fournisseur pour la société %s ",
						GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			Partner supplierPartner = purchaseOrder.getSupplierPartner();

			StockMove stockMove = stockMoveService.createStockMove(supplierPartner.getDeliveryAddress(), null, company, supplierPartner, startLocation, purchaseOrder.getLocation(), purchaseOrder.getDeliveryDate());
			stockMove.setPurchaseOrder(purchaseOrder);
			stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
			
			for(PurchaseOrderLine purchaseOrderLine: purchaseOrder.getPurchaseOrderLineList()) {
				
				Product product = purchaseOrderLine.getProduct();
				// Check if the company field 'hasInSmForStorableProduct' = true and productTypeSelect = 'storable' or 'hasInSmForNonStorableProduct' = true and productTypeSelect = 'service' or productTypeSelect = 'other'
				if(product != null && ((supplychainConfig.getHasInSmForStorableProduct() && product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE)) 
						|| (supplychainConfig.getHasInSmForNonStorableProduct() && !product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE)))) {

					StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(product, purchaseOrderLine.getQty(), purchaseOrderLine.getUnit(), 
							purchaseOrderLineService.computeDiscount(purchaseOrderLine), stockMove, 2);
					if(stockMoveLine != null) {
						
						stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
						
						stockMove.getStockMoveLineList().add(stockMoveLine);
					}
				}	
			}
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
			}
		}
	}
	
	
	public Location getLocation(Company company)  {
		
		return Location.filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", company, true, ILocation.INTERNAL).fetchOne();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateSupplier(PurchaseOrder purchaseOrder)  {
		
		Partner supplierPartner = Partner.find(purchaseOrder.getSupplierPartner().getId());
		supplierPartner.setSupplierTypeSelect(IPartner.SUPPLIER_TYPE_SELECT_APPROVED);
		
		return supplierPartner.save();
	}
	
	
	public void clearPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		List<StockMove> stockMoveList = StockMove.filter("self.purchaseOrder = ?1 AND self.statusSelect = 2", purchaseOrder).fetch();
		
		for(StockMove stockMove : stockMoveList)  {
			
			stockMoveService.cancel(stockMove);
			
		}
		
		
	}
	
	
	
	
}
