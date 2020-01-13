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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.service.ReconcileGroupSequenceService;
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
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
