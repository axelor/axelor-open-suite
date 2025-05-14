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
