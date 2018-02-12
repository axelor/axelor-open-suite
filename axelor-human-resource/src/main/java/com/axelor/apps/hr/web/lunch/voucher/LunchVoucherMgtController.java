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
package com.axelor.apps.hr.web.lunch.voucher;


import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtLineRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LunchVoucherMgtController {
	
	@Inject private Provider<LunchVoucherMgtService> lunchVoucherMgtProvider;
	
	@Inject private Provider<HRConfigService> hrConfigService;
	
	public void calculate(ActionRequest request, ActionResponse response) {
		
		try {
			LunchVoucherMgt lunchVoucherMgt = Beans.get(LunchVoucherMgtRepository.class).find(request.getContext().asType(LunchVoucherMgt.class).getId());
			lunchVoucherMgtProvider.get().calculate(lunchVoucherMgt);
			
			response.setReload(true);
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void checkStock(ActionRequest request, ActionResponse response)  {
		try {
			LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
			Company company = lunchVoucherMgt.getCompany();
			HRConfig hrConfig = hrConfigService.get().getHRConfig(company);
			int stock = lunchVoucherMgtProvider.get().checkStock(company, lunchVoucherMgt.getStockLineQuantity() + lunchVoucherMgt.getTotalLunchVouchers());
			
			if (stock <= 0){ 
				response.setAlert(String.format(I18n.get(IExceptionMessage.LUNCH_VOUCHER_MIN_STOCK),company.getName(),
						hrConfig.getMinStockLunchVoucher(), hrConfig.getAvailableStockLunchVoucher(), IException.INCONSISTENCY));
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void validate(ActionRequest request, ActionResponse response) {
			LunchVoucherMgt lunchVoucherMgt = Beans.get(LunchVoucherMgtRepository.class).find(request.getContext().asType(LunchVoucherMgt.class).getId());
		try {
			lunchVoucherMgtProvider.get().validate(lunchVoucherMgt);

			response.setReload(true);

		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
			return;
		}

		try {
			Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getPayPeriod());
			Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getLeavePeriod());
		} catch (AxelorException e) {
			response.setFlash(e.getMessage());
		}
	}
	
	public void updateTotal(ActionRequest request, ActionResponse response) {
		LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
		LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
		lunchVoucherMgtService.calculateTotal(lunchVoucherMgt);
		
		response.setValue("totalLunchVouchers", lunchVoucherMgt.getTotalLunchVouchers());
		response.setValue("requestedLunchVouchers", lunchVoucherMgt.getRequestedLunchVouchers());
		response.setValue("givenLunchVouchers", lunchVoucherMgt.getGivenLunchVouchers());
	}
	
	public void export(ActionRequest request, ActionResponse response) throws IOException {
		LunchVoucherMgt lunchVoucherMgt = Beans.get(LunchVoucherMgtRepository.class).find(request.getContext().asType(LunchVoucherMgt.class).getId());
		
		try {
/*
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			lunchVoucherMgtService.exportLunchVoucherMgt(lunchVoucherMgt);
*/
			lunchVoucherMgtProvider.get().export(lunchVoucherMgt);
			response.setReload(true);
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		
	}
	
	public void print(ActionRequest request, ActionResponse response) throws IOException {
		LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
		
		String name =  lunchVoucherMgt.getCompany().getName() + " - " + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		
		try {
			String fileLink = ReportFactory.createReport(IReport.LUNCH_VOUCHER_MGT_MONTHLY, name)
					.addParam("lunchVoucherMgtId", lunchVoucherMgt.getId())
					.addParam("Locale", Beans.get(AppBaseService.class).getAppBase().getDefaultPartnerLanguage())
					.addFormat(ReportSettings.FORMAT_PDF)
					.generate()
					.getFileLink();
			
			response.setView(ActionView
					.define(name)
					.add("html", fileLink).map());
			
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}
	public void updateStock(ActionRequest request, ActionResponse response) {
		try {
			LunchVoucherMgtService lunchVoucherMgtService = lunchVoucherMgtProvider.get();
			LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
			if (lunchVoucherMgt.getId() == null) {
				return;
			}
			List<LunchVoucherMgtLine> oldLunchVoucherLines =
					Beans.get(LunchVoucherMgtLineRepository.class).all()
							.filter("self.lunchVoucherMgt.id = ?", lunchVoucherMgt.getId())
							.fetch();
			int stockQuantityStatus = lunchVoucherMgtService.updateStock(lunchVoucherMgt.getLunchVoucherMgtLineList(),
					oldLunchVoucherLines, lunchVoucherMgt.getCompany());
			response.setValue("stockQuantityStatus", stockQuantityStatus);
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
}
