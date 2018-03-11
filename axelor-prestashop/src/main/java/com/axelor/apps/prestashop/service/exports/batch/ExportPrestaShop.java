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
package com.axelor.apps.prestashop.service.exports.batch;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.prestashop.batch.PrestaShopBatchService;
import com.axelor.apps.prestashop.db.PrestaShopBatch;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.exports.PrestaShopServiceExport;
import com.axelor.i18n.I18n;

public class ExportPrestaShop extends AbstractBatch {
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private PrestaShopServiceExport prestaShopServiceExport;
	private AppPrestashopRepository appRepository;
	private PrestaShopBatchService batchService;

	@Inject
	public ExportPrestaShop(PrestaShopServiceExport prestaShopServiceExport, AppPrestashopRepository appRepository, PrestaShopBatchService batchService) {
		this.prestaShopServiceExport = prestaShopServiceExport;
		this.appRepository = appRepository;
		this.batchService = batchService;
	}

	@Override
	protected void process() {
		try {
			PrestaShopBatch prestaShopBatch = (PrestaShopBatch) model;

			ZonedDateTime referenceDate = batchService.getLastSuccessfullRunStartDate(prestaShopBatch);
			if(LOG.isDebugEnabled()) {
				LOG.debug("Starting export from ABS to PrestaShop with reference date {}", referenceDate);
			}
			prestaShopServiceExport.export(appRepository.all().fetchOne(), referenceDate, batch);

			checkPoint(); // cannot call save directly as we've no transaction
			incrementDone();
		} catch (Exception e) {
			LOG.error(String.format("An error occured while running prestashop export batch #%d", batch.getId()), e);
			incrementAnomaly();
		}
	}

	@Override
	protected void stop() {
		super.stop();
		addComment(I18n.get(IExceptionMessage.BATCH_EXPORT));
	}
}
