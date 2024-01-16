/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;

public interface ProductCompanyService {

  /**
   * A generic get method. Serves as a getter for any field of a product, which might or might not
   * be overridden by a company-specific version of the product.
   *
   * @param originalProduct the product which field we want to get
   * @param fieldName the field we want to obtain from the product
   * @param company the company to search for a company-specific version of the product
   * @return the value of the field, either the value specified for the company, or the default
   *     value
   */
  public Object get(Product originalProduct, String fieldName, Company company)
      throws AxelorException;

  /**
   * A generic set method. Serves as a setter for any field of a product, which might or might not
   * be overridden by a company-specific version of the product.
   *
   * @param originalProduct the product which field we want to set
   * @param fieldName the field of the product we want to set
   * @param fieldValue the value to set
   * @param company the company to search for a company-specific version of the product
   */
  public void set(Product originalProduct, String fieldName, Object fieldValue, Company company)
      throws AxelorException;

  /**
   * Method with the same purpose as get(Product originalProduct, String fieldName, Company
   * company). Except that this method WILL NOT try to get the field value from originalProduct if
   * there are no product company in it and will instead return null.
   *
   * @param originalProduct
   * @param fieldName
   * @param company
   * @return the value of the field, either the value specified for the company or null
   * @throws AxelorException
   */
  Object getWithNoDefault(Product originalProduct, String fieldName, Company company)
      throws AxelorException;

  /**
   * Method that checks if field name is a company specific product field
   *
   * @param fieldName
   * @return true if field name is a company specific field, else no.
   */
  boolean isCompanySpecificProductFields(String fieldName);
}
