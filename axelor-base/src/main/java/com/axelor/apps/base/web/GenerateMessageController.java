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

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class GenerateMessageController {

  public void templateDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    String model = (String) context.get("_templateContextModel");

    Object languageObj = context.get("language");
    Language language;
    if (languageObj == null) {
      language = null;
    } else if (languageObj instanceof Map) {
      Map<String, Object> languageMap = (Map<String, Object>) languageObj;
      language =
          Beans.get(LanguageRepository.class)
              .find(Long.parseLong(languageMap.get("id").toString()));
    } else if (languageObj instanceof Language) {
      language = (Language) languageObj;
    } else {
      throw new IllegalArgumentException("erreur...");
    }

    String domain;

    if (language == null) {
      domain = "self.metaModel.fullName = '" + model + "' and self.isSystem != true";
    } else {
      domain =
          "self.metaModel.fullName = '"
              + model
              + "' and self.isSystem != true and self.language.id = "
              + language.getId();
    }

    response.setAttr("_xTemplate", "domain", domain);
  }
}
