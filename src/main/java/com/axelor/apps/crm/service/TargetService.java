/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import java.math.BigDecimal;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.ITarget;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.Target;
import com.axelor.apps.crm.db.TargetConfiguration;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.persist.Transactional;

public class TargetService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TargetService.class);
	
	public void createsTargets(TargetConfiguration targetConfiguration) throws AxelorException  {
		
		if(targetConfiguration.getPeriodTypeSelect() == ITarget.NONE)  {
			Target target = this.createTarget(targetConfiguration, targetConfiguration.getFromDate(), targetConfiguration.getToDate());
			
			this.update(target);
		}
		
		else  {
			
			LocalDate oldDate = targetConfiguration.getFromDate();
			
			for(LocalDate date = oldDate ; date.isBefore(targetConfiguration.getToDate()) || date.isEqual(targetConfiguration.getToDate()); date = this.getNextDate(targetConfiguration.getPeriodTypeSelect(), date))  {
			
				Target target2 = Target.all().filter("self.userInfo = ?1 AND self.team = ?2 AND self.periodTypeSelect = ?3 AND self.fromDate >= ?4 AND self.toDate <= ?5 AND " +
						"((self.callEmittedNumberTarget > 0 AND ?6 > 0) OR (self.meetingNumberTarget > 0 AND ?7 > 0) OR " +
						"(self.opportunityAmountWonTarget > 0.00 AND ?8 > 0.00) OR (self.opportunityCreatedNumberTarget > 0 AND ?9 > 0) OR (self.opportunityCreatedWonTarget > 0 AND ?10 > 0))", 
						targetConfiguration.getUserInfo(), targetConfiguration.getTeam(), targetConfiguration.getPeriodTypeSelect(), targetConfiguration.getFromDate(), targetConfiguration.getToDate(),
						targetConfiguration.getCallEmittedNumber(), targetConfiguration.getMeetingNumber(),
						targetConfiguration.getOpportunityAmountWon().doubleValue(), targetConfiguration.getOpportunityCreatedNumber(), targetConfiguration.getOpportunityCreatedWon()).fetchOne(); 
				
				if(target2 == null)  {
					Target target = this.createTarget(targetConfiguration, oldDate, date.minusDays(1));
				
					this.update(target);
				
					oldDate = date;
				}
				else {
					throw new AxelorException(String.format("L'objectif %s est en contradiction avec la configuration d'objectif %s", 
							target2.getCode(), targetConfiguration.getCode()), IException.CONFIGURATION_ERROR);
				}
			}
		}
	}
	
	
	public LocalDate getNextDate(int periodTypeSelect, LocalDate date)   {
		
		switch (periodTypeSelect) {
		case ITarget.NONE:
			return date;
		case ITarget.MONTHLY:
			return date.plusMonths(1);
		case ITarget.WEEKLY:
			return date.plusWeeks(1);
		case ITarget.DAILY:
			return date.plusDays(1);

		default:
			return date;
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Target createTarget(TargetConfiguration targetConfiguration, LocalDate fromDate, LocalDate toDate)  {
		Target target = new Target();
		target.setCallEmittedNumberTarget(targetConfiguration.getCallEmittedNumber());
		target.setMeetingNumberTarget(targetConfiguration.getMeetingNumber());
		target.setOpportunityAmountWonTarget(targetConfiguration.getOpportunityAmountWon());
		target.setOpportunityCreatedNumberTarget(target.getOpportunityCreatedNumberTarget());
		target.setOpportunityCreatedWonTarget(target.getOpportunityCreatedWonTarget());
//		target.setSalesOrderAmountWonTarget(targetConfiguration.getSalesOrderAmountWon());
//		target.setSalesOrderCreatedNumberTarget(targetConfiguration.getSalesOrderCreatedNumber());
//		target.setSalesOrderCreatedWonTarget(targetConfiguration.getSalesOrderCreatedWon());
		target.setPeriodTypeSelect(targetConfiguration.getPeriodTypeSelect());
		target.setFromDate(fromDate);
		target.setToDate(toDate);
		target.setUserInfo(targetConfiguration.getUserInfo());
		target.setTeam(targetConfiguration.getTeam());
		target.setName(targetConfiguration.getName());
		target.setCode(targetConfiguration.getCode());
		return target.save();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void update(Target target)  {
		UserInfo userInfo = target.getUserInfo();
		Team team = target.getTeam();
		LocalDate fromDate = target.getFromDate();
		LocalDate toDate = target.getToDate();
		
		LocalDateTime fromDateTime = new LocalDateTime(fromDate.getYear(), fromDate.getMonthOfYear(), fromDate.getDayOfMonth(), 0, 0);
		LocalDateTime toDateTime = new LocalDateTime(toDate.getYear(), toDate.getMonthOfYear(), toDate.getDayOfMonth(), 23, 59);
		
		if(userInfo != null)  {
			Query q = JPA.em().createQuery("select SUM(op.amount) FROM Opportunity as op WHERE op.userInfo = ?1 AND op.salesStageSelect = 9 AND op.createdOn >= ?2 AND op.createdOn <= ?3 ");
			q.setParameter(1, userInfo);
			q.setParameter(2, fromDateTime);
			q.setParameter(3, toDateTime);
					
			BigDecimal opportunityAmountWon = (BigDecimal) q.getSingleResult();
			
			Long callEmittedNumber = Event.all().filter("self.typeSelect = ?1 AND self.userInfo = ?2 AND self.startDateTime >= ?3 AND self.endDateTime <= ?4 AND self.callStatusSelect = 2",
					1, userInfo, fromDateTime, toDateTime).count();
			
			target.setCallEmittedNumber(callEmittedNumber.intValue());
			
			Long meetingNumber = Event.all().filter("self.typeSelect = ?1 AND self.userInfo = ?2 AND self.startDateTime >= ?3 AND self.endDateTime <= ?4",
					1, userInfo, fromDateTime, toDateTime).count();
			
			target.setMeetingNumber(meetingNumber.intValue());
			
			
			target.setOpportunityAmountWon(opportunityAmountWon);
			
			Long opportunityCreatedNumber = Opportunity.all().filter("self.userInfo = ?1 AND self.createdOn >= ?2 AND self.createdOn <= ?3",
					userInfo, fromDateTime, toDateTime).count();
			
			target.setOpportunityCreatedNumber(opportunityCreatedNumber.intValue());
			
			Long opportunityCreatedWon = Opportunity.all().filter("self.userInfo = ?1 AND self.createdOn >= ?2 AND self.createdOn <= ?3 AND self.salesStageSelect = 9",
					userInfo, fromDateTime, toDateTime).count();
			
			target.setOpportunityCreatedWon(opportunityCreatedWon.intValue());
		}
		else if(team != null)  {
			
			Query q = JPA.em().createQuery("select SUM(op.amount) FROM Opportunity as op WHERE op.team = ?1 AND op.salesStageSelect = 9  AND op.createdOn >= ?2 AND op.createdOn <= ?3 ");
			q.setParameter(1, team);
			q.setParameter(2, fromDateTime);
			q.setParameter(3, toDateTime);
					
			BigDecimal opportunityAmountWon = (BigDecimal) q.getResultList();
			
			Long callEmittedNumber = Event.all().filter("self.typeSelect = ?1 AND self.team = ?2 AND self.startDateTime >= ?3 AND self.endDateTime <= ?4 AND self.callStatusSelect = 2",
					1, userInfo, fromDateTime, toDateTime).count();
			
			target.setCallEmittedNumber(callEmittedNumber.intValue());
			
			Long meetingNumber = Event.all().filter("self.typeSelect = ?1 AND self.team = ?2 AND self.startDateTime >= ?3 AND self.endDateTime <= ?4",
					1, userInfo, fromDateTime, toDateTime).count();
			
			target.setMeetingNumber(meetingNumber.intValue());
			
			
			target.setOpportunityAmountWon(opportunityAmountWon);
			
			Long opportunityCreatedNumber = Opportunity.all().filter("self.team = ?1 AND self.createdOn >= ?2 AND self.createdOn <= ?3",
					userInfo, fromDateTime, toDateTime).count();
			
			target.setOpportunityCreatedNumber(opportunityCreatedNumber.intValue());
			
			Long opportunityCreatedWon = Opportunity.all().filter("self.team = ?1 AND self.createdOn >= ?2 AND self.createdOn <= ?3 AND self.salesStageSelect = 9",
					userInfo, fromDateTime, toDateTime).count();
			
			target.setOpportunityCreatedWon(opportunityCreatedWon.intValue());
		}
		
		target.save();
		
	}
	
	
}
