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
package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.DashboardBuilder;
import com.axelor.studio.service.builder.DashboardBuilderService;
import com.google.inject.Inject;

public class DashboardBuilderRepo extends DashboardBuilderRepository {

  @Inject private DashboardBuilderService dashboardBuilderService;

  @Inject private MetaViewRepository metaViewRepo;

  @Override
  public DashboardBuilder save(DashboardBuilder dashboardBuilder) {

    dashboardBuilder = super.save(dashboardBuilder);

    MetaView metaView = dashboardBuilderService.build(dashboardBuilder);
    if (metaView != null) {
      dashboardBuilder.setMetaViewGenerated(metaView);
    } else {
      metaView = dashboardBuilder.getMetaViewGenerated();
      if (metaView != null) {
        metaViewRepo.remove(metaView);
      }
    }
    return dashboardBuilder;
  }

  @Override
  public void remove(DashboardBuilder dashboardBuilder) {

    MetaView metaView = dashboardBuilder.getMetaViewGenerated();
    if (metaView != null) {
      metaViewRepo.remove(metaView);
    }

    super.remove(dashboardBuilder);
  }
}
