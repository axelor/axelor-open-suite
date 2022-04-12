package com.axelor.apps.base.service.pricing;

public interface PricingComputer {
	
	/**
	 * Method to adds in the context (for the groovy script) a pair of key,value.
	 * If the key already exist in the context, the former value will be replaced.
	 * @param key: non-null
	 * @param value: non-null
	 * @return itself
	 */
	PricingComputer putInContext(String key, Object value);

}
