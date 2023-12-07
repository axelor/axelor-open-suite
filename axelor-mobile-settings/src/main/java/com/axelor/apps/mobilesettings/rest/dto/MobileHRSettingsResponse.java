package com.axelor.apps.mobilesettings.rest.dto;

public class MobileHRSettingsResponse {
  protected Boolean isMultiCurrencyEnabled;
  protected Boolean isExpenseProjectInvoicingEnabled;
  protected Boolean isKilometricExpenseLineAllowed;
  protected Boolean isManualCreationOfExpenseAllowed;
  protected Boolean isLineCreationOfExpenseDetailsAllowed;
  protected Boolean isManualCreationOfTimesheetAllowed;
  protected Boolean isLineCreationOfTimesheetDetailsAllowed;
  protected Boolean isEditionOfDateAllowed;
  protected Boolean isTimesheetProjectInvoicingEnabled;

  public MobileHRSettingsResponse(
      Boolean isMultiCurrencyEnabled,
      Boolean isExpenseProjectInvoicingEnabled,
      Boolean isKilometricExpenseLineAllowed,
      Boolean isManualCreationOfExpenseAllowed,
      Boolean isLineCreationOfExpenseDetailsAllowed,
      Boolean isManualCreationOfTimesheetAllowed,
      Boolean isLineCreationOfTimesheetDetailsAllowed,
      Boolean isEditionOfDateAllowed,
      Boolean isTimesheetProjectInvoicingEnabled) {
    this.isMultiCurrencyEnabled = isMultiCurrencyEnabled;
    this.isExpenseProjectInvoicingEnabled = isExpenseProjectInvoicingEnabled;
    this.isKilometricExpenseLineAllowed = isKilometricExpenseLineAllowed;
    this.isManualCreationOfExpenseAllowed = isManualCreationOfExpenseAllowed;
    this.isLineCreationOfExpenseDetailsAllowed = isLineCreationOfExpenseDetailsAllowed;
    this.isManualCreationOfTimesheetAllowed = isManualCreationOfTimesheetAllowed;
    this.isLineCreationOfTimesheetDetailsAllowed = isLineCreationOfTimesheetDetailsAllowed;
    this.isEditionOfDateAllowed = isEditionOfDateAllowed;
    this.isTimesheetProjectInvoicingEnabled = isTimesheetProjectInvoicingEnabled;
  }

  public Boolean getMultiCurrencyEnabled() {
    return isMultiCurrencyEnabled;
  }

  public Boolean getExpenseProjectInvoicingEnabled() {
    return isExpenseProjectInvoicingEnabled;
  }

  public Boolean getKilometricExpenseLineAllowed() {
    return isKilometricExpenseLineAllowed;
  }

  public Boolean getManualCreationOfExpenseAllowed() {
    return isManualCreationOfExpenseAllowed;
  }

  public Boolean getLineCreationOfExpenseDetailsAllowed() {
    return isLineCreationOfExpenseDetailsAllowed;
  }

  public Boolean getManualCreationOfTimesheetAllowed() {
    return isManualCreationOfTimesheetAllowed;
  }

  public Boolean getLineCreationOfTimesheetDetailsAllowed() {
    return isLineCreationOfTimesheetDetailsAllowed;
  }

  public Boolean getEditionOfDateAllowed() {
    return isEditionOfDateAllowed;
  }

  public Boolean getTimesheetProjectInvoicingEnabled() {
    return isTimesheetProjectInvoicingEnabled;
  }
}
