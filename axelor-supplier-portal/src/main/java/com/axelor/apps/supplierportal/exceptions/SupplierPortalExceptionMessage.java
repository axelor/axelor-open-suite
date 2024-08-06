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
package com.axelor.apps.supplierportal.exceptions;

public final class SupplierPortalExceptionMessage {

  private SupplierPortalExceptionMessage() {}

  public static final String PRODUCT_SUPPLIER_NO_NAME = /*$$(*/ "Product has no name." /*)*/;

  public static final String PRODUCT_SUPPLIER_NO_CODE = /*$$(*/ "Product has no code." /*)*/;

  public static final String PRODUCT_SUPPLIER_SAME_CODE = /*$$(*/
      "The code is already in database. Please choose another one." /*)*/;
}
