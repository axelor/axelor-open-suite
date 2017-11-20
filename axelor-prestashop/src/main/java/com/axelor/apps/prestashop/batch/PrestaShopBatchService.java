/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.prestashop.batch;


import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.db.IPrestaShopBatch;
import com.axelor.apps.prestashop.db.PrestaShopBatch;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.exports.batch.ExportPrestaShop;
import com.axelor.apps.prestashop.service.imports.batch.ImportPrestaShop;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

/**
 * InvoiceBatchService est une classe implémentant l'ensemble des batchs de
 * comptabilité et assimilé.
 * 
 * @author Geoffrey DUBAUX
 * 
 * @version 0.1
 */
public class PrestaShopBatchService extends AbstractBatchService {

// Appel 	
	
	/**
	 * Lancer un batch à partir de son code.
	 * 
	 * @param batchCode
	 * 		Le code du batch souhaité.
	 * 
	 * @throws AxelorException
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Batch run(Model batchCode) throws AxelorException {
				
		Batch batch;
		PrestaShopBatch prestaShopBatch = (PrestaShopBatch) batchCode;
		
		if (prestaShopBatch != null){
			switch (prestaShopBatch.getActionSelect()) {
			
			case IPrestaShopBatch.BATCH_IMPORT:
				batch = importPrestaShop(prestaShopBatch);
				break;
				
			case IPrestaShopBatch.BATCH_EXPORT:
				batch = exportPrestaShop(prestaShopBatch);
				break;
				
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PRESTASHOP_BATCH_1), prestaShopBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PRESTASHOP_BATCH_2), batchCode), IException.INCONSISTENCY);
		}
		return batch;
	}
	
	/**
	 * Batch run import prestashop to ABS
	*/
	public Batch importPrestaShop(PrestaShopBatch prestaShopBatch) {

		return Beans.get(ImportPrestaShop.class).run(prestaShopBatch);
	}
	
	/**
	 * Batch run export ABS to prestashop
	*/
	public Batch exportPrestaShop(PrestaShopBatch prestaShopBatch) {
		
		return Beans.get(ExportPrestaShop.class).run(prestaShopBatch);
	}

	@Override
	protected Class<? extends Model> getModelClass() {
		return PrestaShopBatch.class;
	}
}
