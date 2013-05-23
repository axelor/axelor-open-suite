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
