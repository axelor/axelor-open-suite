/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.ParkModel;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ParkModelService {
  List<Map<String, Object>> getEquipmentList(ParkModel parkModel);

  @Transactional(rollbackOn = Exception.class)
  List<Long> generateEquipments(
      ParkModel parkModel,
      Partner partner,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Contract contract,
      Map<Long, Integer> quantitiesMap);
}
