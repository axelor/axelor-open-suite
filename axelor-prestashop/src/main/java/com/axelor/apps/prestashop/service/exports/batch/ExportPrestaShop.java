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

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.prestashop.db.PrestaShopBatch;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.exports.PrestaShopServiceExport;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class ExportPrestaShop extends AbstractBatch {
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private PrestaShopServiceExport prestaShopServiceExport;

	@Inject
	public ExportPrestaShop(PrestaShopServiceExport prestaShopServiceExport) {
		this.prestaShopServiceExport = prestaShopServiceExport;
	}

	@Override
	@Transactional
	protected void process() {

			try {
				PrestaShopBatch prestaShopBatch = (PrestaShopBatch) model;
				Integer size = prestaShopBatch.getBatchList().size();
				Batch batchObj;

				if(size == 1) {
					batchObj = prestaShopServiceExport.exportPrestShop(null, batch);
				} else {
					ZonedDateTime endDate = prestaShopBatch.getBatchList().get(size - 2).getEndDate();
					batchObj = prestaShopServiceExport.exportPrestShop(endDate, batch);
				}
				batchRepo.save(batchObj);
			} catch (Exception e) {

				incrementAnomaly();
				LOG.error("An error occured while running prestashop export batch #{}", batch.getId());
			}
	}

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_EXPORT);

		super.stop();
		addComment(comment);
	}
}
