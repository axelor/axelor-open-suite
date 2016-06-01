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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.report.IReport;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private PurchaseOrderLineTaxService purchaseOrderLineVatService;

	@Inject
	private SequenceService sequenceService;

	@Inject
	private PartnerRepository partnerRepo;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected PurchaseOrderRepository purchaseOrderRepo;

	@Override
	public PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder) throws AxelorException  {

		if(purchaseOrder.getPurchaseOrderLineList() != null)  {
			PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
				purchaseOrderLine.setExTaxTotal(purchaseOrderLineService.computePurchaseOrderLine(purchaseOrderLine));
				purchaseOrderLine.setCompanyExTaxTotal(purchaseOrderLineService.getCompanyExTaxTotal(purchaseOrderLine.getExTaxTotal(), purchaseOrder));
			}
		}

		return purchaseOrder;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PurchaseOrder computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {

		this.initPurchaseOrderLineTax(purchaseOrder);

		this._computePurchaseOrderLines(purchaseOrder);

		this._populatePurchaseOrder(purchaseOrder);

		this._computePurchaseOrder(purchaseOrder);

		return purchaseOrder;
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
	@Override
	public void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

		logger.debug("Peupler une facture => lignes de devis: {} ", new Object[] { purchaseOrder.getPurchaseOrderLineList().size() });

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
	@Override
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

		for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
			//Into company currency
			purchaseOrder.setCompanyExTaxTotal(purchaseOrder.getCompanyExTaxTotal().add( purchaseOrderLine.getCompanyExTaxTotal() ));
		}

		logger.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { purchaseOrder.getExTaxTotal(), purchaseOrder.getTaxTotal(), purchaseOrder.getInTaxTotal() });

	}


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param purchaseOrder
	 * 			Une commande.
	 */
	@Override
	public void initPurchaseOrderLineTax(PurchaseOrder purchaseOrder) {

		if (purchaseOrder.getPurchaseOrderLineTaxList() == null) { purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<PurchaseOrderLineTax>()); }

		else { purchaseOrder.getPurchaseOrderLineTaxList().clear(); }

	}


	@Override
	public PurchaseOrder createPurchaseOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, LocalDate orderDate,
			PriceList priceList, Partner supplierPartner) throws AxelorException  {

		logger.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });

		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.setBuyerUser(buyerUser);
		purchaseOrder.setCompany(company);
		purchaseOrder.setContactPartner(contactPartner);
		purchaseOrder.setCurrency(currency);
		purchaseOrder.setDeliveryDate(deliveryDate);
		purchaseOrder.setInternalReference(internalReference);
		purchaseOrder.setExternalReference(externalReference);
		purchaseOrder.setOrderDate(orderDate);
		purchaseOrder.setPriceList(priceList);
		purchaseOrder.setPurchaseOrderLineList(new ArrayList<PurchaseOrderLine>());

		purchaseOrder.setPurchaseOrderSeq(this.getSequence(company));
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
		purchaseOrder.setSupplierPartner(supplierPartner);

		return purchaseOrder;
	}

	@Override
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.PURCHASE_ORDER, company);
		if (seq == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PURCHASE_ORDER_1), company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}

	@Override
	public String getDraftSequence(Long purchaseOrderId){
		return "*"+purchaseOrderId.toString();
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateSupplier(PurchaseOrder purchaseOrder)  {

		Partner supplierPartner = partnerRepo.find(purchaseOrder.getSupplierPartner().getId());
		supplierPartner.setIsSupplier(true);
		supplierPartner.setHasOrdered(true);

		return partnerRepo.save(supplierPartner);
	}

//	@Override
//	@Transactional(rollbackOn = {Exception.class})
//	public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws Exception{
//		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_REQUESTED);
//		if (purchaseOrder.getVersionNumber() == 1){
//			purchaseOrder.setPurchaseOrderSeq(this.getSequence(purchaseOrder.getCompany()));
//		}
//		this.save(purchaseOrder);
//		if (GeneralService.getGeneral().getManagePurchaseOrderVersion()){
//			this.savePurchaseOrderPDFAsAttachment(purchaseOrder);
//		}
//	}

	@Override
	public void savePurchaseOrderPDFAsAttachment(PurchaseOrder purchaseOrder) throws AxelorException  {

		String language="";
		try{
			language = purchaseOrder.getSupplierPartner().getLanguageSelect() != null? purchaseOrder.getSupplierPartner().getLanguageSelect() : purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		String title = I18n.get("Purchase order") + purchaseOrder.getPurchaseOrderSeq() + ((purchaseOrder.getVersionNumber() > 1) ? "-V" + purchaseOrder.getVersionNumber() : "");

		ReportFactory.createReport(IReport.PURCHASE_ORDER, title+"-${date}")
				.addParam("PurchaseOrderId", purchaseOrder.getId().toString())
				.addParam("Locale", language)
				.addModel(purchaseOrder)
				.generate()
				.getFileLink();

	}


	@Override
	@Transactional(rollbackOn = {Exception.class})
	public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws Exception{
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_REQUESTED);
		if (purchaseOrder.getVersionNumber() == 1){
			purchaseOrder.setPurchaseOrderSeq(this.getSequence(purchaseOrder.getCompany()));
		}
		purchaseOrderRepo.save(purchaseOrder);
		if (generalService.getGeneral().getManagePurchaseOrderVersion()){
			this.savePurchaseOrderPDFAsAttachment(purchaseOrder);
		}
	}

	@Override
	@Transactional
	public PurchaseOrder mergePurchaseOrders(List<PurchaseOrder> purchaseOrderList, Currency currency, Partner supplierPartner, Company company, Partner contactPartner, PriceList priceList) throws AxelorException{

		String numSeq = "";
		String externalRef = "";
		for (PurchaseOrder purchaseOrderLocal : purchaseOrderList) {
			if (!numSeq.isEmpty()){
				numSeq += "-";
			}
			numSeq += purchaseOrderLocal.getPurchaseOrderSeq();

			if (!externalRef.isEmpty()){
				externalRef += "|";
			}
			if (purchaseOrderLocal.getExternalReference() != null){
				externalRef += purchaseOrderLocal.getExternalReference();
			}
		}

		PurchaseOrder purchaseOrderMerged = this.createPurchaseOrder(
				AuthUtils.getUser(),
				company,
				contactPartner,
				currency,
				null,
				numSeq,
				externalRef,
				LocalDate.now(),
				priceList,
				supplierPartner);

		//Attachment of all purchase order lines to new purchase order
		for(PurchaseOrder purchaseOrder : purchaseOrderList)  {
			int countLine = 1;
			for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
				purchaseOrderLine.setSequence(countLine * 10);
				purchaseOrderMerged.addPurchaseOrderLineListItem(purchaseOrderLine);
				countLine++;
			}
		}

		this.computePurchaseOrder(purchaseOrderMerged);

		purchaseOrderRepo.save(purchaseOrderMerged);

		//Remove old purchase orders
		for(PurchaseOrder purchaseOrder : purchaseOrderList)  {
			purchaseOrderRepo.remove(purchaseOrder);
		}

		return purchaseOrderMerged;
	}

	@Override
	public void setDraftSequence(PurchaseOrder purchaseOrder) {
		
		if(purchaseOrder.getId() != null && Strings.isNullOrEmpty(purchaseOrder.getPurchaseOrderSeq())){
			purchaseOrder.setPurchaseOrderSeq(this.getDraftSequence(purchaseOrder.getId()));
		}
	}
	
	@Override
	@Transactional
	public void updateCostPrice(PurchaseOrder purchaseOrder) {
		if(purchaseOrder.getPurchaseOrderLineList() != null){
			for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
				Product product = purchaseOrderLine.getProduct();
				if(product.getCostTypeSelect() == ProductRepository.COST_TYPE_LAST_PURCHASE_PRICE){
					product.setPurchasePrice(purchaseOrderLine.getPrice());
					product.setCostPrice(purchaseOrderLine.getPrice());
				}
				
			}
			purchaseOrderRepo.save(purchaseOrder);
		}
	}
	
	@Override
	@Transactional
	public void draftPurchaseOrder(PurchaseOrder purchaseOrder){
		
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
		purchaseOrderRepo.save(purchaseOrder);
	}
	
	@Override
	@Transactional
	public void validatePurchaseOrder(PurchaseOrder purchaseOrder){
		
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_VALIDATED);
		purchaseOrderRepo.save(purchaseOrder);
	}
	
	@Override
	@Transactional
	public void finishPurchaseOrder(PurchaseOrder purchaseOrder){
		
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_FINISHED);
		purchaseOrderRepo.save(purchaseOrder);
	}
	
	@Override
	@Transactional
	public void cancelPurchaseOrder(PurchaseOrder purchaseOrder){
		
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_CANCELED);
		purchaseOrderRepo.save(purchaseOrder);
	}
}
