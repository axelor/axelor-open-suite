package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Partner;

public interface JournalCheckPartnerTypeService {

  /** True if the partner is authorized on this journal. */
  boolean isPartnerCompatible(Journal journal, Partner partner);
}
