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
package com.axelor.apps.base.service.administration;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SequenceService extends SequenceRepository{

	private static final Logger LOG = LoggerFactory.getLogger(Sequence.class); 

	private LocalDate today;
	
	@Inject
	private SequenceRepository sequenceRepo;

	@Inject
	public SequenceService() {
		
		this.today = GeneralService.getTodayDate();
		
	}
	
	public SequenceService(LocalDate today) {
		
		this.today = today;
		
	}	

	/**
	 * Mets à zéro les séquences qui sont configurées peut l'être (check box)
	 * 
	 * @param fromBatch
	 */
	@Transactional(rollbackOn = {Exception.class})
	public void resetSequenceAll()  {

		for( Sequence seq : sequenceRepo.all().fetch() )  { 
			if(seq.getYearlyResetOk())  { resetSequence(seq); }
		}
	
	}
	
	/**
	 * Mets à zéro une séquences 
	 * 
	 * @param fromBatch
	 */
	@Transactional(rollbackOn = {Exception.class})
	public void resetSequence( Sequence seq )  {
		
			seq.setNextNum(1); 
			sequenceRepo.save(seq);
		
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
			return sequenceRepo.findByCode(code);
		}
		else {
			return sequenceRepo.all().filter("self.company = ?1 and self.code = ?2", company, code).fetchOne();
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
		
		return this.getSequenceNumber(sequence, today.getYearOfCentury(), today.getMonthOfYear(), today.getDayOfMonth(), today.getWeekOfWeekyear());
			
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
	
	
	
	public String getSequenceNumber(Sequence sequence)  {
		
		return this.getSequenceNumber(sequence, today.getYearOfCentury(), today.getMonthOfYear(), today.getDayOfMonth(), today.getWeekOfWeekyear());
		
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
	public String getSequenceNumber(Sequence seq, int todayYear, int todayMoy, int todayDom, int todayWoy)  {
		String seqPrefixe = "";
		String seqSuffixe = "";
		if (seq.getPrefixe() != null)  {
			seqPrefixe = ((((seq.getPrefixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
		}	
		if (seq.getSuffixe() != null){
			seqSuffixe = ((((seq.getSuffixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
		}
		
		String padLeft = StringUtils.leftPad(seq.getNextNum().toString(), seq.getPadding(), "0");
		
		String nextSeq = seqPrefixe + padLeft + seqSuffixe;
		
		LOG.debug("nextSeq : : : : {}",nextSeq);	
		
		seq.setNextNum(seq.getNextNum() + seq.getToBeAdded());
		sequenceRepo.save(seq);
		return nextSeq;
	}
}