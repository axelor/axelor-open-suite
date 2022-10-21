package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.mapper.Mapper;
import com.google.common.base.Strings;

public class JournalCheckPartnerTypeServiceImpl implements JournalCheckPartnerTypeService {

  @Override
  public boolean isPartnerCompatible(Journal journal, Partner partner) {
    if (journal == null || Strings.isNullOrEmpty(journal.getCompatiblePartnerTypeSelect())) {
      return true;
    }
    String[] compatiblePartnerTypeSelectArray = journal.getCompatiblePartnerTypeSelect().split(",");
    Mapper partnerMapper = Mapper.of(Partner.class);
    for (String compatiblePartnerType : compatiblePartnerTypeSelectArray) {
      if (Boolean.TRUE.equals(partnerMapper.get(partner, compatiblePartnerType))) {
        return true;
      }
    }
    return false;
  }
}
