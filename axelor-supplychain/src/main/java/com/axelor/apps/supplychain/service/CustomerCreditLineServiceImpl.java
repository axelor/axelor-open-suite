package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.CustomerCreditLine;
import com.axelor.inject.Beans;

public class CustomerCreditLineServiceImpl implements CustomerCreditLineService{

	@Override
	public Partner generateLines(Partner partner){
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
			customerCreditLine.setAcceptedCredit(Beans.get(SaleConfigRepository.class).all().filter("self.company = ?", company).fetchOne().getAcceptedCredit());

			partner.addCustomerCreditLineListItem(customerCreditLine);
		}

		return partner;
	}

	@Override
	public Map<String,Object> updateLines(Partner partner){
		if(partner.getCustomerCreditLineList() == null || partner.getCustomerCreditLineList().isEmpty()){
			partner = generateLines(partner);
		}
		List<CustomerCreditLine> customerCreditLineList = partner.getCustomerCreditLineList();
		for (CustomerCreditLine customerCreditLine : customerCreditLineList) {
			customerCreditLine.setAcceptedCredit(Beans.get(SaleConfigRepository.class).all().filter("self.company = ?", customerCreditLine.getCompany()).fetchOne().getAcceptedCredit());
			customerCreditLine = this.computeUsedCredit(customerCreditLine);
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("customerCreditLineList", customerCreditLineList);
		return map;
	}

	@Override
	public Map<String,Object> updateLinesFromOrder(Partner partner,SaleOrder saleOrder){

		Map<String,Object> map = new HashMap<String,Object>();

		if(partner.getCustomerCreditLineList() == null || partner.getCustomerCreditLineList().isEmpty()){
			partner = generateLines(partner);
		}
		List<CustomerCreditLine> customerCreditLineList = partner.getCustomerCreditLineList();
		for (CustomerCreditLine customerCreditLine : customerCreditLineList) {
			if(customerCreditLine.getCompany().equals(saleOrder.getCompany())){
				customerCreditLine.setAcceptedCredit(Beans.get(SaleConfigRepository.class).all().filter("self.company = ?", customerCreditLine.getCompany()).fetchOne().getAcceptedCredit());
				customerCreditLine = this.computeUsedCredit(customerCreditLine);
				customerCreditLine.setUsedCredit(customerCreditLine.getUsedCredit().add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced())));
				boolean test = testUsedCredit(customerCreditLine);
				map.put("bloqued", test);
				if(test){
					map.put("message", customerCreditLine.getCompany().getOrderBloquedMessage());
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

}
