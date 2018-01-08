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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryActionService;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class BatchDebtRecovery extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private List<DebtRecovery> changedDebtRecoveries = new ArrayList<>();

	protected boolean stopping = false;
	protected PartnerRepository partnerRepository;
	
	@Inject
	public BatchDebtRecovery(DebtRecoveryService debtRecoveryService, PartnerRepository partnerRepository) {
		
		super(debtRecoveryService);
		this.partnerRepository = partnerRepository;
	}


	@Override
	protected void start() throws IllegalAccessException, AxelorException {
		
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
				
		try {
			
			debtRecoveryService.testCompanyField(company);
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(e, e.getCategory(), ""), IException.DEBT_RECOVERY, batch.getId());
			incrementAnomaly();
			stopping = true;
		}
		
		checkPoint();

	}
	
	
	@Override
	protected void process() {
		
		if(!stopping)  {
			
			this.debtRecoveryPartner();
		
			this.generateMail();
		}
	}
	
	
	public void debtRecoveryPartner()  {

		List<Long> anomalyList = Lists.newArrayList(0L);
		List<Long> notRemindedList = Lists.newArrayList(0L);
		Company company = batch.getAccountingBatch().getCompany();
		Query<Partner> query = partnerRepository
				.all()
				.filter("self.isContact = false " +
						"AND :_company MEMBER OF self.companySet " +
						"AND self.accountingSituationList IS NOT EMPTY " +
						"AND self.isCustomer = true " +
						"AND :_batch NOT MEMBER OF self.batchSet " +
						"AND self.id NOT IN (:anomalyList) " +
						"AND self.id NOT IN (:notRemindedList)")
				.bind("_company", company)
				.bind("anomalyList", anomalyList)
				.bind("notRemindedList", notRemindedList)
				.bind("_batch", batch);


		for (List<Partner> partnerList; !(partnerList = query.fetch(FETCH_LIMIT)).isEmpty(); JPA.clear()) {
			for (Partner partner : partnerList) {

				try {
					partner = partnerRepository.find(partner.getId());
					boolean remindedOk = debtRecoveryService.debtRecoveryGenerate(partner, company);

					if (remindedOk) {
						updatePartner(partner);
						changedDebtRecoveries.add(
								debtRecoveryService.getDebtRecovery(partner, company)
						);
					}
					else {
					    notRemindedList.add(partner.getId());
					}

					log.debug("Tiers traité : {}", partner.getName());

				} catch (AxelorException e) {

					TraceBackService.trace(new AxelorException(e, e.getCategory(), I18n.get("Partner") + " %s", partner.getName()), IException.DEBT_RECOVERY, batch.getId());
					incrementAnomaly();

				} catch (Exception e) {

					TraceBackService.trace(new Exception(String.format(I18n.get("Partner") + " %s", partner.getName()), e), IException.DEBT_RECOVERY, batch.getId());
					anomalyList.add(partner.getId());
					incrementAnomaly();

					log.error("Bug(Anomalie) généré(e) pour le tiers {}", partner.getName());

				}
			}
		}
	}
	
	

	void generateMail() {
		for (DebtRecovery debtRecovery : changedDebtRecoveries) {
			try {
				debtRecovery = Beans.get(DebtRecoveryRepository.class).find(debtRecovery.getId());
				DebtRecoveryHistory debtRecoveryHistory = Beans.get(DebtRecoveryActionService.class).getDebtRecoveryHistory(debtRecovery);
				if (debtRecoveryHistory == null) {
					continue;
				}
				if (debtRecoveryHistory.getDebtRecoveryMessageSet() == null
						|| debtRecoveryHistory.getDebtRecoveryMessageSet().isEmpty()) {
					Beans.get(DebtRecoveryActionService.class).runMessage(debtRecovery);
				}
			} catch (Exception e) {
				TraceBackService.trace(new Exception(String.format(I18n.get("Tiers")+" %s", debtRecovery.getAccountingSituation().getPartner().getName()), e), IException.REMINDER, batch.getId());

				incrementAnomaly();
			}
		}
	}

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_DEBT_RECOVERY_1);
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_DEBT_RECOVERY_2)+"\n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
