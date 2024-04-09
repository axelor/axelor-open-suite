/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportCustomerRequest {

  protected SequenceService sequenceService;

  @Inject
  public ImportCustomerRequest(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  public Object importCustomerRequest(Object bean, Map<String, Object> values)
      throws AxelorException {

    assert bean instanceof CustomerRequest;

    CustomerRequest customerRequest = (CustomerRequest) bean;
    customerRequest.setSequence(
        sequenceService.getSequenceNumber(
            SequenceRepository.CUSTOMER_REQUEST_SEQUENCE,
            null,
            CustomerRequest.class,
            "sequence",
            customerRequest));

    Long deliveredPartner = Long.valueOf((String) values.get("deliveredPartner_importId"));
    Partner partner =
        JPA.all(Partner.class)
            .filter("self.importId = :id")
            .bind("id", deliveredPartner)
            .fetchOne();

    customerRequest.setDeliveredPartner(partner);

    List<PartnerAddress> partnerAddresses =
        partner.getPartnerAddressList().stream()
            .filter(
                partnerAddress ->
                    partnerAddress.getIsDeliveryAddr() && partnerAddress.getIsDefaultAddr())
            .collect(Collectors.toList());
    if (!partnerAddresses.isEmpty()) {
      customerRequest.setInterventionAddress(partnerAddresses.get(0).getAddress());
    }

    return customerRequest;
  }
}
