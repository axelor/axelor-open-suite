/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.MetaFilesTemp;
import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceImpl extends SaleOrderRepository  implements SaleOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderServiceImpl.class);

	@Inject
	private SaleOrderLineService saleOrderLineService;

	@Inject
	private SaleOrderLineTaxService saleOrderLineTaxService;

	@Inject
	private SequenceService sequenceService;

	@Inject
	private PartnerService partnerService;

	@Inject
	protected SaleOrderRepository saleOrderRepo;


	@Override
	public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null)  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				saleOrderLine.setExTaxTotal(saleOrderLineService.computeSaleOrderLine(saleOrderLine));
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

		LOG.debug("Peupler un devis => lignes de devis: {} ", new Object[] { saleOrder.getSaleOrderLineList().size() });

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

		LOG.debug("Montant de la facture: HTT = {},  HT = {}, Taxe = {}, TTC = {}",
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

		Partner clientPartner = partnerService.find(saleOrder.getClientPartner().getId());
		clientPartner.setCustomerTypeSelect(IPartner.CUSTOMER_TYPE_SELECT_YES);

		return partnerService.save(clientPartner);
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
	public String getDraftSequence(Long saleOrderId){
		return "*"+saleOrderId.toString();
	}


	@Override
	public SaleOrder createSaleOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, LocalDate orderDate,
			PriceList priceList, Partner clientPartner) throws AxelorException  {

		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company.getName(), externalReference, clientPartner.getFullName() });

		SaleOrder saleOrder = new SaleOrder();
		saleOrder.setCompany(company);
		saleOrder.setContactPartner(contactPartner);
		saleOrder.setCurrency(currency);
		saleOrder.setExternalReference(externalReference);
		saleOrder.setOrderDate(orderDate);
		saleOrder.setPriceList(priceList);
		saleOrder.setSaleOrderLineList(new ArrayList<SaleOrderLine>());

		saleOrder.setSaleOrderSeq(this.getSequence(company));
		saleOrder.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		saleOrder.setClientPartner(clientPartner);

		return saleOrder;
	}

	@Override
	@Transactional
	public void cancelSaleOrder(SaleOrder saleOrder){
		saleOrder.setStatusSelect(4);
		this.save(saleOrder);
	}

	@Override
	@Transactional(rollbackOn = {Exception.class})
	public void finalizeSaleOrder(SaleOrder saleOrder) throws Exception{
		saleOrder.setStatusSelect(ISaleOrder.STATUS_FINALIZE);
		if (saleOrder.getVersionNumber() == 1){
			saleOrder.setSaleOrderSeq(this.getSequence(saleOrder.getCompany()));
		}
		this.save(saleOrder);
		if (GeneralService.getGeneral().getManageSaleOrderVersion()){
			this.saveSaleOrderPDFAsAttachment(saleOrder);
		}
	}

	@Override
	@Transactional
	public void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws IOException{
		String
	    		filePath = AppSettings.get().get("file.upload.dir"),
	    		fileName = saleOrder.getSaleOrderSeq() + ((saleOrder.getVersionNumber() > 1) ? "-V" + saleOrder.getVersionNumber() : "") + "." + ReportSettings.FORMAT_PDF,
	    		birtReportURL = this.getURLSaleOrderPDF(saleOrder);

	    File file = URLService.fileDownload(birtReportURL, filePath, fileName);

		if (file != null){
			MetaFilesTemp metaFilesTemp = Beans.get(MetaFilesTemp.class);
			MetaFile metaFile = metaFilesTemp.upload(file, new MetaFile());
			MetaAttachment metaAttachment = metaFilesTemp.attach(metaFile, saleOrder);
			JPA.save(metaAttachment);
		}
	}

	@Override
	public String getURLSaleOrderPDF(SaleOrder saleOrder){
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;


		return new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_PDF)
							.addParam("Locale", language)
							.addParam("__locale", "fr_FR")
							.addParam("SaleOrderId", saleOrder.getId().toString())
							.getUrl();
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

}



