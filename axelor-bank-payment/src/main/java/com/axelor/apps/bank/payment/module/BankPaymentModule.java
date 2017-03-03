package com.axelor.apps.bank.payment.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.bank.payment.db.repo.BankOrderManagementRepository;
import com.axelor.apps.bank.payment.db.repo.BankOrderRepository;
import com.axelor.apps.bank.payment.db.repo.BankReconciliationManagementRepository;
import com.axelor.apps.bank.payment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bank.payment.db.repo.EbicsBankAccountRepository;
import com.axelor.apps.bank.payment.db.repo.EbicsBankRepository;
import com.axelor.apps.bank.payment.db.repo.EbicsCertificateAccountRepository;
import com.axelor.apps.bank.payment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bank.payment.ebics.service.EbicsBankService;
import com.axelor.apps.bank.payment.ebics.service.EbicsBankServiceImpl;
import com.axelor.apps.bank.payment.ebics.service.EbicsPartnerService;
import com.axelor.apps.bank.payment.ebics.service.EbicsPartnerServiceImpl;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderMergeServiceImpl;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderMoveService;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderMoveServiceImpl;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderService;
import com.axelor.apps.bank.payment.service.bankorder.BankOrderServiceImpl;
import com.axelor.apps.bank.payment.service.invoice.payment.InvoicePaymentCancelServiceBankPayImpl;
import com.axelor.apps.bank.payment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;

public class BankPaymentModule extends AxelorModule {

	@Override
	protected void configure() {
		
		 bind(BankReconciliationRepository.class).to(BankReconciliationManagementRepository.class);
		 
		 bind(BankOrderRepository.class).to(BankOrderManagementRepository.class);
		 
		 bind(EbicsBankRepository.class).to(EbicsBankAccountRepository.class);
	        
		 bind(EbicsBankService.class).to(EbicsBankServiceImpl.class);
		 
		 bind(EbicsPartnerService.class).to(EbicsPartnerServiceImpl.class);
        
		 bind(EbicsCertificateRepository.class).to(EbicsCertificateAccountRepository.class);
		 
		 bind(BankOrderService.class).to(BankOrderServiceImpl.class);
		 
		 bind(BankOrderMergeService.class).to(BankOrderMergeServiceImpl.class);
	        
	     bind(BankOrderMoveService.class).to(BankOrderMoveServiceImpl.class);
	    
	     bind(InvoicePaymentCancelServiceImpl.class).to(InvoicePaymentCancelServiceBankPayImpl.class);
	     
	     bind(InvoicePaymentValidateServiceImpl.class).to(InvoicePaymentValidateServiceBankPayImpl.class);

	     
	}

}
