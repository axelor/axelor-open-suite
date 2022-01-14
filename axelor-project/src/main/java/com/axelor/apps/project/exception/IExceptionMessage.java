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
  static final String PROJECT_NO_ACTIVE_TEAM = /*$$(*/
      "You have no active team, the planning can't be generated" /*)*/;
  static final String PROJECT_NO_TEAM = /*$$(*/ "You have selected no team for this project" /*)*/;
  static final String PROJECT_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Project" /*)*/;

  static final String PROJECT_TASK_FREQUENCY_END_DATE_CAN_NOT_BE_BEFORE_TASK_DATE = /*$$(*/
      "Frequency end date cannot be before task date." /*)*/;

  static final String PROJECT_TASK_FILL_TASK_DATE = /*$$(*/ "Please fill in task date." /*)*/;

  static final String TASK_TEMPLATE_PARENT_TASK_CREATED_LOOP = /*$$(*/
      "The parent task creates a loop in the task tree." /*)*/;
  static final String RESOURCE_ALREADY_BOOKED_ERROR_MSG = /*$$(*/
      "This resource is already booked for this period" /*)*/;
}
