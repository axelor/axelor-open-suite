/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.sale.service;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.StopReason;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.google.inject.persist.Transactional;

public class SalesRuleService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesRuleService.class); 
	
	@Inject
	private AlarmEngineService<Partner> alarmEngineService;
	
	@Inject
	private MailService mailService;
	
	private StopReason stopReason;
	private UserInfo userInfo;

	@Inject
	public SalesRuleService(UserInfoService userInfoService) {
		
		this.userInfo = userInfoService.getUserInfo();
		
	}
	
	
	protected void actionAlarm( AlarmEngine alarmEngine, Partner partner ) {
		
		Alarm alarm = alarmEngineService.createAlarm(alarmEngine, partner);
		alarm.setPartner(partner);
		
	}
	
	// TRANSACTIONNAL
	@Transactional
	public void actionInvoicingBlocking(Partner partner) {
		if(partner.getBlocking()!=null)  {
			Blocking blocking = partner.getBlocking();
			blocking.setInvoicingBlockingOk(true);
			blocking.setInvoicingBlockingByUserInfo(userInfo);
			blocking.setInvoicingBlockingReason(stopReason);
			
			blocking.save();
		}
	}

	
	// Traitement par lot
	
	protected List<Partner> getPartners( ){
		
		LOG.debug("Récupération des tiers");
		
		return Partner.all().fetch();
		
	}

}
