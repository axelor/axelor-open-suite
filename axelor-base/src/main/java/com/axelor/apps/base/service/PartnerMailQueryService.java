package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface PartnerMailQueryService {

  List<Long> findMailsFromPartner(Partner partner, int emailType);
}
