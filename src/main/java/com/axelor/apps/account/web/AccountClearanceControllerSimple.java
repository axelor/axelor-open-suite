package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class AccountClearanceControllerSimple {

	public void showAccountClearanceMoveLines(ActionRequest request, ActionResponse response)  {
		
		Map<String,Object> viewMap = new HashMap<String,Object>();
		
		Context context = request.getContext();
		viewMap.put("title", "Lignes d'écriture générées");
		viewMap.put("resource", MoveLine.class.getName());
		viewMap.put("domain", "self.accountClearance.id = "+context.get("id"));
		response.setView(viewMap);
	}
}
