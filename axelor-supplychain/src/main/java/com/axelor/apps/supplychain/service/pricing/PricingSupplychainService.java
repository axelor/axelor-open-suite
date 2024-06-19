package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Model;

public interface PricingSupplychainService {
  Model computeFiscalPositionPricing(Model model, Company company) throws AxelorException;
}
