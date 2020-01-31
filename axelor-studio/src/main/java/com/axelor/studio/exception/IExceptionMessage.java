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
package com.axelor.studio.exception;

public interface IExceptionMessage {

  /** Check if app builder code is not conflicting with existing app. */
  static final String APP_BUILDER_1 = /*$$(*/
      "Please provide unique code. The code '%s' is already used" /*)*/;

  /** Check if chart name doesn't contains any space. */
  static final String CHART_BUILDER_1 = /*$$(*/ "Name must not contains space" /*)*/;

  /** Message to display on click of edit icon of node or transition if workflow is not saved. */
  static final String WKF_1 = /*$$(*/ "Workflow is not saved" /*)*/;
}
