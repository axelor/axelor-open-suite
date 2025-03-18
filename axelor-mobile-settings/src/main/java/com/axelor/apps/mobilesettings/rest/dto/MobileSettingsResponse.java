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
package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.api.ResponseStructure;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  protected final Boolean isStockLocationManagementEnabled;
  protected final Boolean isOneLineShortcut;
  protected final String minimalRequiredMobileAppVersion;
  protected final List<String> fieldsToShowOnTimesheet;
  protected final List<Long> dashboardIdList;
  protected final List<MobileShortcutResponse> mobileShortcutList;
  protected final Boolean isGenericProductShown;
  protected final Boolean isConfiguratorProductShown;
  protected final List<String> productTypesToDisplay;
  protected final List<String> reportingTypesToDisplay;
  protected final MetaFile currentApkFile;
  protected final DMSFile defaultDmsRoot;
  protected final Boolean isFavoritesManagementEnabled;
  protected final Boolean isDownloadAllowed;
  protected final Boolean isRenamingAllowed;
  protected final Boolean isFolderCreationAllowed;
  protected final Boolean isFileCreationAllowed;
  protected final Boolean isFileDeletionAllowed;

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
      Boolean isTimesheetProjectInvoicingEnabled,
      Boolean isStockLocationManagementEnabled,
      Boolean isOneLineShortcut,
      String minimalRequiredMobileAppVersion,
      List<String> fieldsToShowOnTimesheet,
      List<Long> dashboardIdList,
      List<MobileShortcutResponse> mobileShortcutList,
      Boolean isGenericProductShown,
      Boolean isConfiguratorProductShown,
      List<String> productTypesToDisplay,
      List<String> reportingTypesToDisplay,
      MetaFile currentApkFile,
      DMSFile defaultDmsRoot,
      Boolean isFavoritesManagementEnabled,
      Boolean isDownloadAllowed,
      Boolean isRenamingAllowed,
      Boolean isFolderCreationAllowed,
      Boolean isFileCreationAllowed,
      Boolean isFileDeletionAllowed) {
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
    this.isStockLocationManagementEnabled = isStockLocationManagementEnabled;
    this.isOneLineShortcut = isOneLineShortcut;
    this.minimalRequiredMobileAppVersion = minimalRequiredMobileAppVersion;
    this.fieldsToShowOnTimesheet = fieldsToShowOnTimesheet;
    this.dashboardIdList = dashboardIdList;
    this.mobileShortcutList = mobileShortcutList;
    this.isGenericProductShown = isGenericProductShown;
    this.isConfiguratorProductShown = isConfiguratorProductShown;
    this.productTypesToDisplay = productTypesToDisplay;
    this.reportingTypesToDisplay = reportingTypesToDisplay;
    this.currentApkFile = currentApkFile;
    this.defaultDmsRoot = defaultDmsRoot;
    this.isFavoritesManagementEnabled = isFavoritesManagementEnabled;
    this.isDownloadAllowed = isDownloadAllowed;
    this.isRenamingAllowed = isRenamingAllowed;
    this.isFolderCreationAllowed = isFolderCreationAllowed;
    this.isFileCreationAllowed = isFileCreationAllowed;
    this.isFileDeletionAllowed = isFileDeletionAllowed;
  }

  public List<MobileConfigResponse> getApps() {
    return apps;
  }

  @JsonProperty(value = "isLoginUserQrcodeEnabled")
  public Boolean getLoginUserQrcodeEnabled() {
    return isLoginUserQrcodeEnabled;
  }

  @JsonProperty(value = "isTrackerMessageEnabled")
  public Boolean getTrackerMessageEnabled() {
    return isTrackerMessageEnabled;
  }

  @JsonProperty(value = "isInventoryValidationEnabled")
  public Boolean getInventoryValidationEnabled() {
    return isInventoryValidationEnabled;
  }

  @JsonProperty(value = "isStockCorrectionValidationEnabled")
  public Boolean getStockCorrectionValidationEnabled() {
    return isStockCorrectionValidationEnabled;
  }

  @JsonProperty(value = "isCustomerDeliveryLineAdditionEnabled")
  public Boolean getCustomerDeliveryLineAdditionEnabled() {
    return isCustomerDeliveryLineAdditionEnabled;
  }

  @JsonProperty(value = "isSupplierArrivalLineAdditionEnabled")
  public Boolean getSupplierArrivalLineAdditionEnabled() {
    return isSupplierArrivalLineAdditionEnabled;
  }

  @JsonProperty(value = "isVerifyCustomerDeliveryLineEnabled")
  public Boolean getVerifyCustomerDeliveryLineEnabled() {
    return isVerifyCustomerDeliveryLineEnabled;
  }

  @JsonProperty(value = "isVerifySupplierArrivalLineEnabled")
  public Boolean getVerifySupplierArrivalLineEnabled() {
    return isVerifySupplierArrivalLineEnabled;
  }

  @JsonProperty(value = "isVerifyInternalMoveLineEnabled")
  public Boolean getVerifyInternalMoveLineEnabled() {
    return isVerifyInternalMoveLineEnabled;
  }

  @JsonProperty(value = "isVerifyInventoryLineEnabled")
  public Boolean getVerifyInventoryLineEnabled() {
    return isVerifyInventoryLineEnabled;
  }

  @JsonProperty(value = "isMultiCurrencyEnabled")
  public Boolean getMultiCurrencyEnabled() {
    return isMultiCurrencyEnabled;
  }

  @JsonProperty(value = "isExpenseProjectInvoicingEnabled")
  public Boolean getExpenseProjectInvoicingEnabled() {
    return isExpenseProjectInvoicingEnabled;
  }

  @JsonProperty(value = "isKilometricExpenseLineAllowed")
  public Boolean getKilometricExpenseLineAllowed() {
    return isKilometricExpenseLineAllowed;
  }

  @JsonProperty(value = "isManualCreationOfExpenseAllowed")
  public Boolean getManualCreationOfExpenseAllowed() {
    return isManualCreationOfExpenseAllowed;
  }

  @JsonProperty(value = "isLineCreationOfExpenseDetailsAllowed")
  public Boolean getLineCreationOfExpenseDetailsAllowed() {
    return isLineCreationOfExpenseDetailsAllowed;
  }

  @JsonProperty(value = "isManualCreationOfTimesheetAllowed")
  public Boolean getManualCreationOfTimesheetAllowed() {
    return isManualCreationOfTimesheetAllowed;
  }

  @JsonProperty(value = "isLineCreationOfTimesheetDetailsAllowed")
  public Boolean getLineCreationOfTimesheetDetailsAllowed() {
    return isLineCreationOfTimesheetDetailsAllowed;
  }

  @JsonProperty(value = "isEditionOfDateAllowed")
  public Boolean getEditionOfDateAllowed() {
    return isEditionOfDateAllowed;
  }

  @JsonProperty(value = "isTimesheetProjectInvoicingEnabled")
  public Boolean getTimesheetProjectInvoicingEnabled() {
    return isTimesheetProjectInvoicingEnabled;
  }

  @JsonProperty(value = "isStockLocationManagementEnabled")
  public Boolean getStockLocationManagementEnabled() {
    return isStockLocationManagementEnabled;
  }

  @JsonProperty(value = "isOneLineShortcut")
  public Boolean getIsOneLineShortcut() {
    return isOneLineShortcut;
  }

  @JsonProperty(value = "minimalRequiredMobileAppVersion")
  public String getMinimalRequiredMobileAppVersion() {
    return minimalRequiredMobileAppVersion;
  }

  @JsonProperty(value = "fieldsToShowOnTimesheet")
  public List<String> getFieldsToShowOnTimesheet() {
    return fieldsToShowOnTimesheet;
  }

  public List<Long> getDashboardIdList() {
    return dashboardIdList;
  }

  public List<MobileShortcutResponse> getMobileShortcutList() {
    return mobileShortcutList;
  }

  @JsonProperty(value = "isGenericProductShown")
  public Boolean getGenericProductShown() {
    return isGenericProductShown;
  }

  @JsonProperty(value = "isConfiguratorProductShown")
  public Boolean getConfiguratorProductShown() {
    return isConfiguratorProductShown;
  }

  @JsonProperty(value = "productTypesToDisplay")
  public List<String> getProductTypesToDisplay() {
    return productTypesToDisplay;
  }

  @JsonProperty(value = "reportingTypesToDisplay")
  public List<String> getReportingTypesToDisplay() {
    return reportingTypesToDisplay;
  }

  @JsonProperty(value = "currentApkFile")
  public MetaFile getCurrentApkFile() {
    return currentApkFile;
  }

  @JsonProperty(value = "defaultDmsRoot")
  public DMSFile getDefaultDmsRoot() {
    return defaultDmsRoot;
  }

  @JsonProperty(value = "isFavoritesManagementEnabled")
  public Boolean getIsFavoritesManagementEnabled() {
    return isFavoritesManagementEnabled;
  }

  @JsonProperty(value = "isDownloadAllowed")
  public Boolean getIsDownloadAllowed() {
    return isDownloadAllowed;
  }

  @JsonProperty(value = "isRenamingAllowed")
  public Boolean getIsRenamingAllowed() {
    return isRenamingAllowed;
  }

  @JsonProperty(value = "isFolderCreationAllowed")
  public Boolean getIsFolderCreationAllowed() {
    return isFolderCreationAllowed;
  }

  @JsonProperty(value = "isFileCreationAllowed")
  public Boolean getIsFileCreationAllowed() {
    return isFileCreationAllowed;
  }

  @JsonProperty(value = "isFileDeletionAllowed")
  public Boolean getIsFileDeletionAllowed() {
    return isFileDeletionAllowed;
  }
}
