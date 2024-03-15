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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.TourLine;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.collections.CollectionUtils;

public class TourManagementRepository extends TourRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Map<String, Object> map = super.populate(json, context);

    Long id = (Long) map.get("id");
    Tour tour = find(id);
    List<TourLine> tourLineList = tour.getTourLineList();

    boolean isValidated = false;

    if (CollectionUtils.isNotEmpty(tourLineList)) {
      isValidated = tourLineList.stream().noneMatch(Predicate.not(TourLine::getIsValidated));
    }

    map.put("$isValidated", isValidated);
    return map;
  }

  public Tour copy(Tour entity, boolean deep) {
    Tour tour = super.copy(entity, deep);
    for (TourLine line : tour.getTourLineList()) {
      line.setIsValidated(false);
    }
    return tour;
  }
}
