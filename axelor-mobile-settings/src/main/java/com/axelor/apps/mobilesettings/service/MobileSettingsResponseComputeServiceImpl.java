package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileHRSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileStockSettingsResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import java.util.Set;

public class MobileSettingsResponseComputeServiceImpl
    implements MobileSettingsResponseComputeService {
  protected AppMobileSettingsService appMobileSettingsService;
  protected MobileConfigRepository mobileConfigRepository;

  @Inject
  public MobileSettingsResponseComputeServiceImpl(
      AppMobileSettingsService appMobileSettingsService,
      MobileConfigRepository mobileConfigRepository) {
    this.appMobileSettingsService = appMobileSettingsService;
    this.mobileConfigRepository = mobileConfigRepository;
  }

  @Override
  public MobileSettingsResponse computeMobileSettingsResponse() {
    AppMobileSettings appMobileSettings = appMobileSettingsService.getAppMobileSettings();
    Boolean isStockAppEnabled =
        checkConfigWithRoles(
            appMobileSettings.getIsStockAppEnabled(),
            getMobileConfigAuthorizedRoles(MobileConfigRepository.APP_SEQUENCE_STOCK));
    Boolean isHRAppEnabled =
        checkConfigWithRoles(
            appMobileSettings.getIsHRAppEnabled(),
            getMobileConfigAuthorizedRoles(MobileConfigRepository.APP_SEQUENCE_HR));

    return new MobileSettingsResponse(
        appMobileSettings.getVersion(),
        isStockAppEnabled,
        checkConfigWithRoles(
            appMobileSettings.getIsProductionAppEnabled(),
            getMobileConfigAuthorizedRoles(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)),
        checkConfigWithRoles(
            appMobileSettings.getIsCrmAppEnabled(),
            getMobileConfigAuthorizedRoles(MobileConfigRepository.APP_SEQUENCE_CRM)),
        checkConfigWithRoles(
            appMobileSettings.getIsHelpdeskAppEnabled(),
            getMobileConfigAuthorizedRoles(MobileConfigRepository.APP_SEQUENCE_HELPDESK)),
        isHRAppEnabled,
        appMobileSettings.getIsLoginUserQrcodeEnabled(),
        appMobileSettings.getIsTrackerMessageEnabled(),
        getStockSettings(appMobileSettings, isStockAppEnabled),
        getHrSettings(appMobileSettings, isHRAppEnabled));
  }

  protected MobileStockSettingsResponse getStockSettings(
      AppMobileSettings appMobileSettings, Boolean isStockAppEnabled) {
    if (!isStockAppEnabled) {
      return null;
    }
    return new MobileStockSettingsResponse(
        checkConfigWithRoles(
            appMobileSettings.getIsInventoryValidationEnabled(),
            appMobileSettings.getInventoryValidationRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsStockCorrectionValidationEnabled(),
            appMobileSettings.getStockCorrectionValidationRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsCustomerDeliveryLineAdditionEnabled(),
            appMobileSettings.getCustomerDeliveryLineAdditionRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsSupplierArrivalLineAdditionEnabled(),
            appMobileSettings.getSupplierArrivalLineAdditionRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsVerifyCustomerDeliveryLineEnabled(),
            appMobileSettings.getVerifyCustomerDeliveryLineRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsVerifySupplierArrivalLineEnabled(),
            appMobileSettings.getVerifySupplierArrivalLineRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsVerifyInternalMoveLineEnabled(),
            appMobileSettings.getVerifyInternalMoveLineRoleSet()),
        checkConfigWithRoles(
            appMobileSettings.getIsVerifyInventoryLineEnabled(),
            appMobileSettings.getVerifyInventoryLineRoleSet()));
  }

  protected MobileHRSettingsResponse getHrSettings(
      AppMobileSettings appMobileSettings, Boolean isHRAppEnabled) {
    if (!isHRAppEnabled) {
      return null;
    }
    return new MobileHRSettingsResponse(
        appMobileSettings.getIsMultiCurrencyEnabled(),
        appMobileSettings.getIsExpenseProjectInvoicingEnabled(),
        appMobileSettings.getIsKilometricExpenseLineAllowed(),
        appMobileSettings.getIsManualCreationOfExpenseAllowed(),
        appMobileSettings.getIsLineCreationOfExpenseDetailsAllowed(),
        appMobileSettings.getIsManualCreationOfTimesheetAllowed(),
        appMobileSettings.getIsLineCreationOfTimesheetDetailsAllowed(),
        appMobileSettings.getIsEditionOfDateAllowed(),
        appMobileSettings.getIsTimesheetProjectInvoicingEnabled());
  }

  protected Boolean checkConfigWithRoles(Boolean config, Set<Role> authorizedRoles) {
    if (!config) {
      return false;
    }
    if (authorizedRoles == null || authorizedRoles.isEmpty()) {
      return true;
    }
    return authorizedRoles.stream().anyMatch(AuthUtils.getUser().getRoles()::contains);
  }

  protected Set<Role> getMobileConfigAuthorizedRoles(String appSequence) {
    return mobileConfigRepository
        .all()
        .filter("self.sequence = '" + appSequence + "'")
        .fetchOne()
        .getAuthorizedRoles();
  }
}
