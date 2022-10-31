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
package com.axelor.studio.exception;

/** @deprecated Replaced by {@link StudioExceptionMessage} */
@Deprecated
public interface IExceptionMessage {

  /** Check if app builder code is not conflicting with existing app. */
  static final String APP_BUILDER_1 = /*$$(*/
      "Please provide unique code. The code '%s' is already used" /*)*/;

  /** Check if chart name doesn't contains any space. */
  static final String CHART_BUILDER_1 = /*$$(*/ "The name must not contain spaces" /*)*/;

  static final String CANNOT_ALTER_NODES = /*$$(*/
      "Can't alter nodes for real existing selection field" /*)*/;

  static final String O2O_ASSOCIATION_EXCEED = /*$$(*/
      "An object that is already associated with O2O mapping should not be associated once again" /*)*/;

  static final String NAMED_PARAMETER_EXCEPTION = /*$$(*/
      "The Query Filter of %s should not contain any string that resembles a Named Parameter" /*)*/;

  static final String NAMED_PARAMETER_BOUND_EXCEPTION = /*$$(*/
      "Named Parameter %s is not bound in %s " /*)*/;

  static final String DATAFORM_NOT_EXIST = /*$$(*/
      "DataForm with the given code %s does not exists" /*)*/;

  static final String DATAFORM_MODEL_MISMATCH = /*$$(*/
      "The Model associated with the DataForm is %s. Cannot create a record of %s" /*)*/;
}
