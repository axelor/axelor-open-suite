/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ModelEmailLink;
import com.axelor.apps.base.db.repo.ModelEmailLinkRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import java.util.List;

public class ModelEmailLinkServiceImpl implements ModelEmailLinkService {

  @Override
  public void validateEmailLinks(List<ModelEmailLink> emailLinkList) throws AxelorException {

    if (ObjectUtils.isEmpty(emailLinkList)) {
      return;
    }

    for (ModelEmailLink modelEmailLink : emailLinkList) {
      modelEmailLink = EntityHelper.getEntity(modelEmailLink);
      if (!validateModelFields(modelEmailLink)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(IExceptionMessage.INVALID_FIELD),
                modelEmailLink.getEmailField(),
                modelEmailLink.getMetaModel().getName()));
      }
    }
  }

  protected boolean validateModelFields(ModelEmailLink modelEmailLink) throws AxelorException {

    try {
      MetaModel metaModel = modelEmailLink.getMetaModel();
      if (ModelEmailLinkRepository.ADDRESS_TYPE_MANUAL == modelEmailLink.getAddressTypeSelect()) {
        String filter = modelEmailLink.getFilter();
        String query =
            String.format("SELECT self.id FROM %s self WHERE %s", metaModel.getName(), filter);
        JPA.em().createQuery(query, Long.class).getFirstResult();
      } else if (ModelEmailLinkRepository.ADDRESS_TYPE_FROM == modelEmailLink.getAddressTypeSelect()
          || ModelEmailLinkRepository.ADDRESS_TYPE_TO == modelEmailLink.getAddressTypeSelect()) {
        String fieldStr = modelEmailLink.getEmailField();
        String query = String.format("SELECT %s FROM %s", fieldStr, metaModel.getName());
        JPA.em().createQuery(query, String.class).getFirstResult();
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }
}
