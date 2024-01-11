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
package com.axelor.apps.contract.exception;

public final class ContractExceptionMessage {

  private ContractExceptionMessage() {}

  public static final String CONTRACT_MISSING_TERMINATE_DATE = /*$$(*/
      "Please enter a terminated date for this version." /*)*/;
  public static final String CONTRACT_MISSING_ENGAGEMENT_DATE = /*$$(*/
      "Please enter a engagement date." /*)*/;
  public static final String CONTRACT_ENGAGEMENT_DURATION_NOT_RESPECTED = /*$$(*/
      "Engagement duration is not fulfilled." /*)*/;
  public static final String CONTRACT_PRIOR_DURATION_NOT_RESPECTED = /*$$(*/
      "Prior notice duration is not respected." /*)*/;
  public static final String CONTRACT_UNVALIDE_TERMINATE_DATE = /*$$(*/
      "You cannot terminate a contract before version activation date." /*)*/;
  public static final String CONTRACT_CANT_REMOVE_INVOICED_LINE = /*$$(*/
      "You cannot remove a line which has been already invoiced." /*)*/;
  public static final String CONTRACT_MISSING_FROM_VERSION = /*$$(*/
      "There is no contract associated with this version." /*)*/;
  public static final String CONTRACT_MISSING_FIRST_PERIOD = /*$$(*/
      "Please fill the first period end date and the invoice frequency." /*)*/;
  public static final String CONTRACT_VERSION_EMPTY_NEXT_CONTRACT = /*$$(*/
      "The next contract field is not set on the current contract version." /*)*/;
  public static final String CONTRACT_WAITING_WRONG_STATUS = /*$$(*/
      "Can only put on hold drafted contract." /*)*/;
  public static final String CONTRACT_ONGOING_WRONG_STATUS = /*$$(*/
      "Can only activate waiting contract." /*)*/;
  public static final String CONTRACT_TERMINATE_WRONG_STATUS = /*$$(*/
      "Can only terminate ongoing contract." /*)*/;
}
