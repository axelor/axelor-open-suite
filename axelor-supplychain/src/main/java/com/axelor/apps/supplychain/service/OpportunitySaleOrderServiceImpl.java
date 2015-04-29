package com.axelor.apps.supplychain.service;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceImpl extends OpportunityRepository implements OpportunitySaleOrderService{
	
	@Inject
	private PartnerRepository partnerRepo;
	
	@Transactional
	public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity){
		SaleOrder saleOrder = new SaleOrder();
		saleOrder.setCompany(opportunity.getCompany());
		if(saleOrder.getCompany() == null){
			saleOrder.setCompany(Beans.get(CompanyRepository.class).all().fetchOne());
		}
		saleOrder.setCurrency(opportunity.getCurrency());
		saleOrder.setClientPartner(opportunity.getPartner());
		saleOrder.setMainInvoicingAddress(saleOrder.getClientPartner().getMainInvoicingAddress());
		saleOrder.setDeliveryAddress(saleOrder.getClientPartner().getDeliveryAddress());
		saleOrder.setPriceList(saleOrder.getClientPartner().getSalePriceList());
		saleOrder.setCreationDate(new LocalDate());
		saleOrder.setStatusSelect(1);
		saleOrder.setSalemanUser(opportunity.getUser());
		saleOrder.setTeam(opportunity.getTeam());
		saleOrder.setPaymentMode(saleOrder.getClientPartner().getPaymentMode());
		saleOrder.setPaymentCondition(saleOrder.getClientPartner().getPaymentCondition());
		saleOrder.setInvoicingTypeSelect(1);
		saleOrder.setLocation(Beans.get(LocationRepository.class).find(new Long(1)));
		saleOrder.setShowDetailsInInvoice(saleOrder.getCompany().getAccountConfig().getShowDetailsInInvoice());
		opportunity.setSaleOrder(saleOrder);
		save(opportunity);
		saleOrder.setSaleOrderSeq(Beans.get(SaleOrderServiceImpl.class).getDraftSequence(saleOrder.getId()));
		Beans.get(SaleOrderRepository.class).save(saleOrder);
		return saleOrder;
	}
	
	@Transactional
	public Partner createClientFromLead(Opportunity opportunity){
		Lead lead = opportunity.getLead();
		Partner partner = new Partner();
		partner.setPartnerTypeSelect(1);
		partner.setName(lead.getEnterpriseName());
		partner.setCustomerTypeSelect(2);
		partner.setFixedPhone(lead.getFixedPhone());
		partner.setMobilePhone(lead.getMobilePhone());
		partner.setEmailAddress(lead.getEmailAddress());
		partner.setCurrency(opportunity.getCurrency());
		Address address = new Address();
		address.setAddressL4(lead.getPrimaryAddress());
		address.setAddressL6(lead.getPrimaryPostalCode()+" "+lead.getPrimaryCity());
		address.setAddressL7Country(lead.getPrimaryCountry());
		partner.setDeliveryAddress(address);
		partner.setMainInvoicingAddress(address);
		partner.setFullName(partner.getName());
		Partner contact = new Partner();
		contact.setPartnerTypeSelect(2);
		contact.setIsContact(true);
		contact.setName(lead.getName());
		contact.setFirstName(lead.getFirstName());
		contact.setMainPartner(partner);
		contact.setFullName(contact.getName()+" "+contact.getFirstName());
		partner.addContactPartnerSetItem(contact);
		opportunity.setPartner(partner);
		save(opportunity);
		
		return partner;
	}
}
