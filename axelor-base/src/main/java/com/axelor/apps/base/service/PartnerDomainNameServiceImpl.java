package com.axelor.apps.base.service;

import com.axelor.apps.base.db.DomainName;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PartnerDomainNameServiceImpl implements PartnerDomainNameService {

  PartnerRepository partnerRepo;

  public PartnerDomainNameServiceImpl(PartnerRepository partnerRepo) {
    this.partnerRepo = partnerRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<Partner> getPartnersWithSameDomainNameAndUpdateDomainNameList(Partner partner) {
    List<Partner> contactWithSameDomainNameList = new ArrayList<>();
    Set<Partner> contactList = partner.getContactPartnerSet();
    List<String> partnerDomainNameStrList =
        partner.getDomainNameList().stream().map(dm -> dm.getName()).collect(Collectors.toList());
    List<Long> idList = contactList.stream().map(c -> c.getId()).collect(Collectors.toList());
    idList.add(partner.getId());
    List<String> contactsDomainNameStrList =
        partner.getContactPartnerSet().stream()
            .filter(cp -> cp.getEmailAddress() != null)
            .map(cp -> cp.getEmailAddress().getName().split("@")[1].replace("]", ""))
            .collect(Collectors.toList());
    for (String contactDomainNameStr : contactsDomainNameStrList) {
      if (!partnerDomainNameStrList.contains(contactDomainNameStr)) {
        List<Partner> contactWithSameDomainNameListTemp =
            partnerRepo
                .all()
                .filter(
                    "self.isContact is true and self.emailAddress is not null and self.emailAddress.name like :domainName and self.id not in ( :idList )")
                .bind("domainName", "%" + contactDomainNameStr + "%")
                .bind("idList", idList)
                .fetch();
        if (ObjectUtils.notEmpty(contactWithSameDomainNameListTemp)) {
          contactWithSameDomainNameList.addAll(contactWithSameDomainNameListTemp);
        }
        partner.addDomainNameListItem(new DomainName(contactDomainNameStr, partner));
      }
    }
    List<DomainName> partnerdomainNameList = partner.getDomainNameList();
    for (int i = 0; i < partnerdomainNameList.size(); i++) {
      if (!contactsDomainNameStrList.contains(partnerdomainNameList.get(i).getName())) {
        partner.removeDomainNameListItem(partnerdomainNameList.get(i));
        i--;
      }
    }
    return contactWithSameDomainNameList;
  }
}
