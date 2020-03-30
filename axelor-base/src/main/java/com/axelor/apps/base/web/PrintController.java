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

import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.service.PrintService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PrintController {

  public void generatePDF(ActionRequest request, ActionResponse response) {
    Print print = request.getContext().asType(Print.class);

    String pdfPath = null;
    try {
      pdfPath = Beans.get(PrintService.class).generatePDF(print);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }

    if (pdfPath != null) {
      response.setView(
          ActionView.define(I18n.get("PRINT_NOUN") + " " + print.getName())
              .add("html", pdfPath)
              .map());
    } else {
      response.setFlash(
          I18n.get("Error in print. Please check report configuration and print settings."));
    }
  }
}
