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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.apps.prestashop.db.PrestashopOrderStatusCacheEntry;
import com.axelor.apps.prestashop.db.repo.PrestashopOrderStatusCacheEntryRepository;
import com.axelor.apps.prestashop.entities.PrestashopLanguage;
import com.axelor.apps.prestashop.entities.PrestashopOrderStatus;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportMetaDataServiceImpl implements ImportMetaDataService {
	private LanguageRepository languageRepository;
	private PrestashopOrderStatusCacheEntryRepository orderStatusRepository;

	@Inject
	public ImportMetaDataServiceImpl(LanguageRepository languageRepository,
			PrestashopOrderStatusCacheEntryRepository orderStatusRepository) {
		this.languageRepository = languageRepository;
		this.orderStatusRepository = orderStatusRepository;
	}

	@Override
	@Transactional
	public void importLanguages(PSWebServiceClient ws) throws PrestaShopWebserviceException {
		final List<PrestashopLanguage> remoteLanguages = ws.fetchAll(PrestashopResourceType.LANGUAGES);

		for(PrestashopLanguage remoteLanguage : remoteLanguages) {
			Language localLanguage = languageRepository.findByCode(remoteLanguage.getIsoCode().toUpperCase());
			if(localLanguage != null) {
				localLanguage.setPrestaShopId(remoteLanguage.getId());
			}
		}
	}

	@Override
	@Transactional
	public void importOrderStatuses(Language prestashopLanguage, PSWebServiceClient ws) throws PrestaShopWebserviceException {
		final List<PrestashopOrderStatus> remoteStatuses = ws.fetchAll(PrestashopResourceType.ORDER_STATUSES);
		final List<PrestashopOrderStatusCacheEntry> localStatuses = orderStatusRepository.all().fetch();
		final Map<Integer, PrestashopOrderStatusCacheEntry> statusesById = new HashMap<>();
		for(PrestashopOrderStatusCacheEntry e : localStatuses) {
			statusesById.put(e.getPrestaShopId(), e);
		}


		for(PrestashopOrderStatus remoteStatus : remoteStatuses) {
			if(statusesById.remove(remoteStatus.getId()) == null) {
				PrestashopOrderStatusCacheEntry entry = new PrestashopOrderStatusCacheEntry();
				entry.setPrestaShopId(remoteStatus.getId());
				entry.setName(remoteStatus.getName().getTranslation(prestashopLanguage.getPrestaShopId() == null ? 1 : prestashopLanguage.getPrestaShopId())); // TODO Handle language correctly
				entry.setDelivered(remoteStatus.getDelivered());
				entry.setInvoiced(remoteStatus.getInvoiced());
				entry.setShipped(remoteStatus.getShipped());
				entry.setPaid(remoteStatus.getPaid());
				orderStatusRepository.save(entry);
			}
		}
	}

}
