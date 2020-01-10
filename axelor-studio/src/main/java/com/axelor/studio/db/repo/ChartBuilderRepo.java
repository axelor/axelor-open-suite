/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ChartBuilder;
import com.axelor.studio.service.builder.ChartBuilderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBException;

public class ChartBuilderRepo extends ChartBuilderRepository {

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private ChartBuilderService chartBuilderService;

  @Override
  public ChartBuilder save(ChartBuilder chartBuilder) throws ValidationException {

    try {
      chartBuilderService.build(chartBuilder);
    } catch (AxelorException | JAXBException e) {
      refresh(chartBuilder);
      throw new ValidationException(e.getMessage());
    }

    return super.save(chartBuilder);
  }

  @Override
  @Transactional
  public void remove(ChartBuilder chartBuilder) {

    MetaView metaView = chartBuilder.getMetaViewGenerated();
    List<ChartBuilder> chartBuilders =
        all()
            .filter("self.metaViewGenerated = ?1 and self.id != ?2", metaView, chartBuilder.getId())
            .fetch();
    for (ChartBuilder builder : chartBuilders) {
      builder.setMetaViewGenerated(null);
    }

    if (metaView != null) {
      metaViewRepo.remove(metaView);
    }

    super.remove(chartBuilder);
  }
}
