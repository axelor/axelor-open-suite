/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ReconcileGroupService {

  /**
   * Validate the given reconcile group. A reconcile Group can be validated if it is not empty and
   * its lines are balanced.
   *
   * @param reconcileGroup a reconcileGroup
   */
  void validate(ReconcileGroup reconcileGroup) throws AxelorException;

  /**
   * Check if the given reconcile lines are balanced.
   *
   * @param reconcileList a list of reconcile.
   */
  boolean isBalanced(List<Reconcile> reconcileList) throws AxelorException;
}
