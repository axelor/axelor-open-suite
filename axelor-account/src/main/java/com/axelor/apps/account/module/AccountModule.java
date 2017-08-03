/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountAccountRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.JournalManagementRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineManagementRepository;
import com.axelor.apps.account.db.repo.AccountingReportManagementRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherManagementRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.db.repo.ReconcileManagementRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.*;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.app.AppAccountServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelService;
import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationServiceImpl;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentModeServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.message.service.TemplateMessageServiceImpl;


public class AccountModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(AddressServiceImpl.class).to(AddressServiceAccountImpl.class);

        bind(AccountManagementServiceImpl.class).to(AccountManagementServiceAccountImpl.class);
        
        bind(AccountManagementAccountService.class).to(AccountManagementServiceAccountImpl.class);

        bind(FiscalPositionServiceImpl.class).to(FiscalPositionServiceAccountImpl.class);

        bind(TemplateMessageService.class).to(TemplateMessageServiceImpl.class);

        bind(InvoiceRepository.class).to(InvoiceManagementRepository.class);

        bind(MoveRepository.class).to(MoveManagementRepository.class);
        
        bind(MoveLineRepository.class).to(MoveLineManagementRepository.class);
        
        bind(AccountingReportRepository.class).to(AccountingReportManagementRepository.class);
        
        bind(AccountingReportService.class).to(AccountingReportServiceImpl.class);
        
        bind(JournalRepository.class).to(JournalManagementRepository.class);

        bind(PaymentVoucherRepository.class).to(PaymentVoucherManagementRepository.class);

        bind(InvoiceService.class).to(InvoiceServiceImpl.class);

        bind(PartnerBaseRepository.class).to(PartnerAccountRepository.class);
        
        bind(AnalyticMoveLineService.class).to(AnalyticMoveLineServiceImpl.class);
        
        bind(InvoicePaymentRepository.class).to(InvoicePaymentManagementRepository.class);

        bind(InvoicePaymentValidateService.class).to(InvoicePaymentValidateServiceImpl.class);
        
        bind(InvoicePaymentCreateService.class).to(InvoicePaymentCreateServiceImpl.class);
        
        bind(InvoicePaymentCancelService.class).to(InvoicePaymentCancelServiceImpl.class);
        
        bind(InvoicePaymentToolService.class).to(InvoicePaymentToolServiceImpl.class);

        bind(AnalyticMoveLineRepository.class).to(AnalyticMoveLineMngtRepository.class);
        
        bind(ReconcileService.class).to(ReconcileServiceImpl.class);
        
        bind(ReconcileRepository.class).to(ReconcileManagementRepository.class);
        
        bind(AppAccountService.class).to(AppAccountServiceImpl.class);

        bind(AccountingSituationService.class).to(AccountingSituationServiceImpl.class);

        bind(PaymentModeService.class).to(PaymentModeServiceImpl.class);

        bind(MoveLineExportService.class).to(MoveLineExportServiceImpl.class);

        bind(AccountRepository.class).to(AccountAccountRepository.class);

        bind(WorkflowVentilationService.class).to(WorkflowVentilationServiceImpl.class);

        bind(WorkflowCancelService.class).to(WorkflowCancelServiceImpl.class);

        IPartner.modelPartnerFieldMap.put(Invoice.class.getName(), "partner");
    }
    
    
}

