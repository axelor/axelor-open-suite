/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProductVariant;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.exception.AxelorException;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductServiceAccountImpl extends ProductServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceAccountImpl.class);
		
	@Override
	public Product createProduct(Product productModel, ProductVariant productVariant)  {
		
		Product product = super.createProduct(productModel, productVariant);
		
		product.setInvoiceLineType(productModel.getInvoiceLineType());
		
		return product;
	}
	
	
	
}