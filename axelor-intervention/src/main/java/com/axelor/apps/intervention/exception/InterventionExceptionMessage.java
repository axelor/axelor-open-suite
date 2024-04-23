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
package com.axelor.apps.intervention.exception;

public interface InterventionExceptionMessage {

  String EQUIPMENT_MODEL_REMOVE_NOT_ALLOWED = /*$$(*/
      "Impossible to remove equipment model that has children's equipment models" /*)*/;

  String EQUIPMENT_REMOVE_NO_ALLOWED = /*$$(*/
      "Impossible to remove equipment that has children's equipment" /*)*/;

  String MESSAGE_ON_EXCEPTION = /*$$(*/ "Error happened during process execution" /*)*/;

  String MISSING_INTERVENTION_FIELD_TO_PLAN = /*$$(*/
      "Unable to create a planification for this intervention. Please check planification fields." /*)*/;

  String ALL_REQUIRED_QUESTIONS_NOT_ANSWERED = /*$$(*/
      "All required questions of the survey must be answered to finish the intervention." /*)*/;

  String CUSTOMER_REQUEST_NO_SEQUENCE = /*$$(*/
      "There is no configured sequence for customer request." /*)*/;

  String INTERVENTION_NO_SEQUENCE = /*$$(*/
      "There is no configured sequence for intervention." /*)*/;

  String INTERVENTION_MISSING_FIELDS = /*$$(*/
      "Customer request can't be generated as some required fields are missing." /*)*/;

  String INTERVENTION_API_WRONG_STATUS = /*$$(*/
      "Cannot go to requested status from current status." /*)*/;

  String INTERVENTION_API_SAME_STATUS = /*$$(*/ "Already in requested status." /*)*/;

  String INTERVENTION_API_MISSING_USER_ID = /*$$(*/
      "No planned technician user id in request body." /*)*/;

  String INTERVENTION_API_MISSING_PLANNED_DURATION = /*$$(*/
      "No planned duration in request body." /*)*/;

  String INTERVENTION_API_MISSING_DATE_TIME = /*$$(*/ "No date time in request body." /*)*/;

  String INTERVENTION_API_EQUIPMENT_NOT_FOUND = /*$$(*/
      "Could not find equipment with id %s." /*)*/;

  String INTERVENTION_API_PICTURE_NOT_FOUND = /*$$(*/ "Could not find picture with id %s." /*)*/;
}
