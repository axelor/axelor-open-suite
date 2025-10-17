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
package com.axelor.apps.base.service.indicator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Singleton;
import jakarta.persistence.Query;
import java.util.List;

@Singleton
public class IndicatorQueryServiceImpl implements IndicatorQueryService {

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Model> List<T> list(IndicatorConfig config) throws AxelorException {
    try {
      String jpql = config.getRecordQuery();
      Class<T> klass = resultClass(config);
      Query q = JPA.em().createQuery(jpql, klass);
      return q.getResultList();
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public <T extends Model> T loadSelected(IndicatorConfig config) throws ClassNotFoundException {
    Class<T> klass = resultClass(config);
    return JPA.find(klass, Long.valueOf(config.getRecordValue()));
  }

  @SuppressWarnings("unchecked")
  private <T extends Model> Class<T> resultClass(IndicatorConfig config)
      throws ClassNotFoundException {
    MetaModel target = config.getTargetModel();
    return (Class<T>) Class.forName(target.getFullName());
  }
}
