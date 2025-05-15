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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.db.JPA;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.repo.MessageRepository;
import java.util.List;
import java.util.Optional;
import javax.persistence.TypedQuery;

public class PartnerMailQueryServiceImpl implements PartnerMailQueryService {

  protected String baseQuery =
      "SELECT DISTINCT email.id"
          + " FROM Message AS email"
          + " LEFT JOIN email.multiRelatedList AS related"
          + " WHERE email.mediaTypeSelect = :mediaType AND email.typeSelect = :emailType";

  protected String relatedToPartner =
      "related.relatedToSelect = :relatedToSelect AND related.relatedToSelectId = :partnerId";

  @Override
  public List<Long> findMailsFromPartner(Partner partner, int emailType) {

    Optional<EmailAddress> emailAddress = Optional.of(partner).map(Partner::getEmailAddress);

    String query;

    if (emailAddress.isPresent()) {
      query = computeEmailQuery(emailType);
    } else {
      query = baseQuery + " AND " + relatedToPartner;
    }

    TypedQuery<Long> messageQuery =
        JPA.em()
            .createQuery(query, Long.class)
            .setParameter("partnerId", partner.getId())
            .setParameter("relatedToSelect", "com.axelor.apps.base.db.Partner")
            .setParameter("emailType", emailType)
            .setParameter("mediaType", MessageRepository.MEDIA_TYPE_EMAIL);

    emailAddress.ifPresent(e -> messageQuery.setParameter("emailAddress", e));

    return messageQuery.getResultList();
  }

  protected String computeEmailQuery(int emailType) {

    String emailIsRelatedToPartner;

    if (emailType == MessageRepository.TYPE_RECEIVED) {
      emailIsRelatedToPartner = "email.fromEmailAddress = :emailAddress";
    } else {
      emailIsRelatedToPartner = ":emailAddress MEMBER OF email.toEmailAddressSet";
    }

    return baseQuery + " AND ((" + relatedToPartner + ") OR (" + emailIsRelatedToPartner + "))";
  }
}
