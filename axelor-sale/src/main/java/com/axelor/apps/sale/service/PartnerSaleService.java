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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.db.JPA;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PartnerSaleService extends PartnerService {

  @Override
  public List<Long> findPartnerMails(Partner partner) {
    List<Long> idList = new ArrayList<Long>();

    idList.addAll(this.findMailsFromPartner(partner));
    idList.addAll(this.findMailsFromSaleOrder(partner));

    Set<Partner> contactSet = partner.getContactPartnerSet();
    if (contactSet != null && !contactSet.isEmpty()) {
      for (Partner contact : contactSet) {
        idList.addAll(this.findMailsFromPartner(contact));
        idList.addAll(this.findMailsFromSaleOrderContact(contact));
      }
    }
    return idList;
  }

  @Override
  public List<Long> findContactMails(Partner partner) {
    List<Long> idList = new ArrayList<Long>();

    idList.addAll(this.findMailsFromPartner(partner));
    idList.addAll(this.findMailsFromSaleOrderContact(partner));

    return idList;
  }

  @SuppressWarnings("unchecked")
  public List<Long> findMailsFromSaleOrder(Partner partner) {
    String query =
        "SELECT DISTINCT(email.id) FROM Message as email, SaleOrder as so, Partner as part"
            + " WHERE part.id = "
            + partner.getId()
            + " AND so.clientPartner = part.id AND email.mediaTypeSelect = 2 AND "
            + "((email.relatedTo1Select = 'com.axelor.apps.sale.db.SaleOrder' AND email.relatedTo1SelectId = so.id) "
            + "OR (email.relatedTo2Select = 'com.axelor.apps.sale.db.SaleOrder' AND email.relatedTo2SelectId = so.id))";
    return JPA.em().createQuery(query).getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findMailsFromSaleOrderContact(Partner partner) {
    String query =
        "SELECT DISTINCT(email.id) FROM Message as email, SaleOrder as so, Partner as part"
            + " WHERE part.id = "
            + partner.getId()
            + " AND so.contactPartner = part.id AND email.mediaTypeSelect = 2 AND "
            + "((email.relatedTo1Select = 'com.axelor.apps.sale.db.SaleOrder' AND email.relatedTo1SelectId = so.id) "
            + "OR (email.relatedTo2Select = 'com.axelor.apps.sale.db.SaleOrder' AND email.relatedTo2SelectId = so.id))";
    return JPA.em().createQuery(query).getResultList();
  }
}
