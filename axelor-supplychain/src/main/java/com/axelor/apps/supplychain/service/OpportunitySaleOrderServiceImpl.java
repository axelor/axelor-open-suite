package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceImpl extends OpportunityRepository implements OpportunitySaleOrderService{

	@Inject
	private SaleOrderServiceSupplychainImpl saleOrderServiceSupplychain;

	@Override
	@Transactional
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException{
		User buyerUser = AuthUtils.getUser();
		Company company = opportunity.getCompany();
		if(company == null){
			company = buyerUser.getActiveCompany();
		}
		Location location = saleOrderServiceSupplychain.getLocation(company);
		SaleOrder saleOrder = saleOrderServiceSupplychain.createSaleOrder(buyerUser, company, null, opportunity.getCurrency(), null, null, null, location, GeneralService.getTodayDate(), opportunity.getPartner().getSalePriceList(), opportunity.getPartner());

		saleOrder.setMainInvoicingAddress(saleOrder.getClientPartner().getMainInvoicingAddress());
		saleOrder.setDeliveryAddress(saleOrder.getClientPartner().getDeliveryAddress());

		saleOrder.setSalemanUser(opportunity.getUser());
		saleOrder.setTeam(opportunity.getTeam());
		saleOrder.setPaymentMode(saleOrder.getClientPartner().getPaymentMode());
		saleOrder.setPaymentCondition(saleOrder.getClientPartner().getPaymentCondition());

		opportunity.setSaleOrder(saleOrder);
		save(opportunity);

		return saleOrder;
	}

	@Override
	@Transactional
	public Partner createClientFromLead(Opportunity opportunity) throws AxelorException{
		Lead lead = opportunity.getLead();
		if(lead == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAD_PARTNER)),IException.CONFIGURATION_ERROR);
		}

		String name = lead.getCompanyName();
		if(Strings.isNullOrEmpty(name)){
			name = lead.getFullName();
		}

		Address address = Beans.get(AddressService.class).createAddress(null, null, lead.getPrimaryAddress(), null, lead.getPrimaryPostalCode()+" "+lead.getPrimaryCity(), lead.getPrimaryCountry());

		Partner partner = Beans.get(PartnerService.class).createPartner(name, null, lead.getFixedPhone(), lead.getMobilePhone(), lead.getEmailAddress(), opportunity.getCurrency(), address, address);

		opportunity.setPartner(partner);
		save(opportunity);

		return partner;
	}
}
