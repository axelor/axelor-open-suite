/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PayerQualityService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected PartnerService partnerService;
	protected GeneralService generalService;
	protected PartnerRepository partnerRepository;
	
	protected LocalDate today;
	
	@Inject
	public PayerQualityService(PartnerService partnerService, GeneralService generalService, PartnerRepository partnerRepository) {

		this.partnerService = partnerService;
		this.generalService = generalService;
		this.partnerRepository = partnerRepository;
		this.today = generalService.getTodayDate();

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
		
		MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);
		
		return moveLineRepo.all().filter("self.partner = ?1 AND self.date > ?2 AND self.interbankCodeLine IS NOT NULL", partner, today.minusYears(1)).fetch();
	}


	public BigDecimal getPayerQualityNote(Partner partner, List<PayerQualityConfigLine> payerQualityConfigLineList)  {
		BigDecimal burden = BigDecimal.ZERO;

		List<ReminderHistory> reminderHistoryList = this.getReminderHistoryList(partner);
		List<MoveLine> moveLineList = this.getMoveLineRejectList(partner);

		log.debug("Tiers {} : Nombre de relance concernée : {}",partner.getName(), reminderHistoryList.size());
		log.debug("Tiers {} : Nombre de rejets concernée : {}", partner.getName(), moveLineList.size());

		for(ReminderHistory reminderHistory : reminderHistoryList)  {
			burden = burden.add(this.getPayerQualityNote(reminderHistory, payerQualityConfigLineList));
		}
		for(MoveLine moveLine : moveLineList)  {
			burden = burden.add(this.getPayerQualityNote(moveLine, payerQualityConfigLineList));
		}
		log.debug("Tiers {} : Qualité payeur : {}", partner.getName(), burden);
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
		return  partnerRepository.all().filter("self.isCustomer = true and self.hasOrdered = true").fetch();
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void payerQualityProcess() throws AxelorException  {
		List<PayerQualityConfigLine> payerQualityConfigLineList = generalService.getGeneral().getPayerQualityConfigLineList();
		if(payerQualityConfigLineList == null || payerQualityConfigLineList.size() == 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYER_QUALITY_1),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}

		List<Partner> partnerList = this.getPartnerList();
		if(partnerList != null && partnerList.size() != 0)  {
			for(Partner partner : partnerList)  {
				BigDecimal burden = this.getPayerQualityNote(partner, payerQualityConfigLineList);

				if(burden.compareTo(BigDecimal.ZERO) == 1)  {
					partner.setPayerQuality(burden);
					partnerRepository.save(partner);
					log.debug("Tiers payeur {} : Qualité payeur : {}",partner.getName(), burden);
				}
			}
		}
	}

}
