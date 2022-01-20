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
package com.axelor.apps.project.translation;

public interface ITranslation {

  public static final String PROJECTS_APP_NAME = /* $$( */ "value:Projects"; /* ) */

  public static final String PROJECT_CODE_ERROR =
      /* $$( */ "Project code is already used. Please provide unique code"; /* ) */

  // Project Status
  public static final String PROJECT_STATUS_NEW = /*$$(*/ "value:New"; /*)*/

  public static final String PROJECT_STATUS_IN_PROGRESS = /*$$(*/ "value:In progress"; /*)*/

  public static final String PROJECT_STATUS_DONE = /*$$(*/ "value:Done"; /*)*/

  public static final String PROJECT_STATUS_CANCELED = /*$$(*/ "value:Canceled"; /*)*/

  // Project Priority
  public static final String PROJECT_PRIORITY_LOW = /*$$(*/ "value:Low"; /*)*/

  public static final String PROJECT_PRIORITY_HIGH = /*$$(*/ "value:High"; /*)*/
}
