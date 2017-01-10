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
package com.axelor.apps.hr.web.lunch.voucher;


import java.io.IOException;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LunchVoucherMgtController {
	
	@Inject
	private Provider<LunchVoucherMgtService> lunchVoucherMgtProvider;
	@Inject
	private Provider<LunchVoucherMgtRepository> lunchVoucherMgtRepositoryProvider;
	
	@Inject
	private Provider<HRConfigService> hrConfigService;
	
	
	public void calculate(ActionRequest request, ActionResponse response)  {
		
		try {
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
			lunchVoucherMgt = lunchVoucherMgtRepositoryProvider.get().find(lunchVoucherMgt.getId());
			lunchVoucherMgtService.calculate(lunchVoucherMgt);
			
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}
	
	public void checkStock(ActionRequest request, ActionResponse response)  {
		try {
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
			Company company = lunchVoucherMgt.getCompany();
			HRConfig hrConfig = hrConfigService.get().getHRConfig(company);
			int stock = lunchVoucherMgtService.checkStock(lunchVoucherMgt);
			if (stock <= 0){ 
				response.setAlert(String.format(I18n.get(IExceptionMessage.LUNCH_VOUCHER_MIN_STOCK),company.getName(),
						hrConfig.getMinStockLunchVoucher(), hrConfig.getAvailableStockLunchVoucher(), IException.INCONSISTENCY));
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void updateTotal(ActionRequest request, ActionResponse response)  {
		
		try {
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
			lunchVoucherMgt = lunchVoucherMgtRepositoryProvider.get().find(lunchVoucherMgt.getId());
			lunchVoucherMgtService.calculateTotal(lunchVoucherMgt);
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}
	
	public void exportLunchVoucherMgt(ActionRequest request, ActionResponse response) throws IOException {
		LunchVoucherMgt lunchVoucherMgt = Beans.get(LunchVoucherMgtRepository.class).find(request.getContext().asType(LunchVoucherMgt.class).getId());
		try {
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			response.setExportFile(lunchVoucherMgtService.exportLunchVoucherMgt(lunchVoucherMgt));
			response.setReload(true);
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		
		
	}
}
