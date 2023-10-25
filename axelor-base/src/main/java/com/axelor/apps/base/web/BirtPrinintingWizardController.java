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

import com.axelor.apps.base.db.BirtPrintingWizard;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Optional;

@Singleton
public class BirtPrinintingWizardController {

  @SuppressWarnings("unchecked")
  public void onChangeMetaModel(ActionRequest request, ActionResponse response) {
    try {
      BirtPrintingWizard birtPrintingWizard = request.getContext().asType(BirtPrintingWizard.class);
      MetaModel metaModel = birtPrintingWizard.getMetaModel();
      String metaModelTargetName = null;
      if (metaModel != null) {
        Class<? extends Model> klass =
            (Class<? extends Model>) Class.forName(metaModel.getFullName());
        Mapper mapper = Mapper.of(klass);
        Property nameField =
            Optional.ofNullable(mapper.getNameField()).orElse(mapper.getProperty("id"));
        metaModelTargetName = nameField.getName();
      }
      response.setValue("metaModelTargetName", metaModelTargetName);
      response.setValue("recordValue", null);
      response.setValue("recordTitle", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
