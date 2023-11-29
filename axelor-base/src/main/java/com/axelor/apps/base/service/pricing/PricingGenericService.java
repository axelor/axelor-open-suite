package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Model;
import com.google.inject.persist.Transactional;

public interface PricingGenericService {
  void usePricings(Company company, Model model) throws AxelorException;

  @Transactional
  void computePricingsOnModel(Company company, Model model) throws AxelorException;

  void computePricingsOnChildren(Company company, Model model) throws AxelorException;
}
