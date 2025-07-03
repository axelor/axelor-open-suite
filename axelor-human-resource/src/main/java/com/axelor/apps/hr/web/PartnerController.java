/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.hr.service.PartnerEmployeeService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PartnerController {

  public void changeEmployeeType(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    Beans.get(PartnerEmployeeService.class).convertToContactPartner(partner);
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get("Partner"))
            .model(Partner.class.getName())
            .add("form", "partner-contact-form")
            .add("grid", "partner-contact-grid")
            .context("_showRecord", partner.getId())
            .map());
  }
}
