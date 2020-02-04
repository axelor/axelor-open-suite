/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.apps.supplychain.db.repo.DeclarationOfExchangesRepository;
import com.axelor.apps.supplychain.service.declarationofexchanges.DeclarationOfExchangesService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.nio.file.Path;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
public class DeclarationOfExchangesController {

  public void export(ActionRequest request, ActionResponse response) {
    try {
      DeclarationOfExchanges declarationOfExchanges =
          request.getContext().asType(DeclarationOfExchanges.class);
      declarationOfExchanges =
          Beans.get(DeclarationOfExchangesRepository.class).find(declarationOfExchanges.getId());
      Pair<Path, String> result =
          Beans.get(DeclarationOfExchangesService.class).export(declarationOfExchanges);
      String fileLink = result.getLeft().toString();
      String name = result.getRight();
      ActionViewBuilder actionViewBuilder = ActionView.define(name).add("html", fileLink);

      if (!"pdf".equalsIgnoreCase(declarationOfExchanges.getFormatSelect())) {
        actionViewBuilder.param("download", "true");
      }

      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
