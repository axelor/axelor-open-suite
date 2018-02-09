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
package com.axelor.apps.prestashop.exports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.prestashop.entities.PrestashopProductCategory;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.PrestashopTranslatableString;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportCategoryServiceImpl implements ExportCategoryService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductCategoryRepository categoryRepo;

	@Inject
	public ExportCategoryServiceImpl(ProductCategoryRepository categoryRepo) {
		this.categoryRepo = categoryRepo;
	}

	@Override
	@Transactional
	public void exportCategory(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== PRODUCT CATEGORIES ======%n"));

		Query<ProductCategory> q = categoryRepo.all();
		if(endDate != null) {
			q.filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId IS NULL", endDate, endDate);
		}
		q.order("-self.parentProductCategory.id");

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		final List<PrestashopProductCategory> remoteCategories = ws.fetchAll(PrestashopResourceType.PRODUCT_CATEGORIES);
		final Map<Integer, PrestashopProductCategory> categoriesById = new HashMap<>();
		for(PrestashopProductCategory country : remoteCategories) {
			categoriesById.put(country.getId(), country);
		}

		final PrestashopProductCategory defaultCategory = ws.fetchDefault(PrestashopResourceType.PRODUCT_CATEGORIES);
		final PrestashopProductCategory remoteRootCategory = ws.fetchOne(PrestashopResourceType.PRODUCT_CATEGORIES, Collections.singletonMap("is_root_category", "1"));

		if(remoteRootCategory == null) {
			logBuffer.write(String.format("[ERROR] Unable to fetch root category from remote end, giving up%n"));
			return;
		}

		for(ProductCategory localCategory : q.fetch()) {
			logBuffer.write(String.format("Exporting product category #%d (%s) – ", localCategory.getId(), localCategory.getName()));

			try {
				PrestashopProductCategory remoteCategory;
				if(localCategory.getPrestaShopId() != null) {
					logBuffer.write("prestashop id=" + localCategory.getPrestaShopId());
					remoteCategory = categoriesById.get(localCategory.getPrestaShopId());
					if(remoteCategory == null) {
						logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
						log.error("Unable to fetch remote product category #{} ({}), something's probably very wrong, skipping",
								localCategory.getPrestaShopId(), localCategory.getName());
						++errors;
						continue;
					}
				} else {
					remoteCategory = new PrestashopProductCategory();

					PrestashopTranslatableString str = defaultCategory.getName().clone();
					str.clearTranslations(localCategory.getName());
					remoteCategory.setName(str);

					str = defaultCategory.getLinkRewrite().clone();
					str.clearTranslations(localCategory.getCode());
					remoteCategory.setLinkRewrite(str);
				}

				// FIXME handle language correctly, only override value for appConfig.textsLanguage
				remoteCategory.setUpdateDate(LocalDateTime.now());
				remoteCategory.getName().getTranslations().get(0).setTranslation(localCategory.getName());
				if(localCategory.getParentProductCategory() == null || localCategory.getParentProductCategory().getPrestaShopId() == null) {
					remoteCategory.setParentId(remoteRootCategory.getId());
				} else {
					remoteCategory.setParentId(localCategory.getParentProductCategory().getPrestaShopId());
				}
				remoteCategory = ws.save(PrestashopResourceType.PRODUCT_CATEGORIES, remoteCategory);
				localCategory.setPrestaShopId(remoteCategory.getId());
				logBuffer.write(String.format(" [SUCCESS]%n"));
				++done;
			} catch (PrestaShopWebserviceException e) {
				logBuffer.write(String.format(" [ERROR] %s (full trace is in application logs)%n", e.getLocalizedMessage()));
				log.error(String.format("Exception while synchronizing product category #%d (%s)", localCategory.getId(), localCategory.getName()), e);
				++errors;
			}
		}

		logBuffer.write(String.format("%n=== END OF PRODUCT CATEGORIES EXPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
