/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.suppliermanagement.exceptions;

public interface IExceptionMessage {

  /** Purchase order supplier line service */
  static final String CURRENCY_CONVERSION_2 = /*$$(*/
      "WARNING : To Date must be after or equals to From Date" /*)*/;

  /** Purchase order Controller */
  static final String PURCHASE_ORDER_1 = /*$$(*/ "Supplier's consulting's request created" /*)*/;

  static final String PURCHASE_ORDER_2 = /*$$(*/ "Suppliers orders' generation over" /*)*/;
}
