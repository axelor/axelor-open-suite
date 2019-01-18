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
package com.axelor.apps.contract.exception;

public interface IExceptionMessage {

  String CONTRACT_MISSING_TERMINATE_DATE = /*$$(*/
      "Please enter a terminated date for this version." /*)*/;
  String CONTRACT_MISSING_ENGAGEMENT_DATE = /*$$(*/ "Please enter a engagement date." /*)*/;
  String CONTRACT_ENGAGEMENT_DURATION_NOT_RESPECTED = /*$$(*/
      "Engagement duration is not fulfilled." /*)*/;
  String CONTRACT_PRIOR_DURATION_NOT_RESPECTED = /*$$(*/
      "Prior notice duration is not respected." /*)*/;
  String CONTRACT_UNVALIDE_TERMINATE_DATE = /*$$(*/
      "You cannot terminate a contract before version activation date." /*)*/;
  String CONTRACT_CANT_REMOVE_INVOICED_LINE = /*$$(*/
      "You cannot remove a line which has been already invoiced." /*)*/;
  String CONTRACT_EMPTY_PRODUCT = /*$$(*/ "The product can't be empty." /*)*/;
  String CONTRACT_MISSING_FROM_VERSION = /*$$(*/
      "There is no contract associated with this version." /*)*/;
  String CONTRACT_MISSING_FIRST_PERIOD = /*$$(*/
      "Please fill the first period end date and the invoice frequency." /*)*/;
}
