/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;
import com.axelor.apps.purchase.service.CallTenderAttrConfigService;
import com.axelor.inject.Beans;
import jakarta.persistence.PersistenceException;

public class CallTenderAttrConfigManagementRepository extends CallTenderAttrConfigRepository {

  @Override
  public CallTenderAttrConfig save(CallTenderAttrConfig entity) {
    CallTenderAttrConfigService service = Beans.get(CallTenderAttrConfigService.class);
    service.regenerateFieldNames(entity);
    try {
      service.validateFieldNameUniqueness(entity);
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    CallTenderAttrConfig saved = super.save(entity);
    service.syncMirrorFields(saved);
    return saved;
  }

  @Override
  public void remove(CallTenderAttrConfig entity) {
    Beans.get(CallTenderAttrConfigService.class).deleteFields(entity);
    super.remove(entity);
  }
}
