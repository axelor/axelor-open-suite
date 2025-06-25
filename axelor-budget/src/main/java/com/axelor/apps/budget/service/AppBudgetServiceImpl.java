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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.AppVersionService;
import com.axelor.studio.db.AppBudget;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;

@Singleton
public class AppBudgetServiceImpl extends AppBaseServiceImpl implements AppBudgetService {

  protected AppBudgetRepository appBudgetRepo;

  @Inject
  public AppBudgetServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppBudgetRepository appBudgetRepo) {
    super(appRepo, metaFiles, appVersionService, appSettingsService, metaModuleRepo, metaFileRepo);
    this.appBudgetRepo = appBudgetRepo;
  }

  @Override
  public AppBudget getAppBudget() {
    return appBudgetRepo.all().fetchOne();
  }

  @Override
  public Boolean isMissingBudgetCheckError() {
    Integer missingBudgetCheck =
        Optional.ofNullable(this.getAppBudget())
            .map(AppBudget::getMissingBudgetCheckSelect)
            .orElse(0);
    switch (missingBudgetCheck) {
      case AppBudgetRepository.APP_BUDGET_MISSING_CHECK_SELECT_OPTIONAL:
        return Boolean.FALSE;
      case AppBudgetRepository.APP_BUDGET_MISSING_CHECK_SELECT_REQUIRED:
        return Boolean.TRUE;
    }
    return null;
  }

  @Override
  public Boolean isBudgetExceedValuesError(boolean isOrder) {
    if (!isOrder) {
      return Boolean.FALSE;
    }
    Integer budgetExceedCheck =
        Optional.ofNullable(this.getAppBudget())
            .map(AppBudget::getOrderBudgetExceedSelect)
            .orElse(0);
    switch (budgetExceedCheck) {
      case AppBudgetRepository.APP_BUDGET_EXCEED_ORDERS_SELECT_OPTIONAL:
        return Boolean.FALSE;
      case AppBudgetRepository.APP_BUDGET_EXCEED_ORDERS_SELECT_REQUIRED:
        return Boolean.TRUE;
    }
    return null;
  }
}
