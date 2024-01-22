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
package com.axelor.apps.maintenance.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.Imaintenance;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class EquipementMaintenanceRepo extends EquipementMaintenanceRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public EquipementMaintenance save(EquipementMaintenance entity) {
    try {
      if (Strings.isNullOrEmpty(entity.getCode())) {
        setCode(entity);
      }
      return super.save(entity);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected void setCode(EquipementMaintenance entity) throws AxelorException {
    String code =
        sequenceService.getSequenceNumber(
            Imaintenance.SEQ_MAINTENANCE, EquipementMaintenance.class, "code");

    if (Strings.isNullOrEmpty(code)) {
      throw new AxelorException(
          Sequence.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(MaintenanceExceptionMessage.EQUIPEMENT_MAINTENANCE_MISSING_SEQUENCE));
    }

    entity.setCode(code);
  }
}
