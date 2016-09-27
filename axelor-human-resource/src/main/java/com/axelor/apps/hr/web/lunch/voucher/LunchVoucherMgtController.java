package com.axelor.apps.hr.web.lunch.voucher;

import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LunchVoucherMgtController {
	
	@Inject
	private Provider<LunchVoucherMgtService> lunchVoucherMgtProvider;
	@Inject
	private Provider<LunchVoucherMgtRepository> lunchVoucherMgtRepositoryProvider;
	
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
}
