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

import com.axelor.apps.base.service.AnonymizerLineService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Objects;

public class AnonymizerLineController {

  public void getFakerApiFieldDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    MetaField metaField = (MetaField) context.get("metaField");

    if (Objects.isNull(metaField)) {
      return;
    }

    MetaJsonField metaJsonField = (MetaJsonField) context.get("metaJsonField");
    String domain =
        Beans.get(AnonymizerLineService.class).getFakerApiFieldDomain(metaField, metaJsonField);

    response.setAttr("fakerApiField", "domain", domain);
  }
}
