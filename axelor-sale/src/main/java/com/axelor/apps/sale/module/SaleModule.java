/*
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
package com.axelor.apps.sale.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.sale.db.repo.SaleBatchRepository;
import com.axelor.apps.sale.db.repo.SaleBatchSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineSaleRepository;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.AdvancePaymentService;
import com.axelor.apps.sale.service.AdvancePaymentServiceImpl;
import com.axelor.apps.sale.service.ConfiguratorCreatorService;
import com.axelor.apps.sale.service.ConfiguratorCreatorServiceImpl;
import com.axelor.apps.sale.service.ConfiguratorFormulaService;
import com.axelor.apps.sale.service.ConfiguratorFormulaServiceImpl;
import com.axelor.apps.sale.service.ConfiguratorService;
import com.axelor.apps.sale.service.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.OpportunitySaleOrderService;
import com.axelor.apps.sale.service.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.PartnerSaleService;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderLineServiceImpl;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.app.AppSaleServiceImpl;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.config.SaleConfigServiceImpl;


public class SaleModule extends AxelorModule {

    @Override
    protected void configure() {
    	bind(PartnerService.class).to(PartnerSaleService.class);
        bind(SaleOrderService.class).to(SaleOrderServiceImpl.class);
        bind(SaleOrderLineService.class).to(SaleOrderLineServiceImpl.class);
        bind(SaleOrderRepository.class).to(SaleOrderManagementRepository.class);
        bind(OpportunitySaleOrderService.class).to(OpportunitySaleOrderServiceImpl.class);
        bind(AdvancePaymentService.class).to(AdvancePaymentServiceImpl.class);
        bind(AppSaleService.class).to(AppSaleServiceImpl.class);
        bind(SaleOrderLineRepository.class).to(SaleOrderLineSaleRepository.class);
        bind(SaleConfigService.class).to(SaleConfigServiceImpl.class);
        bind(SaleBatchRepository.class).to(SaleBatchSaleRepository.class);
        IPartner.modelPartnerFieldMap.put(SaleOrder.class.getName(), "clientPartner");
        bind(AdvancePaymentRepository.class).to(AdvancePaymentSaleRepository.class);
        bind(ConfiguratorCreatorService.class).to(ConfiguratorCreatorServiceImpl.class);
        bind(ConfiguratorService.class).to(ConfiguratorServiceImpl.class);
        bind(ConfiguratorFormulaService.class).to(ConfiguratorFormulaServiceImpl.class);
    }
}