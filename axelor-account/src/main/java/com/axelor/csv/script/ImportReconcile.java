/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ImportReconcile {

  private ReconcileServiceImpl reconcileService;

  @Inject
  public ImportReconcile(ReconcileServiceImpl reconcileService) {
    this.reconcileService = reconcileService;
  }

  public Object updateInvoicePayments(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof Reconcile;
    Reconcile reconcile = (Reconcile) bean;
    if (reconcile.getStatusSelect() == ReconcileRepository.STATUS_CONFIRMED) {
      reconcileService.updateInvoicePayments(reconcile);
    }
    return reconcile;
  }
}
