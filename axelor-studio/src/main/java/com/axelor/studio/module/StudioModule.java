/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.studio.module;

import com.axelor.app.AxelorModule;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.repo.ActionBuilderRepo;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.AppBuilderRepo;
import com.axelor.studio.db.repo.AppBuilderRepository;
import com.axelor.studio.db.repo.ChartBuilderRepo;
import com.axelor.studio.db.repo.ChartBuilderRepository;
import com.axelor.studio.db.repo.DashboardBuilderRepo;
import com.axelor.studio.db.repo.DashboardBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepo;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.MetaJsonFieldRepo;
import com.axelor.studio.db.repo.MetaJsonModelRepo;
import com.axelor.studio.db.repo.StudioWkfRepository;
import com.axelor.studio.db.repo.WkfRepository;

public class StudioModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(WkfRepository.class).to(StudioWkfRepository.class);
    bind(ChartBuilderRepository.class).to(ChartBuilderRepo.class);
    bind(ActionBuilderRepository.class).to(ActionBuilderRepo.class);
    bind(MenuBuilderRepository.class).to(MenuBuilderRepo.class);
    bind(DashboardBuilderRepository.class).to(DashboardBuilderRepo.class);
    bind(AppBuilderRepository.class).to(AppBuilderRepo.class);
    bind(MetaJsonFieldRepository.class).to(MetaJsonFieldRepo.class);
    bind(MetaJsonModelRepository.class).to(MetaJsonModelRepo.class);
  }
}
