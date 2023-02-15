/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.exceptions;

/** @deprecated Replaced by {@link AdminExceptionMessage} */
@Deprecated
public interface IExceptionMessages {

  public static final String OBJECT_DATA_REPLACE_MISSING = /*$$(*/ "No record found for: %s" /*)*/;

  public static final String EMPTY_RELATIONAL_FIELD_IN_DATA_CONFIG_LINE = /*$$(*/
      "The 'Relational field' of the line '%s' cannot be empty." /*)*/;
  public static final String EMPTY_QUERY_IN_DATA_CONFIG_LINE = /*$$(*/
      "The 'Query' of the line '%s' cannot be empty." /*)*/;

  public static final String FAKER_METHOD_DOES_NOT_EXIST = /*$$(*/
      "The method '%s' doesn't exist in the Faker API." /*)*/;

  public static final String FAKER_CLASS_DOES_NOT_EXIST = /*$$(*/
      "The class '%s' doesn't exist in the Faker API." /*)*/;

  public static final String FAKER_METHOD_ERROR = /*$$(*/
      "An error occured while executing '%s'." /*)*/;
}
