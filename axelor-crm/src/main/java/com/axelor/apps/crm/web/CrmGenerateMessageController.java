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
package com.axelor.apps.crm.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.web.GenerateMessageController;
import com.axelor.db.Model;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;

public class CrmGenerateMessageController extends GenerateMessageController {

  @Override
  public ActionViewBuilder getActionView(
      long templateNumber, Model context, String model, String simpleModel, Message message) {

    ActionViewBuilder builder =
        super.getActionView(templateNumber, context, model, simpleModel, message);

    if (!model.equals(Lead.class.getName())
        && !model.equals(Partner.class.getName())
        && !model.equals("com.axelor.apps.sale.db.SaleOrder")) {
      return builder;
    }
    return builder.param("popup", "reload");
  }
}
