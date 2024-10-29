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

import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.service.TagService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class TagController {

  @ErrorException
  public void setConcernedModelsDomain(ActionRequest request, ActionResponse response) {

    Tag tag = request.getContext().asType(Tag.class);
    Object packageName = request.getContext().get("_packageName");
    if (tag == null || packageName == null) {
      return;
    }

    response.setAttr(
        "concernedModelSet", "domain", "self.packageName like '%" + packageName + "%'");
  }

  public void setDefaultConcernedModel(ActionRequest request, ActionResponse response) {

    Tag tag = request.getContext().asType(Tag.class);
    Context parentContext = request.getContext().getParent();

    if (tag == null) {
      return;
    }

    TagService tagService = Beans.get(TagService.class);
    if (parentContext != null && parentContext.get("_model") != null) {
      String fullNameModel = parentContext.get("_model").toString();
      tagService.addMetaModelToTag(tag, fullNameModel);
    }
    if (request.getContext().get("_fieldModel") != null) {
      tagService.addMetaModelToTag(tag, request.getContext().get("_fieldModel").toString());
    }

    response.setValue("concernedModelSet", tag.getConcernedModelSet());
  }
}
