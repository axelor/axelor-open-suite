/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.web;

import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.repo.MobileMenuRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class MobileConfigController {

  public void selectAllMenus(ActionRequest request, ActionResponse response) {
    MobileConfig mobileConfig = request.getContext().asType(MobileConfig.class);
    List<MobileMenu> menuList =
        Beans.get(MobileMenuRepository.class)
            .findByParentApplication(mobileConfig.getSequence())
            .fetch();
    response.setValue("menuList", menuList);
  }
}
