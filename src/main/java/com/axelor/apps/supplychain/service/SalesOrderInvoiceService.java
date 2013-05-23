package com.axelor.apps.supplychain.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineVat;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.service.CurrencyService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderInvoiceService.class); 

	@Inject
	private SalesOrderLineService salesOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private SalesOrderLineVatService salesOrderLineVatService;
	

	private LocalDate today;
	
	@Inject
	public SalesOrderInvoiceService() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInvoice(SalesOrder salesOrder)  {
		
		Invoice invoice = this.createInvoiceHeader(salesOrder);
		
		invoice.setInvoiceLineList(this.createInvoiceLines(invoice, salesOrder.getSalesOrderLineList()));
		
		invoice.setInvoiceLineVatList(this.createInvoiceVatLines(invoice, salesOrder.getSalesOrderLineVatList()));
		
		this.assignInvoice(salesOrder, invoice);
		
		this.fillSalesOrder(salesOrder, invoice).save();
		
		return invoice;
	}
	
	
	public SalesOrder fillSalesOrder(SalesOrder salesOrder, Invoice invoice)  {
		
		salesOrder.setOrderDate(this.today);
		
		// TODO Créer une séquence pour les commandes (Porter sur la facture ?)
//		salesOrder.setOrderNumber();
		
		return salesOrder;
		
	}
	
	
	public SalesOrder assignInvoice(SalesOrder salesOrder, Invoice invoice)  {
		
		if(salesOrder.getInvoiceSet() != null)  {
			salesOrder.getInvoiceSet().add(invoice);
		}
		else  {
			salesOrder.setInvoiceSet(new HashSet<Invoice>());
			salesOrder.getInvoiceSet().add(invoice);
		}
		
		return salesOrder;
	}
	
	
	public Invoice createInvoiceHeader(SalesOrder salesOrder)  {
		
		Invoice invoice = new Invoice();
		
		invoice.setOperationTypeSelect(IInvoice.CLIENT_SALE);
		
		invoice.setInvoiceDate(this.today);
		
		PaymentCondition paymentCondition = salesOrder.getPaymentCondition();
		invoice.setPaymentCondition(paymentCondition);
		invoice.setDueDate(this.today.plusDays(paymentCondition.getPaymentTime()));
		
		invoice.setPaymentMode(salesOrder.getPaymentMode());
		invoice.setAddress(salesOrder.getMainInvoicingAddress());
		invoice.setContactPartner(salesOrder.getContactPartner());
		invoice.setCurrency(salesOrder.getCurrency());
		
		Company company = salesOrder.getCompany();
		invoice.setCompany(company);
		invoice.setJournal(company.getCustomerSalesJournal()); // TODO ajouter un controle sur le remplissage du champs
		
		Partner partner = salesOrder.getClientPartner();
		invoice.setPartner(partner);
		invoice.setPartnerAccount(this.getCustomerAccount(partner, company));
		
		return invoice;
	}
	
	
	public Account getCustomerAccount(Partner partner, Company company)  {
		
		Account partnerAccount = null;
		
		for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {
			
			if(accountingSituation.getCompany().equals(company))  {
				
				partnerAccount = accountingSituation.getCustomerAccount();
				
			}
			
		}
		
		if(partnerAccount == null)  {
			
			partnerAccount = company.getCustomerAccount();
			
		}
		
		if(partnerAccount == null)  {
			
			// TODO ajouter message d'erreur configuration manquante dans la société
			
		}
		
		return partnerAccount;
		
	}
	
	
	// TODO ajouter tri sur les séquences
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SalesOrderLine> salesOrderLineList)  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
			
			if(salesOrderLine.getSalesOrderSubLineList() != null && !salesOrderLine.getSalesOrderSubLineList().isEmpty())  {
				
				for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
					
					invoiceLineList.add(this.createInvoiceLine(invoice, salesOrderSubLine));
					
				}
			
			}
			else  {
				
				invoiceLineList.add(this.createInvoiceLine(invoice, salesOrderLine));
				
			}
		}
		
		return invoiceLineList;
		
	}
	
	
	//TODO ajouter conversion en devise de la comptabilité du tiers
	public InvoiceLine createInvoiceLine(Invoice invoice, SalesOrderLine salesOrderLine)  {
		
		InvoiceLine invoiceLine = new InvoiceLine();
		invoiceLine.setInvoice(invoice);
		invoiceLine.setExTaxTotal(salesOrderLine.getExTaxTotal());
		invoiceLine.setProduct(salesOrderLine.getProduct());
		invoiceLine.setProductName(salesOrderLine.getProductName());
		invoiceLine.setPrice(salesOrderLine.getPrice());
		invoiceLine.setDescription(salesOrderLine.getDescription());
		invoiceLine.setQty(salesOrderLine.getQty());
		invoiceLine.setPricingListUnit(salesOrderLine.getUnit());
		invoiceLine.setVatLine(salesOrderLine.getVatLine());
		invoiceLine.setTask(salesOrderLine.getTask());
		
		return invoiceLine;
		
	}
	
	//TODO ajouter conversion en devise de la comptabilité du tiers
	public InvoiceLine createInvoiceLine(Invoice invoice, SalesOrderSubLine salesOrderSubLine)  {
		
		InvoiceLine invoiceLine = new InvoiceLine();
		invoiceLine.setInvoice(invoice);
		invoiceLine.setExTaxTotal(salesOrderSubLine.getExTaxTotal());
		invoiceLine.setProduct(salesOrderSubLine.getProduct());
		invoiceLine.setProductName(salesOrderSubLine.getProductName());
		invoiceLine.setPrice(salesOrderSubLine.getPrice());
		invoiceLine.setDescription(salesOrderSubLine.getDescription());
		invoiceLine.setQty(salesOrderSubLine.getQty());
		invoiceLine.setPricingListUnit(salesOrderSubLine.getUnit());
		invoiceLine.setVatLine(salesOrderSubLine.getVatLine());
		invoiceLine.setTask(salesOrderSubLine.getSalesOrderLine().getTask());
		
		return invoiceLine;
		
	}
	
	
	public List<InvoiceLineVat> createInvoiceVatLines(Invoice invoice, List<SalesOrderLineVat> salesOrderLineVatList)  {
		
		List<InvoiceLineVat> invoiceLineVatList = new ArrayList<InvoiceLineVat>();
		
		for(SalesOrderLineVat salesOrderLineVat : salesOrderLineVatList)  {
			
			invoiceLineVatList.add(this.createInvoiceLineVat(invoice, salesOrderLineVat));
			
		}
		
		return invoiceLineVatList;
		
	}
	
	//TODO ajouter conversion en devise de la comptabilité du tiers
	public InvoiceLineVat createInvoiceLineVat(Invoice invoice, SalesOrderLineVat salesOrderLineVat)  {
		
		InvoiceLineVat invoiceLineVat = new InvoiceLineVat();
		invoiceLineVat.setInvoice(invoice);
		invoiceLineVat.setExAllTaxBase(salesOrderLineVat.getExAllTaxBase());
		invoiceLineVat.setExTaxBase(salesOrderLineVat.getExTaxBase());
		invoiceLineVat.setInTaxTotal(salesOrderLineVat.getInTaxTotal());
		invoiceLineVat.setVatTotal(salesOrderLineVat.getVatTotal());
		invoiceLineVat.setVatLine(salesOrderLineVat.getVatLine());
		
		return invoiceLineVat;
		
	}
	
}


