/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

  public static final String PROJECT_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Project" /*)*/;

  public static final String PROJECT_CONFIG_1 = /*$$(*/
      "%s : You must configure Project module for company %s" /*)*/;

  public static final String PROJECT_GROOVY_FORMULA_ERROR = /*$$(*/
      "Please check the compute full name groovy formula in project app configuration" /*)*/;

  public static final String PROJECT_GROOVY_FORMULA_SYNTAX_ERROR = /*$$(*/
      "Full name could not be computed, please check the groovy formula." /*)*/;

  public static final String PROJECT_TASK_INFINITE_LOOP_ISSUE = /*$$(*/
      "Task parent or children has not been configured correctly." /*)*/;

  public static final String PROJECT_CONFIG_COMPLETED_PROJECT_STATUS_MISSING = /*$$(*/
      "Please set the completed project status in the project configuration." /*)*/;

  public static final String PROJECT_CONFIG_COMPLETED_PAID_PROJECT_STATUS_MISSING = /*$$(*/
      "Please set the completed paid project status in the project configuration." /*)*/;

  public static final String PROJECT_CODE_ERROR =
      /*$$(*/ "Project code is already used. Please provide unique code" /*)*/;

  public static final String LINK_TYPE_UNAVAILABLE_IN_PROJECT_CONFIG = /*$$(*/
      "Please configure the project %s with project task link type %s if you want to create this link." /*)*/;

  public static final String PROJECT_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS = /*$$(*/
      "Please configure the completed task status in the project config panel." /*)*/;

  public static final String CATEGORY_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS = /*$$(*/
      "Please configure the completed task status of the project task category." /*)*/;

  public static final String TASK_STATUS_USED_ON_PROJECT_TASK_CATEGORY = /*$$(*/
      "This task status is used on project task category for the auto progress process. Do you want to update all unmodified values with this one ?" /*)*/;

  public static final String BATCH_TASK_STATUS_UPDATE_TASK = /*$$(*/ "Task %s" /*)*/;

  public static final String BATCH_TASK_STATUS_UPDATE_PROJECT = /*$$(*/ "Project %s" /*)*/;

  public static final String BATCH_TASK_STATUS_UPDATE_PROJECT_TASK_CATEGORY = /*$$(*/
      "Project task category %s" /*)*/;

  public static final String BATCH_TASK_STATUS_UPDATE_2 = /*$$(*/
      "Tasks status update completed:" /*)*/;

  public static final String BATCH_TASK_STATUS_UPDATE_DONE = /*$$(*/
      "* %s project task updated" /*)*/;
  public static final String PROJECT_TASK_NO_UNIT_FOUND = /*$$(*/
      "Please set the unit for the task %s." /*)*/;

  public static final String PROJECT_NO_UNIT_FOUND = /*$$(*/
      "Please set the unit for the project %s." /*)*/;

  public static final String PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING = /*$$(*/
      "Please set the default number of hours per day in the project configuration." /*)*/;

  public static final String SPRINT_GENERATED = /*$$(*/ "%s sprints have been generated" /*)*/;

  public static final String SPRINT_FIELDS_MISSING =
      /*$$(*/ "Please fill all dates and the number of days in a sprint with valid values." /*)*/;

  public static final String PROJECT_VERSION_WITH_SAME_PROJECT_ALREADY_EXISTS = /*$$(*/
      "Warning, at least 2 versions with the title %s are used on the project(s) %s." /*)*/;

  public static final String PROJECT_SPRINTS_OVERLAPPED =
      /*$$(*/ "The project contains overlapping sprints. Please correct the date ranges." /*)*/;
}
