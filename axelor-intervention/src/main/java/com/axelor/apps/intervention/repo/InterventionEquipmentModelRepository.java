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
package com.axelor.apps.intervention.repo;

import com.axelor.apps.intervention.db.EquipmentModel;
import com.axelor.apps.intervention.db.repo.EquipmentModelRepository;
import com.axelor.apps.intervention.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.Map;
import javax.persistence.PersistenceException;

public class InterventionEquipmentModelRepository extends EquipmentModelRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      final EquipmentModel entity = find((Long) json.get("id"));
      if (entity != null
          && entity.getTypeSelect().equals(EquipmentRepository.INTERVENTION_TYPE_EQUIPMENT)) {
        json.put(
            "_inService",
            Boolean.TRUE.equals(entity.getInService())
                ? I18n.get("In service")
                : I18n.get("Out of service"));
      } else {
        json.put("_inService", "---");
      }
    }
    return json;
  }

  @Override
  public void remove(EquipmentModel entity) {
    if (all().filter("self.parentEquipmentModel = :parent").bind("parent", entity).count() > 0) {
      throw new PersistenceException(
          I18n.get(IExceptionMessage.EQUIPMENT_MODEL_REMOVE_NOT_ALLOWED));
    }
    super.remove(entity);
  }
}
