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
package com.axelor.apps.project.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 */
public interface IExceptionMessage {

  static final String PROJECT_PLANNING_NO_TASK = /*$$(*/
      "You have no projects or tasks bound to you, your planning can't be generated." /*)*/;
  static final String PROJECT_PLANNING_NO_TASK_TEAM = /*$$(*/
      "Your team has no projects or tasks bound to it, the planning can't be generated." /*)*/;
  static final String PROJECT_CUSTOMER_PARTNER = /*$$(*/
      "The selected project/task doesn't contain any customers" /*)*/;
  static final String PROJECT_DEEP_LIMIT_REACH = /*$$(*/
      "The deep limit of the project is too high" /*)*/;
  static final String PROJECT_NO_ACTIVE_TEAM = /*$$(*/
      "You have no active team, the planning can't be generated" /*)*/;
  static final String PROJECT_NO_TEAM = /*$$(*/ "You have selected no team for this project" /*)*/;
}
