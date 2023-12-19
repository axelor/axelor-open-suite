package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileSettingsResponse extends ResponseStructure {

  protected final List<MobileConfigResponse> apps;
  protected final Boolean isLoginUserQrcodeEnabled;
  protected final Boolean isTrackerMessageEnabled;
  protected final Boolean isInventoryValidationEnabled;
  protected final Boolean isStockCorrectionValidationEnabled;
  protected final Boolean isCustomerDeliveryLineAdditionEnabled;
  protected final Boolean isSupplierArrivalLineAdditionEnabled;
  protected final Boolean isVerifyCustomerDeliveryLineEnabled;
  protected final Boolean isVerifySupplierArrivalLineEnabled;
  protected final Boolean isVerifyInternalMoveLineEnabled;
  protected final Boolean isVerifyInventoryLineEnabled;
  protected final Boolean isMultiCurrencyEnabled;
  protected final Boolean isExpenseProjectInvoicingEnabled;
  protected final Boolean isKilometricExpenseLineAllowed;
  protected final Boolean isManualCreationOfExpenseAllowed;
  protected final Boolean isLineCreationOfExpenseDetailsAllowed;
  protected final Boolean isManualCreationOfTimesheetAllowed;
  protected final Boolean isLineCreationOfTimesheetDetailsAllowed;
  protected final Boolean isEditionOfDateAllowed;
  protected final Boolean isTimesheetProjectInvoicingEnabled;

  public MobileSettingsResponse(
      Integer version,
      List<MobileConfigResponse> apps,
      Boolean isLoginUserQrcodeEnabled,
      Boolean isTrackerMessageEnabled,
      Boolean isInventoryValidationEnabled,
      Boolean isStockCorrectionValidationEnabled,
      Boolean isCustomerDeliveryLineAdditionEnabled,
      Boolean isSupplierArrivalLineAdditionEnabled,
      Boolean isVerifyCustomerDeliveryLineEnabled,
      Boolean isVerifySupplierArrivalLineEnabled,
      Boolean isVerifyInternalMoveLineEnabled,
      Boolean isVerifyInventoryLineEnabled,
      Boolean isMultiCurrencyEnabled,
      Boolean isExpenseProjectInvoicingEnabled,
      Boolean isKilometricExpenseLineAllowed,
      Boolean isManualCreationOfExpenseAllowed,
      Boolean isLineCreationOfExpenseDetailsAllowed,
      Boolean isManualCreationOfTimesheetAllowed,
      Boolean isLineCreationOfTimesheetDetailsAllowed,
      Boolean isEditionOfDateAllowed,
      Boolean isTimesheetProjectInvoicingEnabled) {
    super(version);
    this.apps = apps;
    this.isLoginUserQrcodeEnabled = isLoginUserQrcodeEnabled;
    this.isTrackerMessageEnabled = isTrackerMessageEnabled;
    this.isInventoryValidationEnabled = isInventoryValidationEnabled;
    this.isStockCorrectionValidationEnabled = isStockCorrectionValidationEnabled;
    this.isCustomerDeliveryLineAdditionEnabled = isCustomerDeliveryLineAdditionEnabled;
    this.isSupplierArrivalLineAdditionEnabled = isSupplierArrivalLineAdditionEnabled;
    this.isVerifyCustomerDeliveryLineEnabled = isVerifyCustomerDeliveryLineEnabled;
    this.isVerifySupplierArrivalLineEnabled = isVerifySupplierArrivalLineEnabled;
    this.isVerifyInternalMoveLineEnabled = isVerifyInternalMoveLineEnabled;
    this.isVerifyInventoryLineEnabled = isVerifyInventoryLineEnabled;
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

  public List<MobileConfigResponse> getApps() {
    return apps;
  }

  public Boolean getLoginUserQrcodeEnabled() {
    return isLoginUserQrcodeEnabled;
  }

  public Boolean getTrackerMessageEnabled() {
    return isTrackerMessageEnabled;
  }

  public Boolean getInventoryValidationEnabled() {
    return isInventoryValidationEnabled;
  }

  public Boolean getStockCorrectionValidationEnabled() {
    return isStockCorrectionValidationEnabled;
  }

  public Boolean getCustomerDeliveryLineAdditionEnabled() {
    return isCustomerDeliveryLineAdditionEnabled;
  }

  public Boolean getSupplierArrivalLineAdditionEnabled() {
    return isSupplierArrivalLineAdditionEnabled;
  }

  public Boolean getVerifyCustomerDeliveryLineEnabled() {
    return isVerifyCustomerDeliveryLineEnabled;
  }

  public Boolean getVerifySupplierArrivalLineEnabled() {
    return isVerifySupplierArrivalLineEnabled;
  }

  public Boolean getVerifyInternalMoveLineEnabled() {
    return isVerifyInternalMoveLineEnabled;
  }

  public Boolean getVerifyInventoryLineEnabled() {
    return isVerifyInventoryLineEnabled;
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
