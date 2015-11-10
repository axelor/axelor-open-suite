package com.axelor.apps.base.db.repo;

import java.util.Map;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PartnerBaseRepository extends PartnerRepository {
	
	@Inject
	PartnerService partnerService;
	
	@Override
	public Partner save(Partner partner) {
		try {

			if (partner.getPartnerSeq() == null){
				String seq = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.PARTNER);
				if (seq == null)
					throw new AxelorException(I18n.get(IExceptionMessage.PARTNER_1),
							IException.CONFIGURATION_ERROR);
				partner.setPartnerSeq(seq);
			}

			return super.save(partner);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
	
	@Override
	public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
		if (!context.containsKey("json-enhance")) {
			return json;
		}
		try {
			Long id = (Long) json.get("id");
			Partner partner = find(id);
			json.put("address", partnerService.getDefaultAddress(partner));
		} catch (Exception e) {
			e.printStackTrace();
	
		}

		return json;
		
	}
}
