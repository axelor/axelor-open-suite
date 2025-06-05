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
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.repo.CallTenderRepository;
import com.axelor.apps.purchase.service.CallTenderGenerateService;
import com.axelor.apps.purchase.service.CallTenderMailService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;
import javax.mail.MessagingException;

public class CallTenderController {

  public void generateCallTenderOffers(ActionRequest request, ActionResponse response) {

    var callTender = request.getContext().asType(CallTender.class);
    callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
    if (callTender != null) {
      Beans.get(CallTenderGenerateService.class).generateCallTenderOffers(callTender);
      response.setReload(true);
    }
  }

  public void sendCallTenderOffers(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException, MessagingException, ClassNotFoundException {

    var callTender = request.getContext().asType(CallTender.class);

    callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
    if (callTender != null) {
      Beans.get(CallTenderMailService.class).sendCallTenderOffers(callTender);
      response.setInfo(I18n.get("Mails successfully planned for sending."));
    }
    response.setReload(true);
  }
}
