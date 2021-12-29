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
package com.axelor.apps.portal.web.service;

import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class TranslationService extends AbstractWebService {

  @SuppressWarnings({"unchecked", "rawtypes"})
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetch(Map<String, Object> values) {
    String language = values.getOrDefault("lang", AuthUtils.getUser().getLanguage()).toString();
    List<String> keys =
        ObjectUtils.isEmpty(values.get("keys"))
            ? new ArrayList<>()
            : (ArrayList<String>) values.get("keys");
    if (ObjectUtils.isEmpty(keys)) {
      return null;
    }
    List<Map> translations =
        Beans.get(MetaTranslationRepository.class)
            .all()
            .filter("self.language = :language AND self.key in :keys")
            .bind("language", language)
            .bind("keys", keys)
            .select("message", "key", "language")
            .fetch(0, 0);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(translations).success();
  }
}
