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
package com.axelor.apps.account.service.reconcile.reconcilegroup;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

/**
 * Interface with methods to manage reconcile groups. ReconcileGroups are created and updated from
 * Reconcile services: when a reconcile is created or canceled, this service is called to update the
 * matching reconcile group.
 *
 * <p>This class should not be called outside of services directly managing reconciles (e.g. classes
 * in the package {@link com.axelor.apps.account.service.reconcile})
 */
public interface ReconcileGroupService {

  /**
   * Add a reconcile to a group and validate the group if it is balanced. If the reconcile group
   * does not exist, it is created.
   *
   * @param reconcile a reconcile.
   */
  void addAndValidateReconcileGroup(Reconcile reconcile) throws AxelorException;
}
