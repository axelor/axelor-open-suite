/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.service.builder.ViewBuilderService;

public class ViewBuilderRepo extends ViewBuilderRepository {

  @Override
  public ViewBuilder save(ViewBuilder viewBuilder) {

    viewBuilder = super.save(viewBuilder);

    try {
      Beans.get(ViewBuilderService.class).genereateMetaView(viewBuilder, null);
    } catch (AxelorException e) {
      e.printStackTrace();
    }

    return viewBuilder;
  }

  @Override
  public void remove(ViewBuilder viewBuilder) {

    MetaStore.clear();

    Beans.get(MetaViewRepository.class).remove(viewBuilder.getMetaViewGenerated());

    super.remove(viewBuilder);
  }
}
