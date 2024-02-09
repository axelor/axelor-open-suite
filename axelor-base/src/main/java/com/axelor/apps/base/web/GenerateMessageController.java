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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.LocalizationRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;

@Singleton
public class GenerateMessageController {

  public void templateDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    String model = (String) context.get("_templateContextModel");

    Object localizationObj = context.get("localization");
    Localization localization;
    if (localizationObj == null) {
      localization = null;
    } else if (localizationObj instanceof Map) {
      Map<String, Object> localizationMap = (Map<String, Object>) localizationObj;
      localization =
          Beans.get(LocalizationRepository.class)
              .find(Long.parseLong(localizationMap.get("id").toString()));
    } else if (localizationObj instanceof Localization) {
      localization = (Localization) localizationObj;
    } else {
      throw new IllegalArgumentException("erreur...");
    }

    String domain;

    if (localization == null) {
      domain = "self.metaModel.fullName = '" + model + "' and self.isSystem != true";
    } else {
      Long localizationId = localization.getId();
      Query query =
          JPA.em()
              .createQuery(
                  "SELECT DISTINCT t.id "
                      + "FROM Template t "
                      + "JOIN t.localizationSet l "
                      + "WHERE l.id = :localizationId")
              .setParameter("localizationId", localizationId);
      List resultList = query.getResultList();
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      for (int i = 0; i < resultList.size(); i++) {
        long curId = (long) resultList.get(i);
        if (i != resultList.size() - 1) {
          sb.append(curId);
          sb.append(',');
        } else {
          sb.append(curId);
        }
      }
      sb.append(')');
      String ids = sb.toString();

      domain =
          "self.metaModel.fullName = '"
              + model
              + "' and self.isSystem != true and self.id IN "
              + ids;
    }

    response.setAttr("_xTemplate", "domain", domain);
  }
}
