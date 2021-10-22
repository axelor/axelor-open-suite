/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.cash.management.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.cash.management.db.repo.CashManagementForecastGeneratorRepository;
import com.axelor.apps.cash.management.db.repo.CashManagementForecastLineRecapRepository;
import com.axelor.apps.cash.management.db.repo.CashManagementForecastRecapRepository;
import com.axelor.apps.cash.management.db.repo.CashManagementForecastRepository;
import com.axelor.apps.cash.management.db.repo.ForecastGeneratorRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapLineRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRepository;
import com.axelor.apps.cash.management.service.ChartService;
import com.axelor.apps.cash.management.service.ChartServiceImpl;
import com.axelor.apps.cash.management.service.ForecastRecapService;
import com.axelor.apps.cash.management.service.ForecastRecapServiceImpl;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentService;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentServiceImpl;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;

public class CashManagementModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ForecastRecapRepository.class).to(CashManagementForecastRecapRepository.class);
    bind(InvoiceServiceProjectImpl.class).to(InvoiceServiceManagementImpl.class);
    bind(ForecastGeneratorRepository.class).to(CashManagementForecastGeneratorRepository.class);
    bind(ForecastRepository.class).to(CashManagementForecastRepository.class);
    bind(ForecastRecapLineRepository.class).to(CashManagementForecastLineRecapRepository.class);
    bind(ForecastRecapService.class).to(ForecastRecapServiceImpl.class);
    bind(InvoiceEstimatedPaymentService.class).to(InvoiceEstimatedPaymentServiceImpl.class);
    bind(ChartService.class).to(ChartServiceImpl.class);
  }
}
