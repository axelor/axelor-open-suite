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
package com.axelor.apps.base.service.administration;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SequenceService {

	private final static String
		PATTERN_YEAR = "%Y",
		PATTERN_MONTH = "%M",
		PATTERN_FULL_MONTH ="%FM",
		PATTERN_DAY = "%D",
		PATTERN_WEEK = "%WY",
		PADDING_STRING = "0";

	private final Logger log = LoggerFactory.getLogger( getClass() );

	private SequenceVersionRepository sequenceVersionRepository;

	private LocalDate today, refDate;
	
	@Inject
	private SequenceRepository sequenceRepo;

	@Inject
	public SequenceService( SequenceVersionRepository sequenceVersionRepository ) {

		this.sequenceVersionRepository = sequenceVersionRepository;

		this.today = Beans.get(GeneralService.class).getTodayDate();
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
	public Sequence getSequence(String code, Company company) {

		if (code == null)  { return null; }
		if (company == null)  { return sequenceRepo.findByCode(code); }

		return sequenceRepo.find(code, company);

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

	public static boolean isValid( Sequence sequence ){

		boolean
			monthlyResetOk = sequence.getMonthlyResetOk(),
			yearlyResetOk = sequence.getYearlyResetOk();

		if ( !monthlyResetOk && !yearlyResetOk ){ return true; }

		String
			seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
			seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
			seq = seqPrefixe + seqSuffixe;

		if ( yearlyResetOk && !seq.contains(PATTERN_YEAR) ){ return false; }
		if ( monthlyResetOk && !seq.contains(PATTERN_MONTH) && !seq.contains(PATTERN_FULL_MONTH) && !seq.contains(PATTERN_YEAR) ){ return false; }

		return true;

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

		SequenceVersion sequenceVersion = getVersion(sequence);

		String
			seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
			seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
			padLeft = StringUtils.leftPad( sequenceVersion.getNextNum().toString(), sequence.getPadding(), PADDING_STRING );


		String nextSeq = ( seqPrefixe + padLeft + seqSuffixe )
				.replaceAll( PATTERN_YEAR, Integer.toString( refDate.getYearOfCentury() ) )
				.replaceAll( PATTERN_MONTH, Integer.toString( refDate.getMonthOfYear() ) )
				.replaceAll( PATTERN_FULL_MONTH, refDate.toString("MM") )
				.replaceAll( PATTERN_DAY, Integer.toString( refDate.getDayOfMonth() ) )
				.replaceAll( PATTERN_WEEK, Integer.toString( refDate.getWeekOfWeekyear() ) ) ;

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

		SequenceVersion sequenceVersion = sequenceVersionRepository.findByMonth(sequence, refDate.getMonthOfYear(), refDate.getYear());
		if ( sequenceVersion == null ){ sequenceVersion = new SequenceVersion(sequence, refDate.dayOfMonth().withMinimumValue(), refDate.dayOfMonth().withMaximumValue(), 1L); }

		return sequenceVersion;

	}

	protected SequenceVersion getVersionByYear( Sequence sequence ){

		SequenceVersion sequenceVersion = sequenceVersionRepository.findByYear(sequence, refDate.getYear());
		if ( sequenceVersion == null ){
			sequenceVersion = new SequenceVersion(sequence, refDate.monthOfYear().withMinimumValue().dayOfMonth().withMinimumValue(), refDate.monthOfYear().withMaximumValue().dayOfMonth().withMaximumValue(), 1L);
		}

		return sequenceVersion;

	}
	
}