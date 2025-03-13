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
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.web.UserController;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
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

public class TradingNameUpdateQuickMenuCreator implements QuickMenuCreator {

  protected AppBaseService appBaseService;
  protected UserService userService;

  @Inject
  public TradingNameUpdateQuickMenuCreator(AppBaseService appBaseService, UserService userService) {
    this.appBaseService = appBaseService;
    this.userService = userService;
  }

  @Override
  public QuickMenu create() {

    if (hasConfigEnabled()) {
      return null;
    }

    return new QuickMenu(
        Optional.ofNullable(userService.getTradingName())
            .map(TradingName::getName)
            .filter(StringUtils::notEmpty)
            .orElse(I18n.get("Active trading name")),
        2,
        true,
        getItems());
  }

  protected boolean hasConfigEnabled() {
    AppBase appBase = appBaseService.getAppBase();
    return !appBase.getEnableTradingNamesManagement()
        || StringUtils.isBlank(appBase.getShortcutMultiSelect())
        || !appBase
            .getShortcutMultiSelect()
            .contains(AppBaseRepository.SHORTCUT_ACTIVE_TRADING_NAME);
  }

  protected List<QuickMenuItem> getItems() {
    User user = userService.getUser();
    Company activeCompany = user.getActiveCompany();
    TradingName currentTradingName = user.getTradingName();
    List<QuickMenuItem> items = new ArrayList<>();

    if (activeCompany == null || ObjectUtils.isEmpty(activeCompany.getTradingNameList())) {
      return items;
    }

    String action = UserController.class.getName() + ":" + "setTradingName";

    for (TradingName tradingName : activeCompany.getTradingNameList()) {
      QuickMenuItem item =
          new QuickMenuItem(
              tradingName.getName(),
              action,
              new Context(tradingName.getId(), TradingName.class),
              tradingName.equals(currentTradingName));
      items.add(item);
    }

    return items;
  }
}
