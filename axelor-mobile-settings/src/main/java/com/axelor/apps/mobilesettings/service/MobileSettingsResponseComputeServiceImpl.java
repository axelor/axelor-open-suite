package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileHRSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileStockSettingsResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_STOCK)
                .getAuthorizedRoles());
    Boolean isHRAppEnabled =
        checkConfigWithRoles(
            appMobileSettings.getIsHRAppEnabled(),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HR)
                .getAuthorizedRoles());

    return new MobileSettingsResponse(
        appMobileSettings.getVersion(),
        isStockAppEnabled,
        checkConfigWithRoles(
            appMobileSettings.getIsProductionAppEnabled(),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)
                .getAuthorizedRoles()),
        checkConfigWithRoles(
            appMobileSettings.getIsCrmAppEnabled(),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_CRM)
                .getAuthorizedRoles()),
        checkConfigWithRoles(
            appMobileSettings.getIsHelpdeskAppEnabled(),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HELPDESK)
                .getAuthorizedRoles()),
        isHRAppEnabled,
        appMobileSettings.getIsLoginUserQrcodeEnabled(),
        appMobileSettings.getIsTrackerMessageEnabled(),
        getStockSettings(appMobileSettings, isStockAppEnabled),
        getHrSettings(appMobileSettings, isHRAppEnabled),
        getRestrictedMenus());
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

  protected Boolean checkRestrictedMenuWithRoles(Set<Role> authorizedRoles) {
    if (authorizedRoles == null || authorizedRoles.isEmpty()) {
      return true;
    }
    return authorizedRoles.stream().noneMatch(AuthUtils.getUser().getRoles()::contains);
  }

  protected MobileConfig getMobileConfigFromAppSequence(String appSequence) {
    return mobileConfigRepository.all().filter("self.sequence = '" + appSequence + "'").fetchOne();
  }

  protected List<String> getRestrictedMenus() {
    List<String> appSequenceList =
        List.of(
            MobileConfigRepository.APP_SEQUENCE_STOCK,
            MobileConfigRepository.APP_SEQUENCE_MANUFACTURING,
            MobileConfigRepository.APP_SEQUENCE_CRM,
            MobileConfigRepository.APP_SEQUENCE_HELPDESK,
            MobileConfigRepository.APP_SEQUENCE_HR);
    return appSequenceList.stream()
        .map(this::getRestrictedMenusFromApp)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<String> getRestrictedMenusFromApp(String appSequence) {
    MobileConfig mobileConfig = getMobileConfigFromAppSequence(appSequence);
    if (mobileConfig.getIsCustomizeMenuEnabled()) {
      return mobileConfig.getMenus().stream()
          .filter(mobileMenu -> checkRestrictedMenuWithRoles(mobileMenu.getAuthorizedRoles()))
          .map(MobileMenu::getTechnicalName)
          .collect(Collectors.toList());
    }
    return List.of();
  }
}
