/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import org.apache.commons.lang.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.lang.invoke.MethodHandles;

public class SequenceService {

	private final static String
		PATTERN_FULL_YEAR = "%YYYY",	
		PATTERN_YEAR = "%YY",
		PATTERN_MONTH = "%M",
		PATTERN_FULL_MONTH ="%FM",
		PATTERN_DAY = "%D",
		PATTERN_WEEK = "%WY",
		PADDING_STRING = "0";

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private SequenceVersionRepository sequenceVersionRepository;

	private LocalDate today, refDate;
	
	@Inject
	private SequenceRepository sequenceRepo;

	@Inject
	public SequenceService( SequenceVersionRepository sequenceVersionRepository ) {

		this.sequenceVersionRepository = sequenceVersionRepository;

		this.today = Beans.get(AppBaseService.class).getTodayDate();
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
	 * Retourne une sequence en fonction du code, de la sté
	 *
	 * @return
	 */
	public Sequence getSequence(String codeSelect, Company company) {

		if (codeSelect == null)  { return null; }
		if (company == null)  { return sequenceRepo.findByCodeSelect(codeSelect); }

		return sequenceRepo.find(codeSelect, company);

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

		Sequence sequence = getSequence(code, company);

		if (sequence == null)  {  return null;  }

		return this.getSequenceNumber(sequence);

	}

	/**
	 * Retourne une sequence en fonction du code, de la sté
	 *
	 * @return
	 */
	public boolean hasSequence(String code, Company company) {

		return getSequence(code, company) != null;

	}

	public static boolean isYearValid( Sequence sequence ){

		boolean yearlyResetOk = sequence.getYearlyResetOk();

		if ( !yearlyResetOk ){ return true; }

		String
			seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
			seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
			seq = seqPrefixe + seqSuffixe;

		if ( yearlyResetOk && !seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR) ){ return false; }

		return true;

	}

	public static boolean isMonthValid( Sequence sequence ){

		boolean	monthlyResetOk = sequence.getMonthlyResetOk();

		if ( !monthlyResetOk ){ return true; }

		String
			seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
			seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
			seq = seqPrefixe + seqSuffixe;

		if ( monthlyResetOk && (
				(!seq.contains(PATTERN_MONTH) && !seq.contains(PATTERN_FULL_MONTH)) ||
				(!seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR))
            ))  {
             return false;
		}

		return true;

	}

	public static boolean isSequenceLengthValid(Sequence sequence) {
		String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "").replaceAll("%", "");
		String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "").replaceAll("%", "");

		return (seqPrefixe.length() + seqSuffixe.length() + sequence.getPadding()) <= 14;
	}

	/**
	 * Fonction retournant une numéro de séquence depuis une séquence générique, et une date
	 *
	 * @param sequence
	 * @return
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String getSequenceNumber( Sequence sequence )  {

		SequenceVersion sequenceVersion = getVersion(sequence);

		String
			seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
			seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
			padLeft = StringUtils.leftPad( sequenceVersion.getNextNum().toString(), sequence.getPadding(), PADDING_STRING );


		String nextSeq = ( seqPrefixe + padLeft + seqSuffixe )
				.replaceAll( PATTERN_FULL_YEAR, Integer.toString( refDate.get(ChronoField.YEAR_OF_ERA) ) )
				.replaceAll( PATTERN_YEAR, refDate.format(DateTimeFormatter.ofPattern("yy")) )
				.replaceAll( PATTERN_MONTH, Integer.toString( refDate.getMonthValue() ) )
				.replaceAll( PATTERN_FULL_MONTH, refDate.format(DateTimeFormatter.ofPattern("MM")) )
				.replaceAll( PATTERN_DAY, Integer.toString( refDate.getDayOfMonth() ) )
				.replaceAll( PATTERN_WEEK, Integer.toString( refDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) ) ) ;

		log.debug( "nextSeq : : : : {}" ,nextSeq );

		sequenceVersion.setNextNum( sequenceVersion.getNextNum() + sequence.getToBeAdded() );
		sequenceVersionRepository.save( sequenceVersion );
		return nextSeq;
	}

	protected SequenceVersion getVersion( Sequence sequence ){

		log.debug( "Reference date : : : : {}" , refDate );

		if ( sequence.getMonthlyResetOk() ){ return getVersionByMonth(sequence); }
		if ( sequence.getYearlyResetOk() ){ return getVersionByYear(sequence); }
		return getVersionByDate(sequence);

	}

	protected SequenceVersion getVersionByDate( Sequence sequence ){

		SequenceVersion sequenceVersion = sequenceVersionRepository.findByDate(sequence, refDate);
		if ( sequenceVersion == null ){ sequenceVersion = new SequenceVersion(sequence, refDate, null, 1L); }

		return sequenceVersion ;

	}

	protected SequenceVersion getVersionByMonth( Sequence sequence ){

		SequenceVersion sequenceVersion = sequenceVersionRepository.findByMonth(sequence, refDate.getMonthValue(), refDate.getYear());
		if ( sequenceVersion == null ){ sequenceVersion = new SequenceVersion(sequence, refDate.withDayOfMonth(1), refDate.withDayOfMonth(refDate.lengthOfMonth()), 1L); }

		return sequenceVersion;

	}

	protected SequenceVersion getVersionByYear( Sequence sequence ){

		SequenceVersion sequenceVersion = sequenceVersionRepository.findByYear(sequence, refDate.getYear());
		if ( sequenceVersion == null ){
			sequenceVersion = new SequenceVersion(sequence, refDate.withDayOfMonth(1), refDate.withDayOfMonth(refDate.lengthOfMonth()), 1L);
		}

		return sequenceVersion;

	}

	public String getDefaultTitle(Sequence sequence) {
		MetaSelectItem item = Beans.get(MetaSelectItemRepository.class)
								   .all()
								   .filter("self.select.name = ? AND self.value = ?", "sequence.generic.code.select", sequence.getCodeSelect())
								   .fetchOne();

		return item.getTitle();
	}
	
}