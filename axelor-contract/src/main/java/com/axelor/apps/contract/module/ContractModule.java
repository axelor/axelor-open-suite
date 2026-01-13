/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ConsumptionLineManagementRepository;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.contract.db.repo.ContractBatchContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractLineManagementRepository;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.AccountManagementContractService;
import com.axelor.apps.contract.service.AccountManagementContractServiceImpl;
import com.axelor.apps.contract.service.AnalyticLineModelFromContractService;
import com.axelor.apps.contract.service.AnalyticLineModelFromContractServiceImpl;
import com.axelor.apps.contract.service.AnalyticMoveLineContractServiceImpl;
import com.axelor.apps.contract.service.AnalyticMoveLineParentContractServiceImpl;
import com.axelor.apps.contract.service.ConsumptionLineService;
import com.axelor.apps.contract.service.ConsumptionLineServiceImpl;
import com.axelor.apps.contract.service.ContractFileService;
import com.axelor.apps.contract.service.ContractFileServiceImpl;
import com.axelor.apps.contract.service.ContractInvoicingService;
import com.axelor.apps.contract.service.ContractInvoicingServiceImpl;
import com.axelor.apps.contract.service.ContractLineContextToolService;
import com.axelor.apps.contract.service.ContractLineContextToolServiceImpl;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.ContractLineViewService;
import com.axelor.apps.contract.service.ContractLineViewServiceImpl;
import com.axelor.apps.contract.service.ContractPurchaseOrderGeneration;
import com.axelor.apps.contract.service.ContractPurchaseOrderGenerationImpl;
import com.axelor.apps.contract.service.ContractRevaluationService;
import com.axelor.apps.contract.service.ContractRevaluationServiceImpl;
import com.axelor.apps.contract.service.ContractSaleOrderGeneration;
import com.axelor.apps.contract.service.ContractSaleOrderGenerationImpl;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.service.ContractVersionServiceImpl;
import com.axelor.apps.contract.service.ContractYearEndBonusService;
import com.axelor.apps.contract.service.ContractYearEndBonusServiceImpl;
import com.axelor.apps.contract.service.IndexRevaluationService;
import com.axelor.apps.contract.service.IndexRevaluationServiceImpl;
import com.axelor.apps.contract.service.InvoiceLineAnalyticContractServiceImpl;
import com.axelor.apps.contract.service.InvoiceLinePricingService;
import com.axelor.apps.contract.service.InvoiceLinePricingServiceImpl;
import com.axelor.apps.contract.service.InvoicePaymentToolServiceContractImpl;
import com.axelor.apps.contract.service.PurchaseOrderInvoiceContractServiceImpl;
import com.axelor.apps.contract.service.SaleOrderInvoiceContractServiceImpl;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.contract.service.WorkflowVentilationContractServiceImpl;
import com.axelor.apps.contract.service.attributes.ContractLineAttrsService;
import com.axelor.apps.contract.service.attributes.ContractLineAttrsServiceImpl;
import com.axelor.apps.contract.service.pricing.ContractPricingService;
import com.axelor.apps.contract.service.pricing.ContractPricingServiceImpl;
import com.axelor.apps.contract.service.pricing.PricingGroupContractServiceImpl;
import com.axelor.apps.contract.service.record.ContractLineRecordSetService;
import com.axelor.apps.contract.service.record.ContractLineRecordSetServiceImpl;
import com.axelor.apps.supplychain.service.AnalyticMoveLineSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.InvoicePaymentToolServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.analytic.AnalyticMoveLineParentSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceLineAnalyticSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.pricing.PricingGroupSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowVentilationServiceSupplychainImpl;

public class ContractModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AbstractContractRepository.class).to(ContractRepository.class);
    bind(ContractService.class).to(ContractServiceImpl.class);
    bind(ContractVersionService.class).to(ContractVersionServiceImpl.class);
    bind(ContractLineService.class).to(ContractLineServiceImpl.class);
    bind(ConsumptionLineService.class).to(ConsumptionLineServiceImpl.class);
    bind(ContractBatchRepository.class).to(ContractBatchContractRepository.class);
    bind(AnalyticMoveLineSupplychainServiceImpl.class)
        .to(AnalyticMoveLineContractServiceImpl.class);
    bind(InvoiceLineAnalyticSupplychainServiceImpl.class)
        .to(InvoiceLineAnalyticContractServiceImpl.class);
    bind(WorkflowCancelServiceSupplychainImpl.class).to(WorkflowCancelServiceContractImpl.class);
    bind(ContractLineRepository.class).to(ContractLineManagementRepository.class);
    bind(IndexRevaluationService.class).to(IndexRevaluationServiceImpl.class);
    bind(ContractRevaluationService.class).to(ContractRevaluationServiceImpl.class);
    bind(ContractLineViewService.class).to(ContractLineViewServiceImpl.class);
    bind(ConsumptionLineRepository.class).to(ConsumptionLineManagementRepository.class);
    bind(ContractLineAttrsService.class).to(ContractLineAttrsServiceImpl.class);
    bind(ContractLineRecordSetService.class).to(ContractLineRecordSetServiceImpl.class);
    bind(InvoiceLinePricingService.class).to(InvoiceLinePricingServiceImpl.class);
    bind(ContractPricingService.class).to(ContractPricingServiceImpl.class);
    bind(PricingGroupSupplyChainServiceImpl.class).to(PricingGroupContractServiceImpl.class);
    bind(ContractYearEndBonusService.class).to(ContractYearEndBonusServiceImpl.class);
    bind(ContractSaleOrderGeneration.class).to(ContractSaleOrderGenerationImpl.class);
    bind(ContractPurchaseOrderGeneration.class).to(ContractPurchaseOrderGenerationImpl.class);
    bind(AnalyticLineModelFromContractService.class)
        .to(AnalyticLineModelFromContractServiceImpl.class);
    bind(ContractFileService.class).to(ContractFileServiceImpl.class);
    bind(AccountManagementServiceAccountImpl.class).to(AccountManagementContractServiceImpl.class);
    bind(AccountManagementContractService.class).to(AccountManagementContractServiceImpl.class);
    bind(ContractLineContextToolService.class).to(ContractLineContextToolServiceImpl.class);
    bind(ContractInvoicingService.class).to(ContractInvoicingServiceImpl.class);
    bind(WorkflowVentilationServiceSupplychainImpl.class)
        .to(WorkflowVentilationContractServiceImpl.class);
    bind(InvoicePaymentToolServiceSupplychainImpl.class)
        .to(InvoicePaymentToolServiceContractImpl.class);
    bind(SaleOrderInvoiceServiceImpl.class).to(SaleOrderInvoiceContractServiceImpl.class);
    bind(PurchaseOrderInvoiceServiceImpl.class).to(PurchaseOrderInvoiceContractServiceImpl.class);
    bind(AnalyticMoveLineParentSupplychainServiceImpl.class)
        .to(AnalyticMoveLineParentContractServiceImpl.class);
  }
}
