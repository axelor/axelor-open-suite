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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import java.util.HashMap;
import wslite.json.JSONObject;

public interface AnonymizeService {
  /**
   * return a hash or hard-coded value depending on the type of the object given.
   *
   * @param object
   * @param property
   * @return
   * @throws AxelorException if an error occurs when generating a fake value.
   */
  Object anonymizeValue(Object object, Property property) throws AxelorException;

  /**
   * return a hash, hard-coded value or fake generated value depending on the type of the object
   * given.
   *
   * @param object
   * @param property
   * @return
   * @throws AxelorException if an error occurs when generating a fake value.
   */
  Object anonymizeValue(Object object, Property property, FakerApiField fakerApiField)
      throws AxelorException;

  /**
   * return a JSON with hash, hard-coded values or fake generated values.
   *
   * @param object
   * @param fakerMap
   * @return
   * @throws AxelorException
   */
  JSONObject createAnonymizedJson(Object object, HashMap<MetaJsonField, FakerApiField> fakerMap)
      throws AxelorException;

  /**
   * Return a hash of the value given.
   *
   * @param data
   * @return
   */
  String hashValue(String data);

  /**
   * Return a hash of the value given with the given salt.
   *
   * @param data
   * @param salt
   * @return
   */
  String hashValue(String data, byte[] salt);

  /**
   * Generate a random salt.
   *
   * @return
   */
  byte[] getSalt();
}
