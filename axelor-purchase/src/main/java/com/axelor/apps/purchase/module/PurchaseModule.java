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
package com.axelor.apps.purchase.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestManagementRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.db.repo.SupplierCatalogManagementRepository;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.service.ProductServicePurchaseImpl;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderCreateServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderDomainService;
import com.axelor.apps.purchase.service.PurchaseOrderDomainServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLinePurchaseRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxComputeService;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxComputeServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxService;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderLineWarningService;
import com.axelor.apps.purchase.service.PurchaseOrderLineWarningServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderSequenceService;
import com.axelor.apps.purchase.service.PurchaseOrderSequenceServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectService;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowService;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowServiceImpl;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.purchase.service.PurchaseProductServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestLineService;
import com.axelor.apps.purchase.service.PurchaseRequestLineServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestRestService;
import com.axelor.apps.purchase.service.PurchaseRequestRestServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestService;
import com.axelor.apps.purchase.service.PurchaseRequestServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestWorkflowService;
import com.axelor.apps.purchase.service.PurchaseRequestWorkflowServiceImpl;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.purchase.service.SupplierCatalogServiceImpl;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.purchase.service.app.AppPurchaseServiceImpl;
import com.axelor.apps.purchase.service.attributes.PurchaseOrderAttrsService;
import com.axelor.apps.purchase.service.attributes.PurchaseOrderAttrsServiceImpl;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintServiceImpl;
import com.axelor.apps.purchase.service.split.PurchaseOrderSplitService;
import com.axelor.apps.purchase.service.split.PurchaseOrderSplitServiceImpl;

public class PurchaseModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(PurchaseOrderRepository.class).to(PurchaseOrderManagementRepository.class);
    bind(PurchaseOrderService.class).to(PurchaseOrderServiceImpl.class);
    bind(AppPurchaseService.class).to(AppPurchaseServiceImpl.class);
    bind(PurchaseRequestService.class).to(PurchaseRequestServiceImpl.class);
    bind(PurchaseProductService.class).to(PurchaseProductServiceImpl.class);
    bind(PurchaseOrderPrintService.class).to(PurchaseOrderPrintServiceImpl.class);
    bind(ProductServiceImpl.class).to(ProductServicePurchaseImpl.class);
    bind(PurchaseRequestRepository.class).to(PurchaseRequestManagementRepository.class);
    bind(PurchaseOrderLineService.class).to(PurchaseOrderLineServiceImpl.class);
    bind(SupplierCatalogService.class).to(SupplierCatalogServiceImpl.class);
    bind(PurchaseOrderLineRepository.class).to(PurchaseOrderLinePurchaseRepository.class);
    bind(PurchaseOrderWorkflowService.class).to(PurchaseOrderWorkflowServiceImpl.class);
    bind(PurchaseRequestWorkflowService.class).to(PurchaseRequestWorkflowServiceImpl.class);
    bind(PurchaseOrderDomainService.class).to(PurchaseOrderDomainServiceImpl.class);
    bind(SupplierCatalogRepository.class).to(SupplierCatalogManagementRepository.class);
    bind(PurchaseOrderLineTaxService.class).to(PurchaseOrderLineTaxServiceImpl.class);
    bind(PurchaseOrderMergingService.class).to(PurchaseOrderMergingServiceImpl.class);
    bind(PurchaseOrderMergingViewService.class).to(PurchaseOrderMergingViewServiceImpl.class);
    bind(PurchaseOrderAttrsService.class).to(PurchaseOrderAttrsServiceImpl.class);
    bind(PurchaseOrderCreateService.class).to(PurchaseOrderCreateServiceImpl.class);
    bind(PurchaseOrderSequenceService.class).to(PurchaseOrderSequenceServiceImpl.class);
    bind(PurchaseOrderSplitService.class).to(PurchaseOrderSplitServiceImpl.class);
    bind(PurchaseRequestLineService.class).to(PurchaseRequestLineServiceImpl.class);
    bind(PurchaseRequestRestService.class).to(PurchaseRequestRestServiceImpl.class);
    bind(PurchaseOrderTypeSelectService.class).to(PurchaseOrderTypeSelectServiceImpl.class);
    bind(PurchaseOrderLineWarningService.class).to(PurchaseOrderLineWarningServiceImpl.class);
    bind(PurchaseOrderLineTaxComputeService.class).to(PurchaseOrderLineTaxComputeServiceImpl.class);
  }
}
