package com.axelor.apps.base.service.pricing;

import java.util.List;
import java.util.Optional;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;

public interface PricingService {

	/**
	 * This method will "consume" the pricing, meaning that the pricing will be
	 * applied on the model.
	 * @param pricing: non-null
	 * @param model non-null
	 * @param classModel-null
	 * @throws AxelorException if Pricing model does not match with classModel
	 */
	<T extends Model> void comsume(Pricing pricing, Model model, Class<T> classModel) throws AxelorException;
	
	/**
	 * This method will get a random pricing from pricings filtered with company, product, productCategory, modelName, previousPricing.
	 * @param company {@link Company}: can be null
	 * @param product {@link Product}: can be null
	 * @param productCategory {@link ProductCategory}: can be null
	 * @param modelName {@link String}: can be null or empty
	 * @param previousPricing {@link Pricing}: can be null
	 * @return a {@link Optional} of Pricing.
	 */
	Optional<Pricing> getRandomPricing(Company company, Product product, ProductCategory productCategory, String modelName, Pricing previousPricing);
	
	/**
	 * This method will get all pricings filtered with company, product, productCategory, modelName, previousPricing.
	 * @param company {@link Company}: can be null
	 * @param product {@link Product}: can be null
	 * @param productCategory {@link ProductCategory}: can be null
	 * @param modelName {@link String}: can be null or empty
	 * @param previousPricing {@link Pricing}: can be null
	 * @return a {@link Optional} of Pricing.
	 */
	List<Pricing> getPricings(Company company, Product product, ProductCategory productCategory, String modelName, Pricing previousPricing);
	
	/**
	 * This method will checks if model can be classified by a pricingLine in the pricing.
	 * @param pricing: non-null
	 * @param model: non-null
	 * @param classModel: non-null
	 * @throws AxelorException if Pricing model does not match with classModel
	 */
	<T extends Model>  boolean hasPricingLines(Pricing pricing, Model model, Class<T> classModel) throws AxelorException;
	
	
	
}
