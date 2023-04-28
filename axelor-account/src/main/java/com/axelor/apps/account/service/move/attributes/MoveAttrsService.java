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
package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveAttrsService {

  /**
   * This method generates a map of move fields that should be hide or not for the current move
   *
   * <p>The map form is as follow: (fieldName, (attribute("hidden" in our case), attributeValue))
   *
   * @param move
   * @return generated map
   */
  Map<String, Map<String, Object>> getHiddenAttributeValues(Move move);

  /**
   * This method generates a map of move fields for the selection attribute for the current move.
   *
   * <p>The map form is as follow: (fieldName, (attribute("selection-in" in our case),
   * attributeValue))
   *
   * @param move
   * @return generated map
   */
  Map<String, Map<String, Object>> getFunctionalOriginSelectDomain(Move move);

  boolean isHiddenMoveLineListViewer(Move move);

  Map<String, Map<String, Object>> getMoveLineAnalyticAttrs(Move move) throws AxelorException;

  boolean isHiddenDueDate(Move move);
}
