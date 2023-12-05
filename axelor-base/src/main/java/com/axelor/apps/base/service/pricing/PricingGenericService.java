package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.db.Model;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface PricingGenericService {
  void usePricings(Company company, Model model) throws AxelorException;

  @Transactional
  void computePricingsOnModel(Company company, Model model) throws AxelorException;

  List<Pricing> getPricings(Company company, Model model);

  void computePricingsOnChildren(Company company, Model model) throws AxelorException;

  String updatePricingScaleLogs(List<StringBuilder> logsList, Model model);
}
