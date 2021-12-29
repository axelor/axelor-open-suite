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
package com.axelor.apps.portal.web;

import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.portal.exception.IExceptionMessage;
import com.axelor.apps.portal.service.CardService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CardController {

  public void checkDefaultCard(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Card card = request.getContext().asType(Card.class);
    if (card.getIsDefault()) {
      Card defaultCard = Beans.get(CardService.class).getDefault(card.getPartner());
      if (defaultCard != null && !defaultCard.getId().equals(card.getId())) {
        response.setValue("isDefault", false);
        response.setError(I18n.get(IExceptionMessage.STRIPE_DEFAULT_CARD));
      }
    }
  }
}
