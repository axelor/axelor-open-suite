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
package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileShortcut;
import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileConfigResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileMenuResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileShortcutResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppMobileSettings;
import com.axelor.studio.db.repo.AppMobileSettingsRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

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

    return new MobileSettingsResponse(
        appMobileSettings.getVersion(),
        getApps(appMobileSettings),
        appMobileSettings.getIsLoginUserQrcodeEnabled(),
        appMobileSettings.getIsTrackerMessageEnabled(),
        appMobileSettings.getIsInboxAccessEnabled(),
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
            appMobileSettings.getVerifyInventoryLineRoleSet()),
        appMobileSettings.getIsMultiCurrencyEnabled(),
        appMobileSettings.getIsExpenseProjectInvoicingEnabled(),
        appMobileSettings.getIsKilometricExpenseLineAllowed(),
        appMobileSettings.getIsManualCreationOfExpenseAllowed(),
        appMobileSettings.getIsLineCreationOfExpenseDetailsAllowed(),
        appMobileSettings.getIsManualCreationOfTimesheetAllowed(),
        appMobileSettings.getIsLineCreationOfTimesheetDetailsAllowed(),
        appMobileSettings.getIsEditionOfDateAllowed(),
        appMobileSettings.getIsTimesheetProjectInvoicingEnabled(),
        appMobileSettings.getIsStockLocationManagementEnabled(),
        appMobileSettings.getIsOneLineShortcut(),
        appMobileSettings.getMinimalRequiredMobileAppVersion(),
        getFieldsToShowOnTimesheet(appMobileSettings.getFieldsToShowOnTimesheet()),
        getAuthorizedDashboardIdList(appMobileSettings),
        getAuthorizedShortcutList(appMobileSettings),
        appMobileSettings.getIsGenericProductShown(),
        appMobileSettings.getIsConfiguratorProductShown(),
        getProductTypesToDisplay(appMobileSettings),
        getReportingTypesToDisplay(appMobileSettings),
        appMobileSettings.getCurrentApkFile(),
        appMobileSettings.getDefaultDmsRoot(),
        appMobileSettings.getIsFavoritesManagementEnabled(),
        appMobileSettings.getIsDownloadAllowed(),
        appMobileSettings.getIsRenamingAllowed(),
        appMobileSettings.getIsFolderCreationAllowed(),
        appMobileSettings.getIsFileCreationAllowed(),
        appMobileSettings.getIsFileDeletionAllowed());
  }

  protected List<Long> getAuthorizedDashboardIdList(AppMobileSettings appMobileSettings) {
    List<MobileDashboard> mobileDashboardList = appMobileSettings.getMobileDashboardList();
    if (CollectionUtils.isEmpty(mobileDashboardList)) {
      return Collections.emptyList();
    }
    return mobileDashboardList.stream()
        .filter(
            dashboard ->
                UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
                        AuthUtils.getUser(), dashboard.getAuthorizedRoleSet())
                    || CollectionUtils.isEmpty(dashboard.getAuthorizedRoleSet()))
        .map(MobileDashboard::getId)
        .collect(Collectors.toList());
  }

  protected List<MobileShortcutResponse> getAuthorizedShortcutList(
      AppMobileSettings appMobileSettings) {
    List<MobileShortcut> mobileShortcutList = appMobileSettings.getMobileShortcutList();
    if (CollectionUtils.isEmpty(mobileShortcutList)) {
      return Collections.emptyList();
    }

    List<MobileShortcut> authorizedMobileShortcutList =
        mobileShortcutList.stream()
            .filter(
                shortcut ->
                    UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
                            AuthUtils.getUser(), shortcut.getAuthorizedRoleSet())
                        || CollectionUtils.isEmpty(shortcut.getAuthorizedRoleSet()))
            .collect(Collectors.toList());
    List<MobileShortcutResponse> mobileShortcutResponseList = new ArrayList<>();
    for (MobileShortcut mobileShortcut : authorizedMobileShortcutList) {
      mobileShortcutResponseList.add(new MobileShortcutResponse(mobileShortcut));
    }
    return mobileShortcutResponseList;
  }

  protected Boolean checkConfigWithRoles(Boolean config, Set<Role> authorizedRoles) {
    if (!config) {
      return false;
    }
    User user = AuthUtils.getUser();
    return UserRoleToolService.checkUserRolesPermissionIncludingEmpty(user, authorizedRoles);
  }

  protected MobileConfig getMobileConfigFromAppSequence(String appSequence) {
    return mobileConfigRepository.all().filter("self.sequence = '" + appSequence + "'").fetchOne();
  }

  protected List<MobileConfigResponse> getApps(AppMobileSettings appMobileSettings) {
    return List.of(
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_STOCK,
            checkConfigWithRoles(
                appMobileSettings.getIsStockAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_STOCK)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_STOCK)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_STOCK)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_MANUFACTURING,
            checkConfigWithRoles(
                appMobileSettings.getIsProductionAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_CRM,
            checkConfigWithRoles(
                appMobileSettings.getIsCrmAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_CRM)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_CRM)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_CRM)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_HELPDESK,
            checkConfigWithRoles(
                appMobileSettings.getIsHelpdeskAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HELPDESK)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HELPDESK)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_HELPDESK)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_HR,
            checkConfigWithRoles(
                appMobileSettings.getIsHRAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HR)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HR)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_HR)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_QUALITY,
            checkConfigWithRoles(
                appMobileSettings.getIsQualityAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_QUALITY)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_QUALITY)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_QUALITY)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_INTERVENTION,
            checkConfigWithRoles(
                appMobileSettings.getIsInterventionAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_INTERVENTION)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_INTERVENTION)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_INTERVENTION)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_SALE,
            checkConfigWithRoles(
                appMobileSettings.getIsSaleAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_SALE)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_SALE)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_SALE)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_PROJECT,
            checkConfigWithRoles(
                appMobileSettings.getIsProjectAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_PROJECT)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_PROJECT)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_PROJECT)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_DMS,
            checkConfigWithRoles(
                appMobileSettings.getIsDMSAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_DMS)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_DMS)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_DMS)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_PURCHASE,
            checkConfigWithRoles(
                appMobileSettings.getIsPurchaseAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_PURCHASE)
                    .getAuthorizedRoles()),
            getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_PURCHASE)
                .getIsCustomizeMenuEnabled(),
            getAccessibleMenusFromApp(MobileConfigRepository.APP_SEQUENCE_PURCHASE)));
  }

  protected List<MobileMenuResponse> getAccessibleMenusFromApp(String appSequence) {
    MobileConfig mobileConfig = getMobileConfigFromAppSequence(appSequence);
    if (mobileConfig.getIsCustomizeMenuEnabled()) {
      return mobileConfig.getMenuList().stream()
          .filter(
              mobileMenu ->
                  mobileMenu.getAuthorizedRoles().isEmpty()
                      || UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
                          AuthUtils.getUser(), mobileMenu.getAuthorizedRoles()))
          .map(
              mobileMenu ->
                  new MobileMenuResponse(
                      mobileMenu.getName(),
                      mobileMenu.getTechnicalName(),
                      mobileMenu.getMenuOrder()))
          .collect(Collectors.toList());
    }
    return List.of();
  }

  protected List<String> getFieldsToShowOnTimesheet(String timesheetImputationSelect) {
    return Optional.ofNullable(timesheetImputationSelect)
        .map(it -> it.split(","))
        .map(List::of)
        .orElse(
            List.of(
                AppMobileSettingsRepository.IMPUTATION_ON_PROJECT,
                AppMobileSettingsRepository.IMPUTATION_ON_PROJECT_TASK,
                AppMobileSettingsRepository.IMPUTATION_ON_MANUF_ORDER,
                AppMobileSettingsRepository.IMPUTATION_ON_OPERATION_ORDER,
                AppMobileSettingsRepository.IMPUTATION_ON_ACTIVITY));
  }

  protected List<String> getProductTypesToDisplay(AppMobileSettings appMobileSettings) {
    String productTypesToDisplay = appMobileSettings.getProductTypesToDisplaySelect();
    if (StringUtils.isEmpty(productTypesToDisplay)) {
      return List.of(
          ProductRepository.PRODUCT_TYPE_STORABLE, ProductRepository.PRODUCT_TYPE_SERVICE);
    }
    return Arrays.stream(productTypesToDisplay.split(",")).collect(Collectors.toList());
  }

  protected List<String> getReportingTypesToDisplay(AppMobileSettings appMobileSettings) {
    String reportingTypesToDisplay = appMobileSettings.getReportingTypesToDisplaySelect();
    if (StringUtils.isEmpty(reportingTypesToDisplay)) {
      return List.of(
          AppMobileSettingsRepository.REPORTING_TYPE_DISPLAY_INDICATORS,
          AppMobileSettingsRepository.REPORTING_TYPE_DISPLAY_ACTIVITIES,
          AppMobileSettingsRepository.REPORTING_TYPE_DISPLAY_NONE);
    }
    return Arrays.stream(reportingTypesToDisplay.split(",")).collect(Collectors.toList());
  }
}
