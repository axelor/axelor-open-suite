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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Optional;

public interface ReconcileGroupService {

  /**
   * Validate the given reconcile group. A reconcile Group can be validated if it is not empty and
   * its lines are balanced.
   *
   * @param reconcileGroup a reconcile group.
   * @param reconcileList a list of reconcile.
   * @throws AxelorException if the reconcile list is empty.
   */
  void validate(ReconcileGroup reconcileGroup, List<Reconcile> reconcileList)
      throws AxelorException;

  /**
   * Check if the given reconcile lines are balanced.
   *
   * @param reconcileList a list of reconcile.
   */
  boolean isBalanced(List<Reconcile> reconcileList) throws AxelorException;

  /**
   * Call {@link ReconcileGroupService#findOrMergeGroup} to get a reconcile group. If not found,
   * create one with {@link ReconcileGroupService#createReconcileGroup}
   *
   * @param reconcile a confirmed reconcile
   * @return the created or found group.
   */
  ReconcileGroup findOrCreateGroup(Reconcile reconcile);

  /**
   * Find the corresponding group for a given reconcile. If two or more reconcile group are found,
   * then return the merge between them.
   *
   * @param reconcile a confirmed reconcile.
   * @return an optional with the reconcile group if it was found. Else an empty optional.
   */
  Optional<ReconcileGroup> findOrMergeGroup(Reconcile reconcile);

  /**
   * Merge reconcile groups into one. The created reconcile group will have a new sequence and all
   * reconcile lines from the groups.
   *
   * @param reconcileGroupList a non empty list of reconcile group to merge.
   * @return the created reconcile group.
   */
  ReconcileGroup mergeReconcileGroups(List<ReconcileGroup> reconcileGroupList);

  /**
   * Create a reconcile group with the given reconcile.
   *
   * @param company a confirmed reconcile.
   * @return a new reconcile group.
   */
  ReconcileGroup createReconcileGroup(Company company);

  /**
   * Add a reconcile to a group and validate the group if it is balanced.
   *
   * @param reconcileGroup a reconcileGroup.
   * @param reconcile a reconcile.
   */
  void addAndValidate(ReconcileGroup reconcileGroup, Reconcile reconcile) throws AxelorException;

  /**
   * Add the reconcile and its move line to the reconcile group.
   *
   * @param reconcileGroup a reconcileGroup.
   * @param reconcile the confirmed reconcile to be added.
   */
  void addToReconcileGroup(ReconcileGroup reconcileGroup, Reconcile reconcile);

  /**
   * Remove a reconcile from a reconcile group then update the group.
   *
   * @param reconcile a reconcile with a reconcile group.
   */
  void remove(Reconcile reconcile) throws AxelorException;

  /**
   * Update the status and the sequence of a reconcile group.
   *
   * @param reconcileGroup
   */
  void updateStatus(ReconcileGroup reconcileGroup) throws AxelorException;

  /**
   * Unletter every moveline and update unlettering date.
   *
   * @param reconcileGroup
   * @throws AxelorException
   */
  void unletter(ReconcileGroup reconcileGroup) throws AxelorException;
}
