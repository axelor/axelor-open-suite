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

import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.MobileShortcut;
import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.apps.mobilesettings.rest.dto.MobileConfigResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileSettingsResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileShortcutResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppMobileSettings;
import com.axelor.studio.db.repo.AppMobileSettingsRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
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
        getAuthorizedShortcutList(appMobileSettings));
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
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_STOCK)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_MANUFACTURING,
            checkConfigWithRoles(
                appMobileSettings.getIsProductionAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_MANUFACTURING)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_CRM,
            checkConfigWithRoles(
                appMobileSettings.getIsCrmAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_CRM)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_CRM)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_HELPDESK,
            checkConfigWithRoles(
                appMobileSettings.getIsHelpdeskAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HELPDESK)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_HELPDESK)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_HR,
            checkConfigWithRoles(
                appMobileSettings.getIsHRAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_HR)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_HR)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_QUALITY,
            checkConfigWithRoles(
                appMobileSettings.getIsQualityAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_QUALITY)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_QUALITY)),
        new MobileConfigResponse(
            MobileConfigRepository.APP_SEQUENCE_INTERVENTION,
            checkConfigWithRoles(
                appMobileSettings.getIsInterventionAppEnabled(),
                getMobileConfigFromAppSequence(MobileConfigRepository.APP_SEQUENCE_INTERVENTION)
                    .getAuthorizedRoles()),
            getRestrictedMenusFromApp(MobileConfigRepository.APP_SEQUENCE_INTERVENTION)));
  }

  protected List<String> getRestrictedMenusFromApp(String appSequence) {
    MobileConfig mobileConfig = getMobileConfigFromAppSequence(appSequence);
    if (mobileConfig.getIsCustomizeMenuEnabled()) {
      return mobileConfig.getMenus().stream()
          .filter(
              mobileMenu ->
                  !UserRoleToolService.checkUserRolesPermissionExcludingEmpty(
                      AuthUtils.getUser(), mobileMenu.getAuthorizedRoles()))
          .map(MobileMenu::getTechnicalName)
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
}
