package com.axelor.apps.base.service.pricing;

import java.util.Optional;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;

public interface PricingLineService {


  /**
   * This method will return a random pricing line that classify the model in the pricing.
   *
   * @param pricing: non-null
   * @param model non-null
   * @throws AxelorException if Pricing model does not match with classModel
   */
	Optional<PricingLine> getRandomMatchedPricingLines(
      Pricing pricing, Model model) throws AxelorException;
}
