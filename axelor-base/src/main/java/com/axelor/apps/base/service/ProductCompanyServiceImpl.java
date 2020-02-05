/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import java.util.Set;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;

public class ProductCompanyServiceImpl implements ProductCompanyService {

  @Inject protected AppBaseService appBaseService;
	
  @Override
  public Object get(Product originalProduct, String fieldName, Company company) {
	  if (originalProduct == null) {
		  System.out.println("Message d'insulte générique pour dire que tu as pas passé de produit dans la fonction pour récupérer le champ d'un produit");
	  } else if (fieldName == null || fieldName.trim().equals("")) {
		  System.out.println("Message d'insulte générique pour dire que tu as pas passé le nom du champ dans la fonction pour récupérer le champ d'un produit");
	  }
	  
	  Product product = originalProduct;
	  Mapper mapper = Mapper.of(Product.class);

	  if (company != null && originalProduct.getProductCompanyList() != null) {
		  for (ProductCompany productCompany : originalProduct.getProductCompanyList()) {
			if (productCompany.getCompany().getId() == company.getId()) {
			  Set<MetaField> companySpecificFields = appBaseService.getAppBase().getCompanySpecificProductFieldsList();
			  for (MetaField field : companySpecificFields) {
				if (field.getName().equals(fieldName)) {
				  product = productCompany;
				  break;
				}
			  }
			  break;
			}
		  }
	  }
	  
	  return mapper.get(product, fieldName);
  }

}
