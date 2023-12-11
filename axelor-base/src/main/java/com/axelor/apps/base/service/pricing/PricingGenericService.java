package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.db.Model;
import java.util.List;

public interface PricingGenericService {
  <T extends Model> void usePricings(Company company, Class<?> modelClass, Long modelId)
      throws AxelorException;

  <T extends Model> void usePricings(Company company, Class<?> modelClass, List<Integer> idList)
      throws AxelorException;

  void usePricings(Company company, Model model) throws AxelorException;

  void computePricingsOnModel(Company company, Model model) throws AxelorException;

  List<Pricing> getPricings(Company company, Model model);

  void computePricingsOnChildren(Company company, Model model) throws AxelorException;

  String updatePricingScaleLogs(List<StringBuilder> logsList, Model model);
}
