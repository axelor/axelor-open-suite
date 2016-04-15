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
package com.axelor.apps.account.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineMngtRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.BankStatementManagementRepository;
import com.axelor.apps.account.db.repo.BankStatementRepository;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.JournalManagementRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineReportManagementRepository;
import com.axelor.apps.account.db.repo.MoveLineReportRepository;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherManagementRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AddressServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.account.service.AnalyticDistributionLineServiceImpl;
import com.axelor.apps.account.service.FiscalPositionServiceAccountImpl;
import com.axelor.apps.account.service.InvoicePaymentService;
import com.axelor.apps.account.service.InvoicePaymentServiceImpl;
import com.axelor.apps.account.service.MoveLineReportService;
import com.axelor.apps.account.service.MoveLineReportServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
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

        bind(FiscalPositionServiceImpl.class).to(FiscalPositionServiceAccountImpl.class);

        bind(TemplateMessageService.class).to(TemplateMessageServiceImpl.class);

        bind(InvoiceRepository.class).to(InvoiceManagementRepository.class);

        bind(MoveRepository.class).to(MoveManagementRepository.class);
        
        bind(MoveLineReportRepository.class).to(MoveLineReportManagementRepository.class);
        
        bind(MoveLineReportService.class).to(MoveLineReportServiceImpl.class);
        
        bind(JournalRepository.class).to(JournalManagementRepository.class);

        bind(BankStatementRepository.class).to(BankStatementManagementRepository.class);

        bind(PaymentVoucherRepository.class).to(PaymentVoucherManagementRepository.class);

        bind(InvoiceService.class).to(InvoiceServiceImpl.class);

        bind(PartnerBaseRepository.class).to(PartnerAccountRepository.class);
        
        bind(AnalyticDistributionLineService.class).to(AnalyticDistributionLineServiceImpl.class);
        
        bind(InvoicePaymentRepository.class).to(InvoicePaymentManagementRepository.class);

        bind(InvoicePaymentService.class).to(InvoicePaymentServiceImpl.class);
        
        bind(AnalyticDistributionLineRepository.class).to(AnalyticDistributionLineMngtRepository.class);
        
        IPartner.modelPartnerFieldMap.put(Invoice.class.getName(), "partner");
    }
}