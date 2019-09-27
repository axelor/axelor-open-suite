/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.exception.AxelorException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import wslite.json.JSONException;

public interface MapRestService {

  /**
   * Set data response.
   *
   * @param mainNode
   * @param arrayNode
   * @throws AxelorException
   * @throws JSONException
   */
  void setData(ObjectNode mainNode, ArrayNode arrayNode) throws AxelorException, JSONException;

  /**
   * Set error response.
   *
   * @param mainNode
   * @param e
   */
  void setError(ObjectNode mainNode, Exception e);

  /**
   * Make address string.
   *
   * @param address
   * @param objectNode
   * @return
   * @throws AxelorException
   * @throws JSONException
   */
  String makeAddressString(Address address, ObjectNode objectNode)
      throws AxelorException, JSONException;
}
