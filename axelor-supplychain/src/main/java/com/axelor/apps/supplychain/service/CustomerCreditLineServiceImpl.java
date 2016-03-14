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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.supplychain.db.CustomerCreditLine;
import com.axelor.apps.supplychain.db.repo.CustomerCreditLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CustomerCreditLineServiceImpl implements CustomerCreditLineService{
	
	@Inject
	protected CustomerCreditLineRepository customerCreditLineRepo;

	@Inject
	protected SaleConfigService saleConfigService;
	
	@Inject
	protected PartnerRepository partnerRepo;
	
	@Override
	public Partner generateLines(Partner partner) throws AxelorException  {
		
		if(partner.getIsContact() || !partner.getIsCustomer() || !Beans.get(GeneralService.class).getGeneral().getManageCustomerCredit() || partner.getCompanySet() == null)  {  return partner;  }
		
		List<Company> companyList = new ArrayList<Company>(partner.getCompanySet());
		List<CustomerCreditLine> customerCreditLineList = new ArrayList<CustomerCreditLine>();
		
		if(partner.getCustomerCreditLineList()!= null && !partner.getCustomerCreditLineList().isEmpty()){
			customerCreditLineList = new ArrayList<CustomerCreditLine>(partner.getCustomerCreditLineList());
			for (CustomerCreditLine customerCreditLine : customerCreditLineList) {
				if(!companyList.contains(customerCreditLine.getCompany())){
					partner.removeCustomerCreditLineListItem(customerCreditLine);
				}
				else{
					companyList.remove(customerCreditLine.getCompany());
				}
			}
		}
		
		for (Company company : companyList) {
			CustomerCreditLine customerCreditLine = new CustomerCreditLine();
			customerCreditLine.setCompany(company);
			customerCreditLine.setAcceptedCredit(saleConfigService.getSaleConfig(company).getAcceptedCredit());
			partner.addCustomerCreditLineListItem(customerCreditLine);
		}

		return partner;
	}

	@Override
	public void updateLines(Partner partner) throws AxelorException{
		if(partner.getCustomerCreditLineList() == null || partner.getCustomerCreditLineList().isEmpty()){
			partner = generateLines(partner);
		}
		List<CustomerCreditLine> customerCreditLineList = partner.getCustomerCreditLineList();
		for (CustomerCreditLine customerCreditLine : customerCreditLineList) {
			customerCreditLine = this.computeUsedCredit(customerCreditLine);
		}
	}

	@Override
	public Map<String,Object> updateLinesFromOrder(Partner partner,SaleOrder saleOrder) throws AxelorException{

		Map<String,Object> map = new HashMap<String,Object>();

		if(partner.getCustomerCreditLineList() == null || partner.getCustomerCreditLineList().isEmpty()){
			partner = generateLines(partner);
		}
		List<CustomerCreditLine> customerCreditLineList = partner.getCustomerCreditLineList();
		for (CustomerCreditLine customerCreditLine : customerCreditLineList) {
			if(customerCreditLine.getCompany().equals(saleOrder.getCompany())){
				customerCreditLine = this.computeUsedCredit(customerCreditLine);
				customerCreditLine.setUsedCredit(customerCreditLine.getUsedCredit().add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced())));
				boolean test = testUsedCredit(customerCreditLine);
				map.put("bloqued", test);
				if(test){
					if(Strings.isNullOrEmpty(customerCreditLine.getCompany().getOrderBloquedMessage())){
						map.put("message", I18n.get("Client bloqued"));
					}else{
						map.put("message", customerCreditLine.getCompany().getOrderBloquedMessage());
					}
				}
			}
		}
		return map;
	}

	@Override
	public CustomerCreditLine computeUsedCredit(CustomerCreditLine customerCreditLine){
		Company company = customerCreditLine.getCompany();
		if(customerCreditLine.getPartner().getAccountingSituationList()!=null){
			List<AccountingSituation> accountingSituationList = customerCreditLine.getPartner().getAccountingSituationList();
			for (AccountingSituation accountingSituation : accountingSituationList) {
				if(accountingSituation.getCompany().equals(company)){
					List<SaleOrder> saleOrderList = Beans.get(SaleOrderRepository.class).all().filter("self.company = ?1 AND self.clientPartner = ?2 AND self.statusSelect > ?3 AND self.statusSelect < ?4", company, customerCreditLine.getPartner(),ISaleOrder.STATUS_DRAFT,ISaleOrder.STATUS_CANCELED).fetch();
					BigDecimal sum = BigDecimal.ZERO;
					for (SaleOrder saleOrder : saleOrderList) {
						sum = sum.add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced()));
					}
					customerCreditLine.setUsedCredit(accountingSituation.getBalanceCustAccount().add(sum));
				}
			}
		}
		return customerCreditLine;
	}

	@Override
	public boolean testUsedCredit(CustomerCreditLine customerCreditLine){
		if(customerCreditLine.getUsedCredit().compareTo(customerCreditLine.getAcceptedCredit())>0){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public boolean checkBlockedPartner(Partner partner, Company company) throws AxelorException{
		CustomerCreditLine customerCreditLine = customerCreditLineRepo.all().filter("self.company = ?1 AND self.partner = ?2", company, partner).fetchOne();
		if(customerCreditLine == null){
			partner = generateLines(partner);
			for (CustomerCreditLine customerCreditLineIt : partner.getCustomerCreditLineList()) {
				if(customerCreditLineIt.getCompany() == company){
					customerCreditLine = customerCreditLineIt;
				}
			}
			Beans.get(PartnerRepository.class).save(partner);
		}
		else{
			customerCreditLine = this.computeUsedCredit(customerCreditLine);
			customerCreditLineRepo.save(customerCreditLine);
		}
		
		return this.testUsedCredit(customerCreditLine);
	}

}
