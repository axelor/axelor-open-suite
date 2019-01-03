/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class GenerateMessageController {

  @Inject private LanguageRepository languageRepo;

  public void templateDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    String model = (String) context.get("_templateContextModel");

    Object languageObj = context.get("language");
    Language language;
    if (languageObj == null) {
      language = null;
    } else if (languageObj instanceof Map) {
      Map<String, Object> languageMap = (Map<String, Object>) languageObj;
      language = languageRepo.find(Long.parseLong(languageMap.get("id").toString()));
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
