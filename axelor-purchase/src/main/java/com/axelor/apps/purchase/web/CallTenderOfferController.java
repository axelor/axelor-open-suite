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

import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.service.CallTenderOfferService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class CallTenderOfferController {

  public void initCounter(ActionRequest request, ActionResponse response) {

    var callTenderOffer = request.getContext().asType(CallTenderOffer.class);

    if (callTenderOffer != null) {
      var callTender = getCallTender(request, callTenderOffer);
      Beans.get(CallTenderOfferService.class).setCounter(callTenderOffer, callTender);
      response.setValue("counter", callTenderOffer.getCounter());
    }
  }

  protected CallTender getCallTender(ActionRequest request, CallTenderOffer callTenderOffer) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && CallTender.class.equals(parentContext.getContextClass())) {
      return parentContext.asType(CallTender.class);
    } else {
      return callTenderOffer.getCallTender();
    }
  }
}
