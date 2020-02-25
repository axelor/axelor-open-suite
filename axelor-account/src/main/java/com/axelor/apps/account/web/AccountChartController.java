/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountChart;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountChartRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountChartService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class AccountChartController {

  public void installChart(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    AccountChart act =
        Beans.get(AccountChartRepository.class).find(accountConfig.getAccountChart().getId());
    Company company = Beans.get(CompanyRepository.class).find(accountConfig.getCompany().getId());
    accountConfig = Beans.get(AccountConfigRepository.class).find(accountConfig.getId());
    List<? extends Account> accountList =
        Beans.get(AccountRepository.class)
            .all()
            .filter("self.company.id = ?1 AND self.parentAccount != null", company.getId())
            .fetch();

    if (accountList.isEmpty()) {
      if (Beans.get(AccountChartService.class).installAccountChart(act, company, accountConfig))
        response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_1));
      else response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_2));
      response.setReload(true);

    } else response.setFlash(I18n.get(IExceptionMessage.ACCOUNT_CHART_3));
  }
  
  public void chartInvoicedTurnoverThisYearVsLastyear(ActionRequest request, ActionResponse response) {
	    List<Map<String, Object>> dataList = new ArrayList<>();
	    InvoiceRepository repo = Beans.get(InvoiceRepository.class);
	    BigDecimal salesThisYear = repo.all().filter("self.statusSelect != 1 and self.operationTypeSelect=3 and DATE_PART('year', self.invoiceDate) = DATE_PART('year',CURRENT_DATE) ")
	    		.fetchStream().map(i -> i.getExTaxTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
	    BigDecimal salesLastYear = repo.all().filter("self.statusSelect != 1 and self.operationTypeSelect=3 and DATE_PART('year', self.invoiceDate) = DATE_PART('year',CURRENT_DATE) - 1 ")
	    		.fetchStream().map(i -> i.getExTaxTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
	    BigDecimal totalThisYear = repo.all().filter("self.statusSelect != 1 and self.operationTypeSelect=4 and DATE_PART('year', self.invoiceDate) = DATE_PART('year',CURRENT_DATE) ")
	    		.fetchStream().map(i -> i.getExTaxTotal()).reduce(BigDecimal.ZERO, BigDecimal::add).negate().add(salesThisYear);
	    BigDecimal totalLastYear = repo.all().filter("self.statusSelect != 1 and self.operationTypeSelect=4 and DATE_PART('year', self.invoiceDate) = DATE_PART('year',CURRENT_DATE) - 1 ")
	    		.fetchStream().map(i -> i.getExTaxTotal()).reduce(BigDecimal.ZERO, BigDecimal::add).negate().add(salesLastYear);
	    if(!totalLastYear.equals(BigDecimal.ZERO)) {
		    Map<String, Object> dataMap = new HashMap<>();
		    dataMap.put("_year", LocalDate.now().getYear() - 1);
	        dataMap.put("_turn_over", totalLastYear);
	        dataList.add(dataMap);
	    }
	    if(!totalThisYear.equals(BigDecimal.ZERO)) {
		    Map<String, Object> dataMap = new HashMap<>();
	        dataMap.put("_year", LocalDate.now().getYear() );
	        dataMap.put("_turn_over", totalThisYear);
	        dataList.add(dataMap);
	    }
	    response.setData(dataList);
  }
  
  public void chartInvoiceTurnoverCustHistory(ActionRequest request, ActionResponse response) {
	  Context context = request.getContext();
	  LocalDate fromDate,toDate;
	  try {
		  String[] fromDateTab = context.get("fromDate").toString().split("-");
		  fromDate = LocalDate.of(Integer.parseInt(fromDateTab[0]), Integer.parseInt(fromDateTab[1]), Integer.parseInt(fromDateTab[2]));
		  String monthSelect = context.get("monthSelect").toString();
		  if(monthSelect.equals("0")) {
			  toDate = LocalDate.now();
		  }
		  else {
			  toDate = fromDate.plusMonths(Integer.parseInt(monthSelect));
		  }
	  }
	  catch(Exception e) {
		  fromDate = LocalDate.MIN;
		  toDate = LocalDate.MAX;
	  }
	  List<Map<String, Object>> dataList = new ArrayList<>();
	  List<String> productList = new ArrayList<String>();
	  InvoiceRepository invoiceRepo = Beans.get(InvoiceRepository.class);
		  List<Invoice> invoiceList = invoiceRepo.all().filter("self.statusSelect != 1 and (self.operationTypeSelect = 3 OR self.operationTypeSelect = 4 ) and (self.invoiceDate BETWEEN DATE(:fromDate) and DATE(:toDate))")
		  				.bind("fromDate", fromDate)
		  				.bind("toDate",toDate)
		  				.fetch();
		  for(Invoice invoice : invoiceList) {
			invoice = invoiceRepo.find(invoice.getId());
			for(InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
				  if(!productList.contains(invoiceLine.getProduct().getProductCategory().getName())) {
					  	productList.add(invoiceLine.getProduct().getProductCategory().getName());
						Map<String, Object> dataMap = new HashMap<>();
						LocalDate formatedDate = LocalDate.of(invoiceLine.getInvoice().getInvoiceDate().getYear(), invoiceLine.getInvoice().getInvoiceDate().getMonth(), 1);
						dataMap.put("_month_no",formatedDate);
						if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
							dataMap.put("_turn_over",invoiceLine.getExTaxTotal());
						}
						else {
							dataMap.put("_turn_over",invoiceLine.getExTaxTotal().negate());
						}
						dataMap.put("product",invoiceLine.getProduct().getProductCategory().getName());
						dataList.add(dataMap);				  
				  }
				  else {
					  	for(Map<String,Object> dataTemp : dataList) {
					  		if(dataTemp.containsValue((invoiceLine.getProduct().getProductCategory().getName()))) {
					  			if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
					  				dataTemp.put("_turn_over",invoiceLine.getExTaxTotal().add((BigDecimal) dataTemp.remove("_turn_over")));
					  			}
					  			else {
					  				dataTemp.put("_turn_over",invoiceLine.getExTaxTotal().negate().add((BigDecimal) dataTemp.remove("_turn_over")));
					  			}
					  			break;
					  		}
					  	}
				  }
			  }
			  JPA.clear();
		  }
	  response.setData(dataList);
  }
  
  public void chartInvoiceTotalRevenueByProduct(ActionRequest request, ActionResponse response) {
	  Context context = request.getContext();
	  LocalDate fromDate,toDate;
	  try {
		  String[] fromDateTab = context.get("fromDate").toString().split("-");
		  fromDate = LocalDate.of(Integer.parseInt(fromDateTab[0]), Integer.parseInt(fromDateTab[1]), Integer.parseInt(fromDateTab[2]));
		  String[] toDateTab = context.get("toDate").toString().split("-");
		  toDate = LocalDate.of(Integer.parseInt(toDateTab[0]), Integer.parseInt(toDateTab[1]), Integer.parseInt(toDateTab[2]));;
	  }
	  catch(Exception e) {
		  fromDate = LocalDate.MIN;
		  toDate = LocalDate.MAX;
	  }
	  List<Map<String, Object>> dataList = new ArrayList<>();
	  
	  List<String> productList = new ArrayList<String>();
	  InvoiceRepository invoiceRepo = Beans.get(InvoiceRepository.class);
		  List<Invoice> invoiceList = invoiceRepo.all().filter("self.statusSelect != 1 and (self.operationTypeSelect = 3 OR self.operationTypeSelect = 4) and (self.invoiceDate BETWEEN DATE(:fromDate) and DATE(:toDate))")
		  				.bind("fromDate", fromDate)
		  				.bind("toDate",toDate)
		  				.fetch();
		  for(Invoice invoice : invoiceList) {
			invoice = invoiceRepo.find(invoice.getId());
			for(InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
				  if(!productList.contains(invoiceLine.getProduct().getProductCategory().getName())) {
					  	productList.add(invoiceLine.getProduct().getProductCategory().getName());
						Map<String, Object> dataMap = new HashMap<>();
						if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
							dataMap.put("_revenue",invoiceLine.getExTaxTotal());
						}
						else {
							dataMap.put("_revenue",invoiceLine.getExTaxTotal().negate());
						}						
						dataMap.put("_product_category",invoiceLine.getProduct().getProductCategory().getName());
						dataList.add(dataMap);				  
				  }
				  else {
					  	for(Map<String,Object> dataTemp : dataList) {
					  		if(dataTemp.containsValue((invoiceLine.getProduct().getProductCategory().getName()))) {
					  			if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
					  				dataTemp.put("_revenue",invoiceLine.getExTaxTotal().add((BigDecimal) dataTemp.remove("_revenue")));
					  			}
					  			else {
					  				dataTemp.put("_revenue",invoiceLine.getExTaxTotal().negate().add((BigDecimal) dataTemp.remove("_revenue")));
					  			}
					  			break;
					  		}
					  	}
				  }
			  }
			  JPA.clear();
		  }
	  response.setData(dataList);
  }
  
  public void chartTotalRevenueByGeoRegion(ActionRequest request,ActionResponse response) {
	  List<Map<String, Object>> dataList = new ArrayList<>();
	  
	  List<String> countryList = new ArrayList<String>();
	  InvoiceRepository invoiceRepo = Beans.get(InvoiceRepository.class);
		  List<Invoice> invoiceList = invoiceRepo.all().filter("self.statusSelect != 1 and (self.operationTypeSelect = 3 OR self.operationTypeSelect = 4)")
		  				.fetch();
		  for(Invoice invoice : invoiceList) {
			invoice = invoiceRepo.find(invoice.getId());
			for(InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
				  if(!countryList.contains(invoiceLine.getInvoice().getAddress().getAddressL7Country().getName())) {
					  	countryList.add(invoiceLine.getInvoice().getAddress().getAddressL7Country().getName());
						Map<String, Object> dataMap = new HashMap<>();
						if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
							dataMap.put("_revenue",invoiceLine.getExTaxTotal());
						}
						else {
							dataMap.put("_revenue",invoiceLine.getExTaxTotal().negate());
						}						
						dataMap.put("_geo_region",invoiceLine.getInvoice().getAddress().getAddressL7Country().getName());
						dataList.add(dataMap);				  
				  }
				  else {
					  	for(Map<String,Object> dataTemp : dataList) {
					  		if(dataTemp.containsValue((invoiceLine.getInvoice().getAddress().getAddressL7Country().getName()))) {
					  			if(invoiceLine.getInvoice().getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
					  				dataTemp.put("_revenue",invoiceLine.getExTaxTotal().add((BigDecimal) dataTemp.remove("_revenue")));
					  			}
					  			else {
					  				dataTemp.put("_revenue",invoiceLine.getExTaxTotal().negate().add((BigDecimal) dataTemp.remove("_revenue")));
					  			}
					  			break;
					  		}
					  	}
				  }
			  }
			  JPA.clear();
		  }
	  response.setData(dataList);
  }
}
