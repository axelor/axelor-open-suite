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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ModelEmailLink;
import com.axelor.db.JPA;
import com.axelor.meta.db.MetaModel;

public class ModelEmailLinkServiceImpl implements ModelEmailLinkService {

  @Override
  public boolean validateModelFields(ModelEmailLink modelEmailLink) throws AxelorException {

    try {
      MetaModel metaModel = modelEmailLink.getMetaModel();
      String fieldStr = modelEmailLink.getEmailField();
      String query = String.format("SELECT %s FROM %s", fieldStr, metaModel.getName());
      JPA.em().createQuery(query, String.class).getFirstResult();
    } catch (Exception e) {
      return false;
    }

    return true;
  }
}
