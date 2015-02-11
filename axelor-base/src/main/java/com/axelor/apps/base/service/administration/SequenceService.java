/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.administration;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.internal.oxm.schema.model.All;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SequenceService extends SequenceRepository {

	private static final Logger LOG = LoggerFactory.getLogger(SequenceService.class); 

	private LocalDate today, refDate;

	@Inject
	public SequenceService() {
		
		this.today = GeneralService.getTodayDate();
		this.refDate = this.today;
		
	}
	
	public SequenceService(LocalDate today) { 
		this.today = today;
		this.refDate = this.today; 
	}
	
	public SequenceService setRefDate( LocalDate refDate ){
		this.refDate = refDate;
		return this;
	}
	
	/**
	 * Retourne une sequence en fonction du code
	 * @return
	 */
	public Sequence getSequence(String code) {
		return getSequence(code, null);
	}	
	
	
	/**
	 * Retourne une sequence en fonction du code, de la sté 
	 * 
	 * @return
	 */
	public Sequence getSequence(String code, Company company) {
	
		if (code == null)  {
			return null;
		}	
			
		if (company == null)  {
			return findByCode(code);
		}
		else {
			return all().filter("self.company = ?1 and self.code = ?2", company, code).fetchOne();
		}
	
	}
	
	/**
	 * Retourne une sequence en fonction du code, de la sté 
	 * 
	 * @return
	 */
	public String getSequenceNumber(String code) {
	
		return this.getSequenceNumber(code, null);
			
	}
	
	
	/**
	 * Retourne une sequence en fonction du code, de la sté 
	 * 
	 * @return
	 */
	public String getSequenceNumber(String code, Company company) {
	
		Sequence sequence = this.getSequence(code, company);
		
		if(sequence == null)  {  return null;  }
		
		return this.getSequenceNumber(sequence);
			
	}
	
	
	
	/**
	 * Retourne une sequence en fonction du code, de la sté 
	 * 
	 * @return
	 */
	public boolean hasSequence(String code, Company company) {
	
		if (this.getSequence(code, company) != null)  {
			return true;
		}
		
		return false;
			
	}
	
	/**
	 * Fonction retournant une numéro de séquence depuis une séquence générique, et une date
	 *  
	 * @param seq
	 * @param todayYear
	 * @param todayMoy
	 * @param todayDom
	 * @param todayWoy
	 * @return
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String getSequenceNumber( Sequence sequence )  {
		
		reset(sequence);
		
		int todayYear = today.getYearOfCentury(), 
			todayMoy = today.getMonthOfYear(), 
			todayDom = today.getDayOfMonth(), 
			todayWoy = today.getWeekOfWeekyear();
		
		String seqPrefixe = "";
		String seqSuffixe = "";
		
		if (sequence.getPrefixe() != null)  {
			seqPrefixe = ((((sequence.getPrefixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
		}	
		if (sequence.getSuffixe() != null){
			seqSuffixe = ((((sequence.getSuffixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
		}
		
		String padLeft = StringUtils.leftPad(sequence.getNextNum().toString(), sequence.getPadding(), "0");
		
		String nextSeq = seqPrefixe + padLeft + seqSuffixe;
		
		LOG.debug("nextSeq : : : : {}",nextSeq);	
		
		sequence.setNextNum(sequence.getNextNum() + sequence.getToBeAdded());
		save(sequence);
		return nextSeq;
	}
	
	private boolean reset( Sequence sequence ){
		
		if ( this.refDate == null ) { return false; }
		if ( !sequence.getYearlyResetOk() && !sequence.getMonthlyResetOk() ){ return false; }
		if ( !this.refDate.isAfter( sequence.getResetDate() ) ) { return false; }
		if ( sequence.getYearlyResetOk() && !this.refDate.equals( this.refDate.monthOfYear().withMinimumValue().dayOfMonth().withMinimumValue() ) ) { return false; }
		if ( sequence.getMonthlyResetOk() && !this.refDate.equals( this.refDate.dayOfMonth().withMinimumValue() ) ) { return false; }
		
		sequence.setResetDate( refDate );
		sequence.setNextNum(1);
		return true;
		
	}
	
}