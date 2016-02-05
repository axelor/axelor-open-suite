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
package com.axelor.apps.base.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class PartnerService {

	private static final Logger LOG = LoggerFactory.getLogger(PartnerService.class);
	
	@Inject
	private PartnerRepository partnerRepo;

	public Partner createPartner(String name, String firstName, String fixedPhone, String mobilePhone, EmailAddress emailAddress, Currency currency, Address deliveryAddress, Address mainInvoicingAddress){
		Partner partner = new Partner();

		partner.setName(name);
		partner.setFirstName(firstName);
		partner.setFullName(this.computeFullName(partner));
		partner.setPartnerTypeSelect(IPartner.PARTNER_TYPE_SELECT_ENTERPRISE);
		partner.setIsCustomer(true);
		partner.setFixedPhone(fixedPhone);
		partner.setMobilePhone(mobilePhone);
		partner.setEmailAddress(emailAddress);
		partner.setCurrency(currency);
		Partner contact = new Partner();
		contact.setPartnerTypeSelect(IPartner.PARTNER_TYPE_SELECT_INDIVIDUAL);
		contact.setIsContact(true);
		contact.setName(name);
		contact.setFirstName(firstName);
		contact.setMainPartner(partner);
		contact.setFullName(this.computeFullName(partner));
		partner.addContactPartnerSetItem(contact);
		
		if(deliveryAddress == mainInvoicingAddress){
			addPartnerAddress(partner, mainInvoicingAddress, true, true, true);
		}
		else {
			addPartnerAddress(partner, deliveryAddress, true, false, true);
			addPartnerAddress(partner, mainInvoicingAddress, true, true, false);
		}

		return partner;
	}

	public void setPartnerFullName(Partner partner)  {

		partner.setFullName(this.computeFullName(partner));

	}

	public String computeFullName(Partner partner)  {
		if(!Strings.isNullOrEmpty(partner.getName()) && !Strings.isNullOrEmpty(partner.getFirstName()))  {
			return partner.getName() + " " + partner.getFirstName();
		}
		else if(!Strings.isNullOrEmpty(partner.getName()))  {
			return partner.getName();
		}
		else if(!Strings.isNullOrEmpty(partner.getFirstName()))  {
			return partner.getFirstName();
		}
		else  {
			return ""+partner.getId();
		}
	}

	public Map<String,String> getSocialNetworkUrl(String name,String firstName, Integer typeSelect){

		Map<String,String> urlMap = new HashMap<String,String>();
		if(typeSelect == 2){
			name = firstName != null && name != null ? firstName+"+"+name : name == null ? firstName : name;
		}
		name = name == null ? "" : name;
		urlMap.put("google","<a class='fa fa-google-plus' href='https://www.google.com/?gws_rd=cr#q="+name+"' target='_blank' />");
		urlMap.put("facebook","<a class='fa fa-facebook' href='https://www.facebook.com/search/more/?q="+name+"&init=public"+"' target='_blank'/>");
		urlMap.put("twitter", "<a class='fa fa-twitter' href='https://twitter.com/search?q="+name+"' target='_blank' />");
		urlMap.put("linkedin","<a class='fa fa-linkedin' href='https://www.linkedin.com/company/"+name+"' target='_blank' />");
		if(typeSelect == 2){
			urlMap.put("linkedin","<a class='fa fa-linkedin' href='http://www.linkedin.com/pub/dir/"+name.replace("+","/")+"' target='_blank' />");
		}
		urlMap.put("youtube","<a class='fa fa-youtube' href='https://www.youtube.com/results?search_query="+name+"' target='_blank' />");

		return urlMap;
	}

	public List<Long> findPartnerMails(Partner partner){
		List<Long> idList = new ArrayList<Long>();

		idList.addAll(this.findMailsFromPartner(partner));

		Set<Partner> contactSet = partner.getContactPartnerSet();
		if(contactSet != null && !contactSet.isEmpty()){
			for (Partner contact : contactSet) {
				idList.addAll(this.findMailsFromPartner(contact));
			}
		}
		return idList;
	}

	public List<Long> findContactMails(Partner partner){
		List<Long> idList = new ArrayList<Long>();

		idList.addAll(this.findMailsFromPartner(partner));

		return idList;
	}

	public List<Long> findMailsFromPartner(Partner partner){
		String query = "SELECT DISTINCT(email.id) FROM Message as email WHERE email.mediaTypeSelect = 2 AND "+
				"(email.relatedTo1Select = 'com.axelor.apps.base.db.Partner' AND email.relatedTo1SelectId = "+partner.getId()+") "+
				"OR (email.relatedTo2Select = 'com.axelor.apps.base.db.Partner' AND email.relatedTo2SelectId = "+partner.getId()+")";
		if(partner.getEmailAddress() != null){
			query += "OR (email.fromEmailAddress.id = "+partner.getEmailAddress().getId()+"))";
		}
		else{
			query += ")";
		}
		
		return JPA.em().createQuery(query).getResultList();
	}
	
	private PartnerAddress createPartnerAddress(Address address, Boolean isDefault){

		PartnerAddress partnerAddress = new PartnerAddress();
		partnerAddress.setAddress(address);
		partnerAddress.setIsDefaultAddr(isDefault);

		return partnerAddress;
	}


	@Transactional
	public void resetDefaultAddress(Partner partner, String addrTypeQuery) {

		if(partner.getId() != null){
			PartnerAddressRepository partnerAddressRepo = Beans.get(PartnerAddressRepository.class);
			PartnerAddress partnerAddress =  partnerAddressRepo.all().filter("self.partner.id = ? AND self.isDefaultAddr = true"+addrTypeQuery,partner.getId()).fetchOne();
			if(partnerAddress != null){
				partnerAddress.setIsDefaultAddr(false);
				partnerAddressRepo.save(partnerAddress);
			}
		}

	}

	public Partner addPartnerAddress(Partner partner,Address address, Boolean isDefault, Boolean isInvoicing, Boolean isDelivery){

		PartnerAddress partnerAddress = createPartnerAddress(address,isDefault);
		
		if(isDefault != null && isDefault){
			String query = " AND self.isDeliveryAddr = false AND self.isInvoicingAddr = false";
			if((isInvoicing != null && isInvoicing)  && (isDelivery != null && isDelivery)){
				query = " AND self.isDeliveryAddr = true AND self.isInvoicingAddr = true";
			}
			else if(isInvoicing != null && isInvoicing){
				query = " AND self.isDeliveryAddr = false AND self.isInvoicingAddr = true";
			}
			else if(isDelivery != null && isDelivery){
				query = " AND self.isDeliveryAddr = true AND self.isInvoicingAddr = false";
			}
			resetDefaultAddress(partner,query);
		}
		
		partnerAddress.setIsInvoicingAddr(isInvoicing);
		partnerAddress.setIsDeliveryAddr(isDelivery);
		partnerAddress.setIsDefaultAddr(isDefault);
		partner.addPartnerAddressListItem(partnerAddress);
		
		return partner;
	}


	private Address getAddress(Partner partner, String querySpecific, String queryComman){

		if(partner != null){
			PartnerAddressRepository partnerAddressRepo = Beans.get(PartnerAddressRepository.class);
			List<PartnerAddress> partnerAddressList = partnerAddressRepo.all().filter(querySpecific, partner.getId()).fetch();
			if(partnerAddressList.isEmpty()){
				partnerAddressList = partnerAddressRepo.all().filter(queryComman, partner.getId()).fetch();
			}
			if(partnerAddressList.size() == 1){
				return partnerAddressList.get(0).getAddress();
			}
			for(PartnerAddress partnerAddress : partnerAddressList){
				if(partnerAddress.getIsDefaultAddr()){
					return partnerAddress.getAddress();
				}
			}
		}
		
		return null;
	}

	public Address getInvoicingAddress(Partner partner){

		return getAddress(partner, "self.partner.id = ?1 AND self.isInvoicingAddr = true AND self.isDeliveryAddr = false AND self.isDefaultAddr = true",
				"self.partner.id = ?1 AND self.isInvoicingAddr = true");
	}

	public Address getDeliveryAddress(Partner partner){

		return getAddress(partner, "self.partner.id = ?1 AND self.isDeliveryAddr = true AND self.isInvoicingAddr = false AND self.isDefaultAddr = true",
				"self.partner.id = ?1 AND self.isDeliveryAddr = true");
	}
	
	public Address getDefaultAddress(Partner partner){
		
		return getAddress(partner, "self.partner.id = ?1 AND self.isDeliveryAddr = true AND self.isInvoicingAddr = true AND self.isDefaultAddr = true",
				"self.partner.id = ?1 AND self.isDefaultAddr = true");
	}

	@Transactional
	public Partner savePartner(Partner partner){
		return partnerRepo.save(partner);
	}
	
	public BankDetails getDefaultBankDetails(Partner partner){
		
		for(BankDetails bankDetails : partner.getBankDetailsList()){
			if(bankDetails.getIsDefault()){
				return bankDetails;
			}
		}
		
		return null;
	}

}
