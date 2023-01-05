/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.ModelEmailLink;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.ModelEmailLinkService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ModelEmailLinkController {

  public void validateModelFields(ActionRequest request, ActionResponse response)
      throws AxelorException {

    AppBase appBase = request.getContext().asType(AppBase.class);
    if (ObjectUtils.isEmpty(appBase.getEmailLinkList())) {
      return;
    }

    for (ModelEmailLink modelEmailLink : appBase.getEmailLinkList()) {
      modelEmailLink = EntityHelper.getEntity(modelEmailLink);
      if (!Beans.get(ModelEmailLinkService.class).validateModelFields(modelEmailLink)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(IExceptionMessage.INVALID_FIELD),
                modelEmailLink.getEmailField(),
                modelEmailLink.getMetaModel().getName()));
      }
    }
  }
}
