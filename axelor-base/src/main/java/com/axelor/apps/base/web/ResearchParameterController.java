/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.ResearchParameter;
import com.axelor.apps.base.db.ResearchParameterConfig;
import com.axelor.apps.base.db.repo.ResearchPrimaryKeyRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ResearchParameterController {

  public void setQuery(ActionRequest request, ActionResponse response) {
    try {
      ResearchParameter researchParameter = request.getContext().asType(ResearchParameter.class);
      StringBuilder queryStr = new StringBuilder();
      ;
      if (researchParameter.getMapping() != null) {
        queryStr.append(researchParameter.getMapping().getName());
      }
      if (researchParameter.getResearchPrimaryKey() != null) {
        if (ResearchPrimaryKeyRepository.RESEARCH_PRIMARY_TYPE_SELECT_TEXT.equals(
            researchParameter.getResearchPrimaryKey().getTypeSelect())) {
          queryStr.append(" like :");
        } else if (ResearchPrimaryKeyRepository.RESEARCH_PRIMARY_TYPE_SELECT_LOCAL_DATE.equals(
            researchParameter.getResearchPrimaryKey().getTypeSelect())) {
          queryStr.append(" = :");
        }
      }
      if (researchParameter.getBinding() != null) {
        queryStr.append(researchParameter.getBinding());
      }
      response.setValue("query", queryStr.toString());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void getMappingDomain(ActionRequest request, ActionResponse response) {
    try {
      ResearchParameter researchParameter = request.getContext().asType(ResearchParameter.class);
      ResearchParameterConfig researchParameterConfig =
          researchParameter.getResearchParameterConfig();
      if (researchParameterConfig == null) {
        researchParameterConfig =
            request.getContext().getParent().asType(ResearchParameterConfig.class);
      }

      String domain =
          String.format("self.metaModel.id = %d ", researchParameterConfig.getMetaModel().getId());

      if (researchParameter.getResearchPrimaryKey() != null) {
        domain +=
            " AND "
                + String.format(
                    "self.typeName = %s",
                    "'" + researchParameter.getResearchPrimaryKey().getTypeSelect() + "'");
      }
      response.setAttr("mapping", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
