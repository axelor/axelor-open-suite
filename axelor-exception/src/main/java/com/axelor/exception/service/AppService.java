package com.axelor.exception.service;

import com.axelor.db.Model;

public interface AppService {

	/**
	 * Get persistent class.
	 * 
	 * @param model
	 * @return
	 */
	Class<? extends Model> getPersistentClass(Model model);

}
