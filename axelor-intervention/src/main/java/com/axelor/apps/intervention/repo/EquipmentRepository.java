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

import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.exception.IExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.JpaSequence;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import java.util.List;
import java.util.Map;

public class EquipmentRepository extends JpaRepository<Equipment> {

  public EquipmentRepository() {
    super(Equipment.class);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      final Equipment entity = find((Long) json.get("id"));
      if (entity != null && entity.getTypeSelect().equals(INTERVENTION_TYPE_EQUIPMENT)) {
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

  public List<Equipment> findByContract(Long contractId) {
    return Query.of(Equipment.class)
        .filter("self.contract.id = :contractId")
        .bind("contractId", contractId)
        .fetch();
  }

  public Equipment findByCode(String code) {
    return Query.of(Equipment.class).filter("self.code = :code").bind("code", code).fetchOne();
  }

  public Equipment findByName(String name) {
    return Query.of(Equipment.class).filter("self.name = :name").bind("name", name).fetchOne();
  }

  @Override
  public void remove(Equipment entity) {
    if (all().filter("self.parentEquipment = ?1", entity).count() == 0) {
      super.remove(entity);
      return;
    }
    throw new IllegalArgumentException(I18n.get(IExceptionMessage.EQUIPMENT_REMOVE_NO_ALLOWED));
  }

  @Override
  public Equipment save(Equipment entity) {
    if (StringUtils.isBlank(entity.getSequence())) {
      entity.setSequence(JpaSequence.nextValue("equipment.sequence"));
    }
    if (entity.getContract() != null
        && entity
            .getContract()
            .getStatusSelect()
            .equals(AbstractContractRepository.ACTIVE_CONTRACT)) {
      if (Boolean.TRUE.equals(entity.getInService())) {
        entity.setIndicatorSelect(INTERVENTION_INDICATOR_UC_OP);
      } else {
        entity.setIndicatorSelect(INTERVENTION_INDICATOR_UC_NOP);
      }
    } else {
      if (Boolean.TRUE.equals(entity.getInService())) {
        entity.setIndicatorSelect(INTERVENTION_INDICATOR_OC_OP);
      } else {
        entity.setIndicatorSelect(INTERVENTION_INDICATOR_OC_NOP);
      }
    }
    return super.save(entity);
  }

  // TYPE SELECT
  public static final String INTERVENTION_TYPE_PLACE = "place";
  public static final String INTERVENTION_TYPE_EQUIPMENT = "equipment";

  // INDICATOR SELECT
  public static final int INTERVENTION_INDICATOR_UC_OP = 1;
  public static final int INTERVENTION_INDICATOR_UC_NOP = 2;
  public static final int INTERVENTION_INDICATOR_OC_OP = 3;
  public static final int INTERVENTION_INDICATOR_OC_NOP = 4;
}
