package com.axelor.apps.base.service.pricing;

import java.util.List;
import java.util.Optional;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;

public interface PricingLineService {

	/**
	 * This method will return every pricing lines that classify the model in the pricing.
	 * @param pricing: non-null
	 * @param model non-null
	 * @param classModel-null
	 * @throws AxelorException if Pricing model does not match with classModel
	 */
	<T extends Model>  List<PricingLine> getMatchedPricingLines(Pricing pricing, Model model, Class<T> classModel) throws AxelorException;
	
	/**
	 * This methods will return every pricing lines of the pricing.
	 * @param pricing: non-null
	 */
	List<PricingLine> getPricingLines(Pricing pricing);
	
	/**
	 * This method will return a random pricing line that classify the model in the pricing.
	 * @param pricing: non-null
	 * @param model non-null
	 * @param classModel-null
	 * @throws AxelorException if Pricing model does not match with classModel
	 */
	<T extends Model>  Optional<PricingLine> getRandomMatchedPricingLines(Pricing pricing, Model model, Class<T> classModel) throws AxelorException;
	
	
}
