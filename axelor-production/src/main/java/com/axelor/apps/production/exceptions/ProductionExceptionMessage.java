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
package com.axelor.apps.production.exceptions;

public final class ProductionExceptionMessage {

  private ProductionExceptionMessage() {}

  /** Production order service */
  public static final String PRODUCTION_ORDER_SEQ = /*$$(*/
      "There's no configured sequence for production's orders" /*)*/;

  /** Operation order service */
  public static final String PRODUCTION_PROCESS_IS_EMPTY = /*$$(*/
      "Your production process is empty." /*)*/;

  /** Production order sale order service */
  public static final String PRODUCTION_ORDER_SALES_ORDER_NO_BOM = /*$$(*/
      "There no's defined nomenclature for product %s (%s)" /*)*/;

  /** Manuf order service */
  public static final String MANUF_ORDER_SEQ = /*$$(*/
      "There's no configured sequence for fabrication's orders" /*)*/;

  public static final String CANNOT_DELETE_REALIZED_STOCK_MOVE_LINES = /*$$(*/
      "You cannot delete realized stock move lines." /*)*/;

  public static final String CHECK_BOM_AND_PROD_PROCESS = /*$$(*/
      "The production process and the bill of material must be applicable" /*)*/;

  /** Batch Compute work in progress valuation */
  public static final String BATCH_COMPUTE_VALUATION = /*$$(*/
      "* %s Computed work in progress valuation" /*)*/;

  public static final String IN_OR_OUT_INVALID_ARG = /*$$(*/ "inOrOut is invalid" /*)*/;

  /** Bill of Materials Service */
  public static final String BOM_1 = /*$$(*/ "Personalized" /*)*/;

  public static final String BOM_MISSING_UNIT_ON_PRODUCT = /*$$(*/
      "Please fill unit for product %s." /*)*/;

  /** Production Order Wizard and controller */
  public static final String PRODUCTION_ORDER_1 = /*$$(*/ "Production's order created" /*)*/;

  public static final String PRODUCTION_ORDER_2 = /*$$(*/
      "Error during production's order's creation" /*)*/;
  public static final String PRODUCTION_ORDER_3 = /*$$(*/ "You must add a positive quantity" /*)*/;
  public static final String PRODUCTION_ORDER_4 = /*$$(*/ "You must select a nomenclature" /*)*/;
  public static final String PRODUCTION_ORDER_5 = /*$$(*/ "Invalid date" /*)*/;

  /** Production Config Service */
  public static final String PRODUCTION_CONFIG_1 = /*$$(*/
      "You must configure a production for company %s" /*)*/;

  public static final String PRODUCTION_CONFIG_2 = /*$$(*/
      "You must configure a production virtual stock location for company %s" /*)*/;
  public static final String PRODUCTION_CONFIG_3 = /*$$(*/
      "You must configure a waste stock location for company %s." /*)*/;
  public static final String PRODUCTION_CONFIG_4 = /*$$(*/
      "You must configure a finished products default stock location for company %s." /*)*/;
  public static final String PRODUCTION_CONFIG_5 = /*$$(*/
      "You must configure a component default stock location for company %s." /*)*/;
  public static final String PRODUCTION_CONFIG_MISSING_MANUF_ORDER_SEQ = /*$$(*/
      "You must configure a sequence for manufacturing order for company %s" /*)*/;
  public static final String PRODUCTION_CONFIG_STOCK_LOCATION_NOT_OUTSOURCING = /*$$(*/
      "Your production virtual stock location is not an outsourcing location." /*)*/;

  /** Manuf Order Controller */
  public static final String MANUF_ORDER_1 = /*$$(*/
      "Please select the Manufacturing order(s) to print." /*)*/;

  public static final String MANUF_ORDER_ONLY_ONE_SELECTED = /*$$(*/
      "Please select more than one manufacturing order." /*)*/;

  public static final String MANUF_ORDER_MERGE_VALIDATION = /*$$(*/
      "Are you sure you want to merge those manufacturing orders?" /*)*/;

  public static final String MANUF_ORDER_NO_ONE_SELECTED = /*$$(*/
      "Please select something to merge" /*)*/;

  public static final String MANUF_ORDER_MERGE_ERROR = /*$$(*/
      "The merge must concern only manufacturing orders with Draft or Planned status, with the same products and same workshop stock location and with all bill of materials compatibles." /*)*/;

  public static final String MANUF_ORDER_MERGE_ERROR_MANAGE_WORKSHOP_FALSE = /*$$(*/
      "The merge must concern only manufacturing orders with Draft or Planned status, with the same products and with all bill of materials compatibles." /*)*/;

  /** Operation Order Controller */
  public static final String OPERATION_ORDER_1 = /*$$(*/
      "Please select the Operation order(s) to print." /*)*/;

  /** Production Order Controller */
  public static final String PRODUCTION_ORDER_NO_GENERATION = /*$$(*/
      "No production order could be generated. Make sure that everything has been configured correctly. Reminder: check that the order lines that should be produced have their supply method set to 'produce' and that the chosen BoM has a production process associated to it." /*)*/;

  /** ProdProcess service */
  public static final String PROD_PROCESS_USELESS_PRODUCT = /*$$(*/
      "The product %s is not in the bill of materials related to this production process" /*)*/;

  public static final String PROD_PROCESS_MISS_PRODUCT = /*$$(*/
      "Not enough quantity in products to consume for: %s" /*)*/;

  public static final String CHARGE_MACHINE_DAYS = /*$$(*/ "Too many days" /*)*/;

  public static final String PROD_PROCESS_LINE_MISSING_WORK_CENTER = /*$$(*/
      "Work center is missing from prod process line %s-%s." /*)*/;

  /** Bill of materials service */
  public static final String COST_TYPE_CANNOT_BE_CHANGED = /*$$(*/
      "The product cost cannot be changed because the product cost type is not manual" /*)*/;

  public static final String MAX_DEPTH_REACHED = /*$$(*/
      "Max depth reached when copying BOM." /*)*/;

  /** Configurator Controller */
  public static final String BILL_OF_MATERIAL_GENERATED = /*$$(*/
      "The bill of materials %s has been generated" /*)*/;

  /** Configurator Bom Service */
  public static final String CONFIGURATOR_BOM_TOO_MANY_CALLS = /*$$(*/
      "Too many recursive calls to create the bill of materials." /*)*/;

  public static final String CONFIGURATOR_BOM_IMPORT_TOO_MANY_CALLS = /*$$(*/
      "Too many recursive calls to import the bill of material configurator." /*)*/;

  public static final String CONFIGURATOR_BOM_IMPORT_GENERATED_PRODUCT_NULL = /*$$(*/
      "Error while generating bill of material: the product of the bill of material is supposed to be generated from the configurator but the configurator did not generate a product." /*)*/;

  public static final String CONFIGURATOR_BOM_IMPORT_FORMULA_PRODUCT_NULL = /*$$(*/
      "Error while generating bill of material: the product of the bill of material is supposed to be computed from a script but the script did not return a product." /*)*/;

  public static final String CONFIGURATOR_BOM_IMPORT_FILLED_PRODUCT_NULL = /*$$(*/
      "Error while generating bill of material: the product of the bill of material is supposed to be filled in the configurator BOM but it was empty." /*)*/;

  public static final String CONFIGURATOR_BOM_INCONSISTENT_CONDITION = /*$$(*/
      "The condition formula to generate the bill of material returns null value or is not consistent. Please correct on configurator BOM id : %s." /*)*/;

  /** Stock move line production controller */
  public static final String STOCK_MOVE_LINE_UNKNOWN_PARENT_CONTEXT = /*$$(*/
      "Unknown parent context class." /*)*/;

  /** Production Order Controller */
  public static final String MANUF_ORDER_NO_GENERATION = /*$$(*/
      "Cannot add a manufacturing order without a production process. Please check that your chosen bill of materials has a valid production process." /*)*/;

  public static final String MANUF_ORDER_MISSING_TEMPLATE = /*$$(*/
      "The template to send message for manufacturing order is missing." /*)*/;

  /** Operation Order Workflow Service */
  public static final String WORKCENTER_NO_MACHINE = /*$$(*/
      "Please fill the machine in the workcenter %s." /*)*/;

  public static final String NO_WORK_CENTER_GROUP = /*$$(*/
      "Please fill the work center group with at least one work center." /*)*/;

  /** Raw Material RequirementService */
  public static final String RAW_MATERIAL_REQUIREMENT_NO_SEQUENCE = /*$$(*/
      "Error : You must configure a raw material requirement reporting sequence for the company %s" /*)*/;

  public static final String ORDER_REMOVE_NOT_OK = /*$$(*/ "You can't remove this record" /*)*/;

  public static final String MANUF_ORDER_CANCEL = /*$$(*/
      "The manufacturing order was canceled." /*)*/;

  public static final String MANUF_ORDER_CANCEL_REASON_ERROR = /*$$(*/
      "A cancel reason must be selected" /*)*/;

  public static final String MANUF_ORDER_EMAIL_NOT_SENT = /*$$(*/
      "Automatic email was not sent because no default email account and/or no valid email account was found : please create one." /*)*/;

  public static final String MANUF_STOCK_MOVE_ERROR_1 = /*$$(*/
      "All products has been consumed" /*)*/;

  public static final String UNIT_COST_CALCULATION_IMPORT_FAIL_ERROR = /*$$(*/
      "Data import failed" /*)*/;

  public static final String UNIT_COST_CALCULATION_IMPORT_CSV_ERROR = /*$$(*/
      "Uploaded file is not a CSV file" /*)*/;

  public static final String UNIT_COST_CALCULATION_CHOOSE_FILTERS = /*$$(*/
      "Please select an element (a product, a product category or a product family) to run calculation" /*)*/;

  public static final String UNIT_COST_CALCULATION_NO_PRODUCT_FOUND = /*$$(*/
      "No product has been found with these filters. Please check your filters" /*)*/;

  public static final String NO_PRODUCT_SELECTED = /*$$(*/
      "Please select at least one product in the list." /*)*/;

  public static final String DUPLICATE_PRODUCT_SELECTED = /*$$(*/
      "Multiple same product selected in the list." /*)*/;

  public static final String MO_CREATED = /*$$(*/ "%d MO created." /*)*/;

  // CostSheetLine service
  public static final String MISSING_PRODUCT_PURCHASE_CURRENCY = /*$$(*/
      "Purchase currency is missing for product %s, please configure it." /*)*/;

  // Production Order Sale Order Service
  public static final String CHILD_BOM_TOO_MANY_ITERATION = /*$$(*/
      "Too many iterations when searching for children bills of materials. Please check for bill of materials being in its own component list." /*)*/;
  //  Mrp service
  public static final String MRP_BOM_LEVEL_TOO_HIGH = /*$$(*/
      "Configuration issue: the MRP execution was canceled because a loop was detected when searching for components in BOM." /*)*/;
  public static final String MRP_BOM_LEVEL_TOO_HIGH_PRODUCT = /*$$(*/
      "Configuration issue in product %s: the MRP execution was canceled because a loop was detected when searching for components in BOM." /*)*/;

  // Configurator Prod Process Service
  public static final String CONFIGURATOR_PROD_PROCESS_INCONSISTENT_NAME_FORMULA = /*$$(*/
      "The formula script to fill the name returns null value. Please correct on prod process configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PROCESS_INCONSISTENT_NULL_NAME = /*$$(*/
      "Name cannot be null for prod process to generate. Please correct on prod process configurator id : %s." /*)*/;

  public static final String
      CONFIGURATOR_PROD_PROCESS_INCONSISTENT_IS_CONS_PRO_ON_OPERATION_FORMULA = /*$$(*/
          "The formula script to fill the checkbox manage consumed products on phases returns null value or is not consistent. Please correct on prod process configurator id : %s." /*)*/;

  // Configurator Prod Process Line Service
  public static final String CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_NAME_FORMULA = /*$$(*/
      "The formula script to fill the name returns null value. Please correct on prod process line configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_NULL_NAME = /*$$(*/
      "Name cannot be null for prod process line to generate. Please correct on prod process line configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_CONDITION = /*$$(*/
      "The condition formula to generate the prod process line returns null value or is not consistent. Please correct on prod process line configurator id : %s." /*)*/;

  public static final String
      CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_WORK_CENTER_FORMULA = /*$$(*/
          "The formula script to fill the work center returns null value. Please correct on prod process line configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_NULL_WORK_CENTER = /*$$(*/
      "Work center cannot be null for prod process line to generate. Please correct on prod process line configurator id : %s." /*)*/;

  public static final String
      CONFIGURATOR_PROD_PROCESS_LINE_INCONSISTENT_NULL_WORK_CENTER_GROUP = /*$$(*/
          "Work center group cannot be null for prod process line to generate. Please correct on prod process line configurator id : %s." /*)*/;

  // Configurator Prod Product Service
  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_PRODUCT_FORMULA = /*$$(*/
      "The formula script to fill the product returns null value. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_PRODUCT = /*$$(*/
      "Product cannot be null for prod product to generate. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_QTY_FORMULA = /*$$(*/
      "The formula script to fill the qty returns null value. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_QTY = /*$$(*/
      "Qty cannot be null for prod product to generate. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_UNIT_FORMULA = /*$$(*/
      "The formula script to fill the unit returns null value. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_NULL_UNIT = /*$$(*/
      "Unit cannot be null for prod product to generate. Please correct on prod product configurator id : %s." /*)*/;

  public static final String CONFIGURATOR_PROD_PRODUCT_INCONSISTENT_CONDITION = /*$$(*/
      "The condition formula to generate the prod product returns null value or is not consistent. Please correct on prod product configurator id : %s." /*)*/;
  public static final String CONFIGURATOR_PROD_PROCESS_COULD_NOT_CAST_INTO_STOCK_LOCATION = /*$$(*/
      "The result formula of '%s' in configurator prod process '%s' could not be converted in a Stock location, please verify the formula." /*)*/;
  public static final String MANUF_ORDER_STOCK_MOVE_MISSING_OUTSOURCING_SOURCE_STOCK_LOCATION =
      /*$$(*/
      "The outsourcing receipt stock location is missing from the stock config" /*)*/;
  public static final String MANUF_ORDER_STOCK_MOVE_MISSING_SOURCE_STOCK_LOCATION =
      /*$$(*/
      "Stock location is missing from the prod process and in the component default stock location in stock configuration." /*)*/;
  public static final String MANUF_ORDER_STOCK_MOVE_MISSING_OUTSOURCING_DEST_STOCK_LOCATION =
      /*$$(*/
      "The produced product stock location is missing in the prod process" /*)*/;

  public static final String GENERATE_MANUF_ORDER_BOM_DIVIDE_ZERO =
      /*$$(*/
      "The quantity of the bill of material %s is equal to zero." /*)*/;

  public static final String GENERATE_MANUF_ORDER_BOM_MISSING_UNIT =
      /*$$(*/ "The unit of the bill of material %s is missing. Please correct it." /*)*/;

  public static final String MANUF_ORDER_MERGE_MISSING_UNIT =
      /*$$(*/ "You can not merge manufacturing orders without unit." /*)*/;

  public static final String PURCHASE_ORDER_NO_HOURS_UNIT = /*$$(*/
      "Outsourcing purchase order cannot be generated. Please fill hours unit in your base configuration." /*)*/;

  public static final String PURCHASE_ORDER_NO_END_UNIT = /*$$(*/
      "Impossible to generate outsourcing purchase order. Please fill unit on your product." /*)*/;

  public static final String NO_APPLICABLE_BILL_OF_MATERIALS = /*$$(*/
      "There is no applicable bill of materials for %s" /*)*/;

  public static final String NO_COMPANY_IN_BILL_OF_MATERIALS = /*$$(*/
      "The manuf order couldn't be generated for the product %s. Please fill a company on the linked BOM." /*)*/;

  public static final String LOOP_IN_BILL_OF_MATERIALS = /*$$(*/
      "The system found a loop in your bill of materials, please correct concerned data" /*)*/;
}
