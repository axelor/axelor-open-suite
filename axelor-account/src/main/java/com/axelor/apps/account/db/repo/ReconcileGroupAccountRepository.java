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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.service.ReconcileGroupSequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class ReconcileGroupAccountRepository extends ReconcileGroupRepository {

  /** On first reconcile group save (e.g. when the code is not filled), we fill the sequence. */
  @Override
  public ReconcileGroup save(ReconcileGroup reconcileGroup) {
    try {
      if (Strings.isNullOrEmpty(reconcileGroup.getCode())) {
        Beans.get(ReconcileGroupSequenceService.class).fillCodeFromSequence(reconcileGroup);
      }
      return super.save(reconcileGroup);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
