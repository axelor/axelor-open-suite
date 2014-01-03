/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.administration;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SequenceService {

	private static final Logger LOG = LoggerFactory.getLogger(Sequence.class); 

	private LocalDate today;

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

		for( Sequence seq : Sequence.all().fetch() )  { 
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
		
			seq.setNextNum(1); seq.save();
		
	}
	
	/**
	 * Retourne une sequence en fonction du code
	 * @return
	 */
	public String getSequence(String code, boolean check) {
		return getSequence(code, null, check);
	}	
	
	
	/**
	 * Retourne une sequence en fonction du code, de la sté 
	 * 
	 * @return
	 */
	public String getSequence(String code, Company company, boolean check) {
		return getSequence(code, company, null, check);
	}
	
	
	/**
	 * Retourne une sequence en fonction du code, de la sté et du journal 
	 * Le paramètre check permet de faire une vérification sans que la séquence ne soit incrémentée
	 * 
	 * @return
	 */
	public String getSequence(String code, Company company, Journal journal, boolean check) {
		if (code != null){
			Sequence seq = null;
			if (company == null){
				seq = Sequence.all().filter("self.code = ?1",code).fetchOne();
			}
			else if (journal == null){
				seq = Sequence.all().filter("self.company = ?1 and self.code = ?2",company,code).fetchOne();
			}
			else{
				 seq = Sequence.all().filter("self.company = ?1 and self.code = ?2 and self.journal = ?3",company,code,journal).fetchOne();
			}
			if (seq != null)  {
				if (!check)  {
					return getSequence(seq, today.getYearOfCentury(), today.getMonthOfYear(), today.getDayOfMonth(), today.getWeekOfWeekyear());
				}
				else  {
					return "true";
				}
			}
			else{
				LOG.debug("End getSequence : : : : NO SEQUENCE.");	
				return null;
			}
		}
		else{
			LOG.debug("End getSequence : : : : NO code");	
			return null;
		}
			
	}
	
	/**
	 * Retourne une sequence en fonction du code, de la sté et du produit 
	 * Le paramètre check permet de faire une vérification sans que la séquence ne soit incrémentée
	 * 
	 * @return
	 */
	public String getSequence(String code, Product product, Company company, boolean check) {
		if (code != null){
			Sequence seq = null;
			if (company == null){
				seq = Sequence.all().filter("self.code = ?1",code).fetchOne();
			}
			else if (product == null){
				seq = Sequence.all().filter("self.company = ?1 and self.code = ?2",company,code).fetchOne();
			}
			else{
				 seq = Sequence.all().filter("self.company = ?1 and self.code = ?2 and self.product = ?3",company,code,product).fetchOne();
			}
			if (seq != null)  {
				if (!check)  {
					return getSequence(seq, today.getYearOfCentury(), today.getMonthOfYear(), today.getDayOfMonth(), today.getWeekOfWeekyear());
				}
				else  {
					return "true";
				}
			}
			else{
				LOG.debug("End getSequence : : : : NO SEQUENCE.");	
				return null;
			}
		}
		else{
			LOG.debug("End getSequence : : : : NO code");	
			return null;
		}
			
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
	public String getSequence(Sequence seq, int todayYear, int todayMoy, int todayDom, int todayWoy)  {
		String seqPrefixe = "";
		String seqSuffixe = "";
		if (seq.getPrefixe() != null)  {
			seqPrefixe = ((((seq.getPrefixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
			if (seq.getProduct() != null)
				seqPrefixe = seqPrefixe.replaceAll("%PC", seq.getProduct().getCode());
		}	
		if (seq.getSuffixe() != null){
			seqSuffixe = ((((seq.getSuffixe()
					.replaceAll("%Y",String.format("%s",todayYear)))
					.replaceAll("%M",String.format("%s",todayMoy)))
					.replaceAll("%D",String.format("%s",todayDom)))
					.replaceAll("%WY",String.format("%s",todayWoy)));
			if (seq.getProduct() != null)
				seqSuffixe = seqSuffixe.replaceAll("%PC", seq.getProduct().getCode());
		}
		
		String padLeft = StringUtils.leftPad(seq.getNextNum().toString(), seq.getPadding(), "0");
		
		String nextSeq = seqPrefixe + padLeft + seqSuffixe;
		
		LOG.debug("nextSeq : : : : {}",nextSeq);	
		
		seq.setNextNum(seq.getNextNum() + seq.getToBeAdded());
		seq.save();
		return nextSeq;
	}
}