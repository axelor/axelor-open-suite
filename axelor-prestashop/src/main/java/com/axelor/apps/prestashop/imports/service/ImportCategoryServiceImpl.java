/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.prestashop.imports.service;

import java.io.IOException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.db.IPrestaShopBatch;
import com.axelor.apps.prestashop.entities.PrestashopProductCategory;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportCategoryServiceImpl implements ImportCategoryService  {
	private ProductCategoryRepository productCategoryRepo;

	@Inject
	public ImportCategoryServiceImpl(ProductCategoryRepository productCategoryRepo) {
		this.productCategoryRepo = productCategoryRepo;
	}

	@Override
	@Transactional
	public void importCategory(AppPrestashop appConfig, ZonedDateTime endDate, Writer logWriter) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logWriter.write(String.format("%n====== PRODUCT CATEGORIES ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		final List<PrestashopProductCategory> remoteCategories = ws.fetchAll(PrestashopResourceType.PRODUCT_CATEGORIES, Collections.singletonList("id_parent_ASC"));

		final PrestashopProductCategory remoteRootCategory = ws.fetchOne(PrestashopResourceType.PRODUCT_CATEGORIES, Collections.singletonMap("is_root_category", "1"));
		final int language = (appConfig.getTextsLanguage().getPrestaShopId() == null ? 1 : appConfig.getTextsLanguage().getPrestaShopId());

		if(remoteRootCategory == null) {
			logWriter.write(String.format("[ERROR] Unable to fetch root category from remote end, giving up%n"));
			return;
		}

		for(PrestashopProductCategory remoteCategory : remoteCategories) {
			logWriter.write(String.format("Importing PrestaShop product category #%d (%s) – ", remoteCategory.getId(), remoteCategory.getName().getTranslation(language)));

			if(remoteCategory.isRootCategory()) {
				logWriter.write(String.format("flagged as root category, ignoring [SUCCESS]%n"));
				++done;
				continue;
			}

			ProductCategory parentCategory;
			if(Objects.equals(remoteCategory.getParentId(), remoteRootCategory.getId())) {
				parentCategory = null;
			} else {
				parentCategory = productCategoryRepo.findByPrestaShopId(remoteCategory.getParentId());
				if(parentCategory == null) {
					logWriter.write(String.format(" [WARNING] Category belongs to a not-yet synced category (%d), skipping%n", remoteCategory.getParentId()));
					continue;
				}
			}

			final String categoryCode = remoteCategory.getLinkRewrite().getTranslation(language).toUpperCase();

			ProductCategory localCategory = productCategoryRepo.findByPrestaShopId(remoteCategory.getId());
			if(localCategory == null) {
				localCategory = productCategoryRepo.findByCode(categoryCode);
				if(localCategory != null && localCategory.getPrestaShopId() != null) {
					logWriter.write(String.format(" [ERROR] found a category with code %s but it is already bound to another PrestaShop category, skipping.%n", categoryCode));
					++errors;
					continue;
				}
				localCategory = new ProductCategory();
				localCategory.setPrestaShopId(remoteCategory.getId());
			}

			if(localCategory.getId() == null || appConfig.getPrestaShopMasterForCategories() == Boolean.TRUE) {
				localCategory.setParentProductCategory(parentCategory);
				localCategory.setName(remoteCategory.getName().getTranslation(language));
				localCategory.setCode(categoryCode);
				localCategory.setImportOrigin(IPrestaShopBatch.IMPORT_ORIGIN_PRESTASHOP);
				productCategoryRepo.save(localCategory);
			}  else {
				logWriter.write("local category exists and PrestaShop isn't master for categories, leaving untouched");
			}
			logWriter.write(String.format(" [SUCCESS]%n"));
			++done;
		}

		logWriter.write(String.format("%n=== END OF PRODUCT CATEGORIES IMPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
