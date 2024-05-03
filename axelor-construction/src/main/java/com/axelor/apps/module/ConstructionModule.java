/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.budget.db.repo.SaleOrderBudgetRepository;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineGroupBudgetServiceImpl;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTask;
import com.axelor.apps.businesssupport.service.ProjectTaskBusinessSupportServiceImpl;
import com.axelor.apps.repo.SaleOrderConstructionRepository;
import com.axelor.apps.service.ConsructionProjectGeneratorFactoryTask;
import com.axelor.apps.service.ConstructionSaleOrderLineServiceImpl;
import com.axelor.apps.service.PriceStudyService;
import com.axelor.apps.service.PriceStudyServiceImpl;
import com.axelor.apps.service.ProjectTaskConstructionServiceImpl;

public class ConstructionModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(ProjectTaskBusinessSupportServiceImpl.class).to(ProjectTaskConstructionServiceImpl.class);
    bind(ProjectGeneratorFactoryTask.class).to(ConsructionProjectGeneratorFactoryTask.class);
    bind(SaleOrderLineGroupBudgetServiceImpl.class).to(ConstructionSaleOrderLineServiceImpl.class);
    bind(SaleOrderBudgetRepository.class).to(SaleOrderConstructionRepository.class);
    bind(PriceStudyService.class).to(PriceStudyServiceImpl.class);
  }
}
