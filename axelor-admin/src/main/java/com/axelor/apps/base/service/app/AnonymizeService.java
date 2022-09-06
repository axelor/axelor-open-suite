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

public interface AnonymizeService {
  /**
   * return a hash, hard-coded value or fake generated value depending on the type of the object
   * given.
   *
   * @param object
   * @param property
   * @param useFakeData
   * @param fakerApiField
   * @return
   * @throws AxelorException if an error occurs when generating a fake value.
   */
  Object anonymizeValue(
      Object object, Property property, boolean useFakeData, FakerApiField fakerApiField)
      throws AxelorException;
}
