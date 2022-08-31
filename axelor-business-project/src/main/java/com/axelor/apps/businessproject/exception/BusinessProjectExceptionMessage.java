package com.axelor.apps.businessproject.exception;

public final class BusinessProjectExceptionMessage {

  private BusinessProjectExceptionMessage() {}

  public static final String FOLDER_TEMPLATE = /*$$(*/ "You must add a sale order template" /*)*/;
  public static final String INVOICING_PROJECT_EMPTY = /*$$(*/
      "You haven't select any element to invoice" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT = /*$$(*/
      "You must select a project/task" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_PARTNER = /*$$(*/
      "There is no customer for this project/task" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_PRODUCT = /*$$(*/
      "You haven't select a product to invoice for the task %s" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_COMPANY = /*$$(*/
      "You haven't select a company on the main project" /*)*/;
  public static final String SALE_ORDER_NO_PROJECT = /*$$(*/ "No Project selected" /*)*/;
  public static final String SALE_ORDER_NO_LINES = /*$$(*/ "No Line can be used for tasks" /*)*/;
  public static final String SALE_ORDER_NO_TYPE_GEN_PROJECT = /*$$(*/
      "No type of generation project has been selected" /*)*/;
  public static final String SALE_ORDER_BUSINESS_PROJECT = /*$$(*/
      "The project is configured to be alone" /*)*/;
  public static final String JOB_COSTING_APP = /*$$(*/ "Job costing" /*)*/;
  public static final String FACTORY_NO_FOUND = /*$$(*/
      "Factory not found this type of generator" /*)*/;
  public static final String FACTORY_FILL_WITH_PROJECT_ALONE = /*$$(*/
      "You can't fill a project with the strategy Project Alone." /*)*/;

  public static final String NO_PROJECT_IN_CONTEXT = /*$$(*/ "No project found in context" /*)*/;
  public static final String LINES_NOT_SELECTED = /*$$(*/ "Please select lines" /*)*/;

  public static final String SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_1 = /*$$(*/
      "Products must be Service type and Method of Supply Produce." /*)*/;
  public static final String SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2 = /*$$(*/
      "Please complete the order lines with at least one product type 'Service' and the supply mode 'Produce'" /*)*/;

  public static final String INVALID_EXCLUDE_TASK_FILTER = /*$$(*/
      "Invalid exclude task for invoicing filter" /*)*/;

  public static final String BATCH_TASK_UPDATION_1 = /*$$(*/ "Task %s" /*)*/;

  public static final String BATCH_TASK_UPDATION_2 = /*$$(*/ "Tasks update completed: " /*)*/;

  public static final String BATCH_TIMESHEETLINE_UPDATION_1 = /*$$(*/ "Timesheet line %s" /*)*/;

  public static final String BATCH_INVOICING_PROJECT_1 = /*$$(*/ "Project %s" /*)*/;

  public static final String BATCH_INVOICING_PROJECT_2 = /*$$(*/
      "Generated invoicing project" /*)*/;

  public static final String PROJECT_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Project" /*)*/;
}
