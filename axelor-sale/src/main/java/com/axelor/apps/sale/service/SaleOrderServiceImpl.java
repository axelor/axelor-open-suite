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
package com.axelor.apps.sale.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.persistence.Query;

import org.eclipse.birt.core.exception.BirtException;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceImpl implements SaleOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected SaleOrderLineService saleOrderLineService;
	protected SaleOrderLineTaxService saleOrderLineTaxService;
	protected SequenceService sequenceService;
	protected PartnerService partnerService;
	protected PartnerRepository partnerRepo;
	protected SaleOrderRepository saleOrderRepo;
	protected GeneralService generalService;
	protected User currentUser;
	
	protected LocalDate today;
	
	@Inject
	public SaleOrderServiceImpl(SaleOrderLineService saleOrderLineService, SaleOrderLineTaxService saleOrderLineTaxService, SequenceService sequenceService,
			PartnerService partnerService, PartnerRepository partnerRepo, SaleOrderRepository saleOrderRepo, GeneralService generalService, UserService userService)  {
		
		this.saleOrderLineService = saleOrderLineService;
		this.saleOrderLineTaxService = saleOrderLineTaxService;
		this.sequenceService = sequenceService;
		this.partnerService = partnerService;
		this.partnerRepo = partnerRepo;
		this.saleOrderRepo = saleOrderRepo;
		this.generalService = generalService;

		this.today = generalService.getTodayDate();
		this.currentUser = userService.getUser();
	}
	

	@Override
	public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null)  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				saleOrderLine.setCompanyExTaxTotal(saleOrderLineService.getAmountInCompanyCurrency(saleOrderLine.getExTaxTotal(), saleOrder));
			}
		}

		return saleOrder;
	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException  {

		this.initSaleOrderLineTaxList(saleOrder);

		this._computeSaleOrderLineList(saleOrder);

		this._populateSaleOrder(saleOrder);

		this._computeSaleOrder(saleOrder);

		return saleOrder;
	}


	/**
	 * Peupler un devis.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'un devis.
	 * </p>
	 *
	 * @param saleOrder
	 *
	 * @throws AxelorException
	 */
	@Override
	public void _populateSaleOrder(SaleOrder saleOrder) throws AxelorException {

		logger.debug("Peupler un devis => lignes de devis: {} ", new Object[] { saleOrder.getSaleOrderLineList().size() });

		// create Tva lines
		saleOrder.getSaleOrderLineTaxList().addAll(saleOrderLineTaxService.createsSaleOrderLineTax(saleOrder, saleOrder.getSaleOrderLineList()));

	}

	/**
	 * Calculer le montant d'une facture.
	 * <p>
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 *
	 * @param invoice
	 * @param vatLines
	 * @throws AxelorException
	 */
	@Override
	public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

		saleOrder.setExTaxTotal(BigDecimal.ZERO);
		saleOrder.setTaxTotal(BigDecimal.ZERO);
		saleOrder.setInTaxTotal(BigDecimal.ZERO);

		for (SaleOrderLineTax saleOrderLineVat : saleOrder.getSaleOrderLineTaxList()) {

			// Dans la devise de la comptabilité du tiers
			saleOrder.setExTaxTotal(saleOrder.getExTaxTotal().add( saleOrderLineVat.getExTaxBase() ));
			saleOrder.setTaxTotal(saleOrder.getTaxTotal().add( saleOrderLineVat.getTaxTotal() ));
			saleOrder.setInTaxTotal(saleOrder.getInTaxTotal().add( saleOrderLineVat.getInTaxTotal() ));

		}

		for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
			//Into company currency
			saleOrder.setCompanyExTaxTotal(saleOrder.getCompanyExTaxTotal().add( saleOrderLine.getCompanyExTaxTotal() ));
		}

		logger.debug("Montant de la facture: HTT = {},  HT = {}, Taxe = {}, TTC = {}",
				new Object[] { saleOrder.getExTaxTotal(), saleOrder.getTaxTotal(), saleOrder.getInTaxTotal() });

	}


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param saleOrder
	 * 			Un devis
	 */
	@Override
	public void initSaleOrderLineTaxList(SaleOrder saleOrder) {

		if (saleOrder.getSaleOrderLineTaxList() == null) { saleOrder.setSaleOrderLineTaxList(new ArrayList<SaleOrderLineTax>()); }

		else { saleOrder.getSaleOrderLineTaxList().clear(); }

	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateCustomer(SaleOrder saleOrder)  {

		Partner clientPartner = partnerRepo.find(saleOrder.getClientPartner().getId());
		clientPartner.setIsCustomer(true);
		clientPartner.setHasOrdered(true);

		return partnerRepo.save(clientPartner);
	}



	@Override
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.SALES_ORDER, company);
		if (seq == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SALES_ORDER_1),company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}

	@Override
	public SaleOrder createSaleOrder(Company company) throws AxelorException{
		SaleOrder saleOrder = new SaleOrder();
		saleOrder.setCreationDate(generalService.getTodayDate());
		if(company != null){
			saleOrder.setCompany(company);
			saleOrder.setSaleOrderSeq(this.getSequence(company));
			saleOrder.setCurrency(company.getCurrency());
		}
		saleOrder.setSalemanUser(AuthUtils.getUser());
		saleOrder.setTeam(saleOrder.getSalemanUser().getActiveTeam());
		saleOrder.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		this.computeEndOfValidityDate(saleOrder);
		return saleOrder;
	}

	@Override
	public SaleOrder createSaleOrder(User salemanUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, LocalDate orderDate,
			PriceList priceList, Partner clientPartner, Team team) throws AxelorException  {

		logger.debug("Création d'un devis client : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company, externalReference, clientPartner.getFullName() });

		SaleOrder saleOrder = new SaleOrder();
		saleOrder.setClientPartner(clientPartner);
		saleOrder.setCreationDate(generalService.getTodayDate());
		saleOrder.setContactPartner(contactPartner);
		saleOrder.setCurrency(currency);
		saleOrder.setExternalReference(externalReference);
		saleOrder.setOrderDate(orderDate);

		if(salemanUser == null)  {
			salemanUser = AuthUtils.getUser();
		}
		saleOrder.setSalemanUser(salemanUser);

		if(team == null)  {
			team = salemanUser.getActiveTeam();
		}

		if(company == null)  {
			company = salemanUser.getActiveCompany();
		}

		saleOrder.setCompany(company);
		saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(clientPartner));
		saleOrder.setDeliveryAddress(partnerService.getDeliveryAddress(clientPartner));
		
		if(priceList == null)  {
			priceList = clientPartner.getSalePriceList();
		}

		saleOrder.setPriceList(priceList);

		saleOrder.setSaleOrderLineList(new ArrayList<SaleOrderLine>());

		saleOrder.setSaleOrderSeq(this.getSequence(company));
		saleOrder.setStatusSelect(ISaleOrder.STATUS_DRAFT);

		this.computeEndOfValidityDate(saleOrder);

		return saleOrder;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelSaleOrder(SaleOrder saleOrder){
		Query q = JPA.em().createQuery("select count(*) FROM SaleOrder as self WHERE self.statusSelect = ?1 AND self.clientPartner = ?2 ");
		q.setParameter(1, ISaleOrder.STATUS_ORDER_CONFIRMED);
		q.setParameter(2, saleOrder.getClientPartner());
		if((long) q.getSingleResult() == 1)  {
			saleOrder.getClientPartner().setHasOrdered(false);
		}
		saleOrder.setStatusSelect(ISaleOrder.STATUS_CANCELED);
		saleOrderRepo.save(saleOrder);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finalizeSaleOrder(SaleOrder saleOrder) throws AxelorException, IOException, BirtException {
		saleOrder.setStatusSelect(ISaleOrder.STATUS_FINALIZE);
		if (saleOrder.getVersionNumber() == 1){
			saleOrder.setSaleOrderSeq(this.getSequence(saleOrder.getCompany()));
		}
		saleOrderRepo.save(saleOrder);
		if (generalService.getGeneral().getManageSaleOrderVersion()){
			this.saveSaleOrderPDFAsAttachment(saleOrder);
		}
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirmSaleOrder(SaleOrder saleOrder) throws Exception  {
		saleOrder.setStatusSelect(ISaleOrder.STATUS_ORDER_CONFIRMED);
		saleOrder.setConfirmationDate(this.today);
		saleOrder.setConfirmedByUser(this.currentUser);
		
		this.validateCustomer(saleOrder);
		
		saleOrderRepo.save(saleOrder);
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finishSaleOrder(SaleOrder saleOrder) throws AxelorException {
		saleOrder.setStatusSelect(ISaleOrder.STATUS_FINISHED);

		saleOrderRepo.save(saleOrder);
	}


	@Override
	public void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws AxelorException  {
		
		String language = this.getLanguageForPrinting(saleOrder);
		
		ReportFactory.createReport(IReport.SALES_ORDER, this.getFileName(saleOrder)+"-${date}")
				.addParam("Locale", language)
				.addParam("SaleOrderId", saleOrder.getId())
				.addModel(saleOrder)
				.generate()
				.getFileLink();
		
//		String relatedModel = generalService.getPersistentClass(saleOrder).getCanonicalName(); required ?
		
	}

	@Override
	public String getLanguageForPrinting(SaleOrder saleOrder)  {
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		return language;
	}
	
	@Override
	public String getFileName(SaleOrder saleOrder)  {
		
		return I18n.get("Sale order") + " " + saleOrder.getSaleOrderSeq() + ((saleOrder.getVersionNumber() > 1) ? "-V" + saleOrder.getVersionNumber() : "");
	}
	
	@Override
	@Transactional
	public SaleOrder createSaleOrder(SaleOrder context){
		SaleOrder copy = saleOrderRepo.copy(context, true);
		copy.setTemplate(false);
		copy.setTemplateUser(null);
		return copy;
	}

	@Override
	@Transactional
	public SaleOrder createTemplate(SaleOrder context){
		SaleOrder copy = saleOrderRepo.copy(context, true);
		copy.setTemplate(true);
		copy.setTemplateUser(AuthUtils.getUser());
		return copy;
	}


	@Override
	public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder)  {

		saleOrder.setEndOfValidityDate(
				Beans.get(DurationService.class).computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));

		return saleOrder;

	}
	
	@Override
	public String getReportLink(SaleOrder saleOrder, String name, String language, String format) throws AxelorException{

		return ReportFactory.createReport(IReport.SALES_ORDER, name+"-${date}")
		.addParam("Locale", language)
		.addParam("SaleOrderId", saleOrder.getId())
		.addFormat(format)
		.generate()
		.getFileLink();
	}
}



