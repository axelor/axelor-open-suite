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
package com.axelor.apps.base.quickmenu;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.web.UserController;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.axelor.ui.QuickMenu;
import com.axelor.ui.QuickMenuCreator;
import com.axelor.ui.QuickMenuItem;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ActiveCompanyUpdateQuickMenuCreator implements QuickMenuCreator {

  protected AppBaseService appBaseService;
  protected CompanyRepository companyRepository;

  @Inject
  public ActiveCompanyUpdateQuickMenuCreator(
      AppBaseService appBaseService, CompanyRepository companyRepository) {
    this.appBaseService = appBaseService;
    this.companyRepository = companyRepository;
  }

  @Override
  public QuickMenu create() {

    if (hasConfigEnabled()) {
      return null;
    }

    return new QuickMenu(
        Optional.ofNullable(AuthUtils.getUser())
            .map(User::getActiveCompany)
            .map(Company::getName)
            .orElse(I18n.get("Active company")),
        1,
        true,
        getItems());
  }

  protected boolean hasConfigEnabled() {
    AppBase appBase = appBaseService.getAppBase();
    return !appBase.getEnableMultiCompany()
        || StringUtils.isBlank(appBase.getShortcutMultiSelect())
        || !appBase.getShortcutMultiSelect().contains(AppBaseRepository.SHORTCUT_ACTIVE_COMPANY);
  }

  protected List<QuickMenuItem> getItems() {
    User user = AuthUtils.getUser();
    Set<Company> companies = user.getCompanySet();
    List<QuickMenuItem> items = new ArrayList<>();

    if (companies == null || companies.size() <= 1) {
      return items;
    }

    Company activeCompany = user.getActiveCompany();
    String action = UserController.class.getName() + ":" + "setActiveCompany";

    for (Company company : companies) {
      QuickMenuItem item =
          new QuickMenuItem(
              company.getName(),
              action,
              new Context(company.getId(), Company.class),
              company.equals(activeCompany));
      items.add(item);
    }
    return items;
  }
}
