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
package com.axelor.apps.base.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;


public class PartnerService extends PartnerRepository{
	
	private static final Logger LOG = LoggerFactory.getLogger(PartnerService.class);
	
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
		
		partner.setDeliveryAddress(deliveryAddress);
		partner.setMainInvoicingAddress(mainInvoicingAddress);
		
		Partner contact = new Partner();
		contact.setPartnerTypeSelect(IPartner.PARTNER_TYPE_SELECT_INDIVIDUAL);
		contact.setIsContact(true);
		contact.setName(name);
		contact.setFirstName(firstName);
		contact.setMainPartner(partner);
		contact.setFullName(this.computeFullName(partner));
		partner.addContactPartnerSetItem(contact);
		
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
	
}
