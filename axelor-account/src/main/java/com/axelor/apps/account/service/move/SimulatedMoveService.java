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
package com.axelor.apps.account.service.move;

import com.axelor.apps.base.db.Company;

public interface SimulatedMoveService {

  /**
   * Called when deactivating simulated moves for a company. Fetches and deletes all simulated moves
   * for this company. Also ftches and updates the journals of the company.
   *
   * @param company the company for which the simulated moves have been deactivated.
   */
  public void deactivateSimulatedMoves(Company company);
}
