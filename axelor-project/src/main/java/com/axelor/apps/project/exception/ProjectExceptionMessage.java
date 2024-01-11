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
package com.axelor.apps.project.exception;

public final class ProjectExceptionMessage {

  private ProjectExceptionMessage() {}

  public static final String PROJECT_PLANNING_NO_TASK = /*$$(*/
      "You have no projects or tasks bound to you, your planning can't be generated." /*)*/;
  public static final String PROJECT_PLANNING_NO_TASK_TEAM = /*$$(*/
      "Your team has no projects or tasks bound to it, the planning can't be generated." /*)*/;
  public static final String PROJECT_NO_ACTIVE_TEAM = /*$$(*/
      "You have no active team, the planning can't be generated" /*)*/;
  public static final String PROJECT_NO_TEAM = /*$$(*/
      "You have selected no team for this project" /*)*/;
  public static final String JSON_FIELD_MODEL_INVALID = /*$$(*/ "Invalid model name" /*)*/;
  public static final String JSON_FIELD_SELECTION_NULL = /*$$(*/ "Invalid selection name" /*)*/;
  public static final String PROJECT_TASK_FREQUENCY_END_DATE_CAN_NOT_BE_BEFORE_TASK_DATE = /*$$(*/
      "Frequency end date cannot be before task date." /*)*/;

  public static final String PROJECT_TASK_FILL_TASK_DATE = /*$$(*/
      "Please fill in task date." /*)*/;

  public static final String TASK_TEMPLATE_PARENT_TASK_CREATED_LOOP = /*$$(*/
      "The parent task creates a loop in the task tree." /*)*/;
  public static final String RESOURCE_ALREADY_BOOKED_ERROR_MSG = /*$$(*/
      "This resource is already booked for this period" /*)*/;
}
