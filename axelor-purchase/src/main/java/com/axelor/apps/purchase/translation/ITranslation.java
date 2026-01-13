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
package com.axelor.apps.purchase.translation;

public interface ITranslation {

  public static final String PURCHASE_APP_NAME = /*$$(*/ "value:Purchase"; /*)*/
  public static final String PURCHASE_REQUEST_APP_NAME = /*$$(*/ "value:Purchase Request"; /*)*/

  public static final String ABC_ANALYSIS_START_DATE = /*$$(*/ "AbcAnalysis.startDate"; /*)*/
  public static final String ABC_ANALYSIS_END_DATE = /*$$(*/ "AbcAnalysis.endDate"; /*)*/

  public static final String PURCHASE_REQUEST_UPDATED = /*$$(*/
      "Purchase request successfully updated."; /*)*/
  public static final String PURCHASE_REQUEST_CREATE_WRONG_STATUS = /*$$(*/
      "You can create a purchase request only with the status 'Draft' or 'Requested'."; /*)*/
  public static final String MISSING_PRODUCT_INFORMATION_FOR_PURCHASE_REQUEST_LINE = /*$$(*/
      "Please provide product information."; /*)*/
}
