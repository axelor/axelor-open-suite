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
package com.axelor.apps.portal.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.client.portal.db.repo.CardRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class CardServiceImpl implements CardService {

  @Inject CardRepository cardRepo;

  @Override
  @Transactional
  public Card createCard(Partner partner, Map<String, Object> details, Long id) {

    Card card = null;
    if (id != null) {
      card = cardRepo.find(id);
    }

    if (card == null) {
      card = new Card();
    }

    card.setName(details.get("name").toString());
    card.setCardNumber(Long.parseLong(details.get("number").toString()));
    card.setExpiryMonth(Integer.parseInt(details.get("exp_month").toString()));
    card.setExpiryYear(Integer.parseInt(details.get("exp_year").toString()));
    card.setCvc(Integer.parseInt(details.get("cvc").toString()));
    card.setIsDefault(Boolean.parseBoolean(details.get("isDefault").toString()));
    card.setPartner(partner);
    cardRepo.save(card);
    return card;
  }

  @Override
  public Card getDefault(Partner partner) {

    return cardRepo
        .all()
        .filter("COALESCE(self.isDefault, false) = true AND self.partner = :partner")
        .bind("partner", partner)
        .fetchOne();
  }
}
