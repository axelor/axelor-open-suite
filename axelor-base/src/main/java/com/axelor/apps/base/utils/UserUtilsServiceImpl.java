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
package com.axelor.apps.base.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.studio.db.AppBase;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import wslite.json.JSONException;

public class UserUtilsServiceImpl implements UserUtilsService {

  protected AppBaseService appBaseService;

  @Inject
  public UserUtilsServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    Preconditions.checkNotNull(user, I18n.get("User cannot be null."));
    try {
      if (!user.getSendEmailUponPasswordChange()) {
        return;
      }
      AppBase appBase = appBaseService.getAppBase();
      Template template = appBase.getPasswordChangedTemplate();

      if (template == null) {
        throw new AxelorException(
            appBase,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get("Template for changed password is missing."));
      }
      Beans.get(TemplateMessageService.class).generateAndSendMessage(user, template);
    } finally {
      user.setTransientPassword(null);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeLinkedUser(User user) {
    if (user.getPartner() == null) {
      return;
    }
    PartnerRepository partnerRepository = Beans.get(PartnerRepository.class);
    Partner partner = partnerRepository.find(user.getPartner().getId());
    if (partner != null) {
      partner.setLinkedUser(null);
      partnerRepository.save(partner);
    }
  }
}
