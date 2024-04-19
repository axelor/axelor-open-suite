package com.axelor.apps.base.service.partner.registrationnumber;

import com.axelor.apps.base.db.Partner;

public interface PartnerRegistrationCodeViewService {
  String getRegistrationCodeTitleFromTemplate(Partner partner);

  boolean isSirenHidden(Partner partner);

  boolean isNicHidden(Partner partner);

  boolean isTaxNbrHidden(Partner partner);
}
