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
package com.axelor.apps.account.service.debtrecovery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayerQualityConfigLine;
import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.ReminderHistory;
import com.axelor.apps.account.db.ReminderLevel;
import com.axelor.apps.account.db.ReminderMethodLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PayerQualityService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PayerQualityService.class); 

	private LocalDate today;

	@Inject
	public PayerQualityService() {
		
		this.today = GeneralService.getTodayDate();
		
	}
	
	//TODO : à remplacer par une requête afin de rendre le traitement scalable
	public List<ReminderHistory> getReminderHistoryList(Partner partner)  {
		List<ReminderHistory> reminderHistoryList = new ArrayList<ReminderHistory>();
		if(partner.getAccountingSituationList() != null)  {
			for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {
				Reminder reminder = accountingSituation.getReminder();
				if(reminder != null && reminder.getReminderHistoryList()!= null && !reminder.getReminderHistoryList().isEmpty())  {
					for(ReminderHistory reminderHistory : reminder.getReminderHistoryList())  {
						if((reminderHistory.getReminderDate() != null && reminderHistory.getReminderDate().isAfter(today.minusYears(1))))  {
							reminderHistoryList.add(reminderHistory);
						}
					}
				}
			}
		}
		return reminderHistoryList;
	}
	
	
	public List<MoveLine> getMoveLineRejectList(Partner partner)  {
		return MoveLine.all().filter("self.partner = ?1 AND self.date > ?2 AND self.interbankCodeLine IS NOT NULL", partner, today.minusYears(1)).fetch();
	}
	
	
	public BigDecimal getPayerQualityNote(Partner partner, List<PayerQualityConfigLine> payerQualityConfigLineList)  {
		BigDecimal burden = BigDecimal.ZERO;
		
		List<ReminderHistory> reminderHistoryList = this.getReminderHistoryList(partner); 
		List<MoveLine> moveLineList = this.getMoveLineRejectList(partner);
		
		LOG.debug("Tiers {} : Nombre de relance concernée : {}",partner.getName(), reminderHistoryList.size());
		LOG.debug("Tiers {} : Nombre de rejets concernée : {}", partner.getName(), moveLineList.size());
		
		for(ReminderHistory reminderHistory : reminderHistoryList)  {
			burden = burden.add(this.getPayerQualityNote(reminderHistory, payerQualityConfigLineList));
		}
		for(MoveLine moveLine : moveLineList)  {
			burden = burden.add(this.getPayerQualityNote(moveLine, payerQualityConfigLineList));
		}	
		LOG.debug("Tiers {} : Qualité payeur : {}", partner.getName(), burden);
		return burden;
	}
	
	
	public BigDecimal getPayerQualityNote(ReminderHistory reminderHistory, List<PayerQualityConfigLine> payerQualityConfigLineList)  {
		ReminderLevel reminderLevel = this.getReminderLevel(reminderHistory);
		if(reminderLevel != null)  {
			for(PayerQualityConfigLine payerQualityConfigLine : payerQualityConfigLineList)  {
				if(payerQualityConfigLine.getIncidentTypeSelect() == 0 
						&& payerQualityConfigLine.getReminderLevel().equals(reminderLevel))  {
					return payerQualityConfigLine.getBurden();
				}
			}
		}		
		return BigDecimal.ZERO;
	}
	
	
	public BigDecimal getPayerQualityNote(MoveLine moveLine, List<PayerQualityConfigLine> payerQualityConfigLineList)  {
		for(PayerQualityConfigLine payerQualityConfigLine : payerQualityConfigLineList)  {
			if(payerQualityConfigLine.getIncidentTypeSelect() == 1
					&& !moveLine.getInterbankCodeLine().getTechnicalRejectOk())  {
				return payerQualityConfigLine.getBurden();
			}
		}
		return BigDecimal.ZERO;
	}
	
	
	public ReminderLevel getReminderLevel(ReminderHistory reminderHistory)  {
		ReminderMethodLine reminderMethodLine = null;
	
		if(reminderHistory.getReminderDate() != null)  {
			
			reminderMethodLine = reminderHistory.getReminderMethodLine();
				
		}
		
		if(reminderMethodLine != null)  {
			return reminderMethodLine.getReminderLevel();
		}
		else  {
			return null;
		}
	}
	
	
	public List<Partner> getPartnerList()  {
		return  Partner.all().filter("self.payerOk = 'true'").fetch();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void payerQualityProcess() throws AxelorException  {
		List<PayerQualityConfigLine> payerQualityConfigLineList = GeneralService.getGeneral().getPayerQualityConfigLineList();
		if(payerQualityConfigLineList == null || payerQualityConfigLineList.size() == 0)  {
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer un tableau des poids dans l'administration générale",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}	
		
		List<Partner> partnerList = this.getPartnerList();
		if(partnerList != null && partnerList.size() != 0)  {
			for(Partner partner : partnerList)  {
				BigDecimal burden = this.getPayerQualityNote(partner, payerQualityConfigLineList);
				
				if(burden.compareTo(BigDecimal.ZERO) == 1)  {
					partner.setPayerQuality(burden);
					partner.save();
					LOG.debug("Tiers payeur {} : Qualité payeur : {}",partner.getName(), burden);
				}
			}
		}
	}
	
}
