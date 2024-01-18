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
package com.axelor.apps.production.translation;

public interface ITranslation {

  public static final String MANUFACTURING_APP_NAME = /*$$(*/ "value:Manufacturing"; /*)*/
  public static final String WORK_IN_PROGRESS_VALUATION = /*$$(*/
      "Work in progress valuation"; /*)*/
  public static final String MPS_CHARGE = /*$$(*/ "Mps Charge"; /*)*/
  public static final String PRODUCTION_COMMENT = /*$$(*/
      "Please take the following comment into account:" /*)*/;

  public static final String OPERATION_ORDER_DURATION_PAUSED_200 = /*$$(*/
      "Your time on this operation is paused. The status of the operation has not been updated as someone is still working on it." /*)*/;

  public static final String OPERATION_ORDER_WORKFLOW_NOT_SUPPORTED = /*$$(*/
      "This workflow is not supported for operation order status." /*)*/;

  public static final String OPERATION_ORDER_DURATION_PAUSED_403 = /*$$(*/
      "Your time on this operation is paused. This operation cannot be stopped because other operators are still working on it. You can go to the web instance to force stop the operation." /*)*/;
}
