package com.axelor.apps.mobilesettings.rest.dto;

public class MobileStockSettingsResponse {
  protected Boolean isInventoryValidationEnabled;
  protected Boolean isStockCorrectionValidationEnabled;
  protected Boolean isCustomerDeliveryLineAdditionEnabled;
  protected Boolean isSupplierArrivalLineAdditionEnabled;
  protected Boolean isVerifyCustomerDeliveryLineEnabled;
  protected Boolean isVerifySupplierArrivalLineEnabled;
  protected Boolean isVerifyInternalMoveLineEnabled;
  protected Boolean isVerifyInventoryLineEnabled;

  public MobileStockSettingsResponse(
      Boolean isInventoryValidationEnabled,
      Boolean isStockCorrectionValidationEnabled,
      Boolean isCustomerDeliveryLineAdditionEnabled,
      Boolean isSupplierArrivalLineAdditionEnabled,
      Boolean isVerifyCustomerDeliveryLineEnabled,
      Boolean isVerifySupplierArrivalLineEnabled,
      Boolean isVerifyInternalMoveLineEnabled,
      Boolean isVerifyInventoryLineEnabled) {
    this.isInventoryValidationEnabled = isInventoryValidationEnabled;
    this.isStockCorrectionValidationEnabled = isStockCorrectionValidationEnabled;
    this.isCustomerDeliveryLineAdditionEnabled = isCustomerDeliveryLineAdditionEnabled;
    this.isSupplierArrivalLineAdditionEnabled = isSupplierArrivalLineAdditionEnabled;
    this.isVerifyCustomerDeliveryLineEnabled = isVerifyCustomerDeliveryLineEnabled;
    this.isVerifySupplierArrivalLineEnabled = isVerifySupplierArrivalLineEnabled;
    this.isVerifyInternalMoveLineEnabled = isVerifyInternalMoveLineEnabled;
    this.isVerifyInventoryLineEnabled = isVerifyInventoryLineEnabled;
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
}
