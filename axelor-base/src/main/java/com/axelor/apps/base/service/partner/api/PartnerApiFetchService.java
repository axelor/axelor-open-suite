package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;

public interface PartnerApiFetchService {
  String fetch(String siretNumber) throws AxelorException;
}
