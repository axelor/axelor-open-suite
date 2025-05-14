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
package com.axelor.apps.sale.translation;

public interface ITranslation {

  public static final String SALE_APP_NAME = /*$$(*/ "value:Sale"; /*)*/

  public static final String ABC_ANALYSIS_START_DATE = /*$$(*/ "AbcAnalysis.startDate"; /*)*/
  public static final String ABC_ANALYSIS_END_DATE = /*$$(*/ "AbcAnalysis.endDate"; /*)*/
  public static final String SALE_ORDER_LINE_END_OF_PACK = /*$$(*/ "SaleOrderLine.endOfPack"; /*)*/
  public static final String SALE_ORDER_LINE_TOTAL = /*$$(*/ "SaleOrderLine.total"; /*)*/

  public static final String SALE_ORDER_LINE_OBSERVER_NO_PRICING = /*$$(*/
      "No pricing scale used for this record"; /*)*/
  public static final String PRICING_CUSTOM_TITLE_PREVIOUS_PRICING = /*$$(*/
      "Previous pricing"; /*)*/
  public static final String PRICING_CUSTOM_TITLE_NEXT_PRICING = /*$$(*/ "Next pricing"; /*)*/
  public static final String CONFIGURATOR_VERSION_IS_DIFFERENT =
      /*$$(*/ "Configurator version is different than the version of its configurator creator, therefore only a simple duplicate will be created, proceed ?"; /*)*/
}
