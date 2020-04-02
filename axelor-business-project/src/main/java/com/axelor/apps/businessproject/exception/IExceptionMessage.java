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
package com.axelor.apps.businessproject.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 */
public interface IExceptionMessage {

  static final String FOLDER_TEMPLATE = /*$$(*/ "You must add a sale order template" /*)*/;
  static final String INVOICING_PROJECT_EMPTY = /*$$(*/
      "You haven't select any element to invoice" /*)*/;
  static final String INVOICING_PROJECT_USER = /*$$(*/
      "The project/task selected doesn't have any responsible" /*)*/;
  static final String INVOICING_PROJECT_PROJECT = /*$$(*/ "You must select a project/task" /*)*/;
  static final String INVOICING_PROJECT_PROJECT_PARTNER = /*$$(*/
      "There is no customer for this project/task" /*)*/;
  static final String INVOICING_PROJECT_PROJECT_PRODUCT = /*$$(*/
      "You haven't select a product to invoice for the task %s" /*)*/;
  static final String INVOICING_PROJECT_PROJECT_COMPANY = /*$$(*/
      "You haven't select a company on the main project" /*)*/;
  static final String SALE_ORDER_NO_PROJECT = /*$$(*/ "No Project selected" /*)*/;
  static final String SALE_ORDER_NO_LINES = /*$$(*/ "No Line can be used for tasks" /*)*/;
  static final String SALE_ORDER_NO_TYPE_GEN_PROJECT = /*$$(*/
      "No type of generation project has been selected" /*)*/;
  static final String SALE_ORDER_BUSINESS_PROJECT = /*$$(*/
      "The project is configured to be alone" /*)*/;
  static final String INVOICING_PROJECT_GENERATION = /*$$(*/ "Invoicing project generated" /*)*/;
  static final String JOB_COSTING_APP = /*$$(*/ "Job costing" /*)*/;
}
