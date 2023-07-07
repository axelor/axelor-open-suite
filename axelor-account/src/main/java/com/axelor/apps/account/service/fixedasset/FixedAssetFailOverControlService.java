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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.base.AxelorException;

public interface FixedAssetFailOverControlService {

  /**
   * Method that controls values's consistency when on failOver.
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void controlFailOver(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Method that return true if fixedAsset is a failOver
   *
   * @param fixedAsset
   * @return boolean if FailOver is found else null
   */
  boolean isFailOver(FixedAsset fixedAsset);
}
