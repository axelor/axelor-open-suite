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
package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import java.util.HashMap;
import java.util.List;
import wslite.json.JSONException;
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
   * return a hash or hard-coded value for a JSON field.
   *
   * @param object
   * @param property
   * @param metaJsonField
   * @return
   * @throws JSONException
   */
  Object anonymizeJsonValue(Object object, Property property, MetaJsonField metaJsonField)
      throws JSONException;

  /**
   * return a hash, hard-coded or fake generated value for a JSON field.
   *
   * @param object
   * @param property
   * @param metaJsonField
   * @param fakerApiField
   * @return
   * @throws JSONException
   * @throws AxelorException
   */
  Object anonymizeJsonValue(
      Object object, Property property, MetaJsonField metaJsonField, FakerApiField fakerApiField)
      throws JSONException, AxelorException;

  /**
   * return a JSON with hash or hard-coded values.
   *
   * @param object
   * @param metaJsonFieldList
   * @return
   * @throws AxelorException
   */
  JSONObject createAnonymizedJson(Object object, List<MetaJsonField> metaJsonFieldList)
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
}
