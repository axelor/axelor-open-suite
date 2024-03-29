package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InterventionPartnerServiceImpl implements InterventionPartnerService {

  @Override
  public Partner getDefaultInvoicedPartner(Partner partner) {
    if (partner == null || partner.getId() == null) {
      return null;
    }

    if (CollectionUtils.isEmpty(partner.getManagedByPartnerLinkList())) {
      return partner;
    }

    // Retrieve all Invoiced by Type
    List<PartnerLink> partnerLinkInvoicedByList =
        partner.getManagedByPartnerLinkList().stream()
            .filter(
                partnerSupplychainLink ->
                    PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_BY.equals(
                        partnerSupplychainLink.getPartnerLinkType().getTypeSelect()))
            .collect(Collectors.toList());
    // If there is only one, then it is the default one
    if (partnerLinkInvoicedByList.size() == 1) {
      PartnerLink partnerLinkInvoicedBy = partnerLinkInvoicedByList.get(0);
      return partnerLinkInvoicedBy.getPartner2();
    } else if (CollectionUtils.isEmpty(partnerLinkInvoicedByList)) {
      return partner;
    } else {
      return null;
    }
  }
}
