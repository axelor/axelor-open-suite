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
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
@Singleton
public class SequenceService {

  private static final String PATTERN_FULL_YEAR = "%YYYY",
      PATTERN_YEAR = "%YY",
      PATTERN_MONTH = "%M",
      PATTERN_FULL_MONTH = "%FM",
      PATTERN_DAY = "%D",
      PATTERN_WEEK = "%WY",
      PADDING_STRING = "0";

  private static final String DRAFT_PREFIX = "#";

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private SequenceVersionRepository sequenceVersionRepository;

  private GeneralService generalService;

  @Inject private SequenceRepository sequenceRepo;

  @Inject
  public SequenceService(
      SequenceVersionRepository sequenceVersionRepository, GeneralService generalService) {

    this.sequenceVersionRepository = sequenceVersionRepository;
    this.generalService = generalService;
  }

  /**
   * Retourne une sequence en fonction du code, de la sté
   *
   * @return
   */
  public Sequence getSequence(String code, Company company) {

    if (code == null) {
      return null;
    }
    if (company == null) {
      return sequenceRepo.findByCode(code);
    }

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

    if (sequence == null) {
      return null;
    }

    return this.getSequenceNumber(sequence, generalService.getTodayDate());
  }

  /**
   * Retourne une sequence en fonction du code, de la sté
   *
   * @return
   */
  public boolean hasSequence(String code, Company company) {

    return getSequence(code, company) != null;
  }

  public static boolean isYearValid(Sequence sequence) {

    boolean yearlyResetOk = sequence.getYearlyResetOk();

    if (!yearlyResetOk) {
      return true;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
        seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
        seq = seqPrefixe + seqSuffixe;

    if (yearlyResetOk && !seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR)) {
      return false;
    }

    return true;
  }

  public static boolean isMonthValid(Sequence sequence) {

    boolean monthlyResetOk = sequence.getMonthlyResetOk();

    if (!monthlyResetOk) {
      return true;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
        seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
        seq = seqPrefixe + seqSuffixe;

    if (monthlyResetOk
        && ((!seq.contains(PATTERN_MONTH) && !seq.contains(PATTERN_FULL_MONTH))
            || (!seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR)))) {
      return false;
    }

    return true;
  }

  public String getSequenceNumber(Sequence sequence) {
    return getSequenceNumber(sequence, generalService.getTodayDate());
  }

  /**
   * Fonction retournant une numéro de séquence depuis une séquence générique, et une date
   *
   * @param sequence
   * @return
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public String getSequenceNumber(Sequence sequence, LocalDate refDate) {

    SequenceVersion sequenceVersion = getVersion(sequence, refDate);

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), ""),
        seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), ""),
        padLeft =
            StringUtils.leftPad(
                sequenceVersion.getNextNum().toString(), sequence.getPadding(), PADDING_STRING);

    String nextSeq =
        (seqPrefixe + padLeft + seqSuffixe)
            .replaceAll(PATTERN_FULL_YEAR, Integer.toString(refDate.getYear()))
            .replaceAll(PATTERN_YEAR, Integer.toString(refDate.getYearOfCentury()))
            .replaceAll(PATTERN_MONTH, Integer.toString(refDate.getMonthOfYear()))
            .replaceAll(PATTERN_FULL_MONTH, refDate.toString("MM"))
            .replaceAll(PATTERN_DAY, Integer.toString(refDate.getDayOfMonth()))
            .replaceAll(PATTERN_WEEK, Integer.toString(refDate.getWeekOfWeekyear()));

    log.debug("nextSeq : : : : {}", nextSeq);

    sequenceVersion.setNextNum(sequenceVersion.getNextNum() + sequence.getToBeAdded());
    sequenceVersionRepository.save(sequenceVersion);
    return nextSeq;
  }

  protected SequenceVersion getVersion(Sequence sequence, LocalDate refDate) {

    log.debug("Reference date : : : : {}", refDate);

    if (sequence.getMonthlyResetOk()) {
      return getVersionByMonth(sequence, refDate);
    }
    if (sequence.getYearlyResetOk()) {
      return getVersionByYear(sequence, refDate);
    }
    return getVersionByDate(sequence, refDate);
  }

  protected SequenceVersion getVersionByDate(Sequence sequence, LocalDate refDate) {

    SequenceVersion sequenceVersion = sequenceVersionRepository.findByDate(sequence, refDate);
    if (sequenceVersion == null) {
      sequenceVersion = new SequenceVersion(sequence, refDate, null, 1L);
    }

    return sequenceVersion;
  }

  protected SequenceVersion getVersionByMonth(Sequence sequence, LocalDate refDate) {

    SequenceVersion sequenceVersion =
        sequenceVersionRepository.findByMonth(
            sequence, refDate.getMonthOfYear(), refDate.getYear());
    if (sequenceVersion == null) {
      sequenceVersion =
          new SequenceVersion(
              sequence,
              refDate.dayOfMonth().withMinimumValue(),
              refDate.dayOfMonth().withMaximumValue(),
              1L);
    }

    return sequenceVersion;
  }

  protected SequenceVersion getVersionByYear(Sequence sequence, LocalDate refDate) {

    SequenceVersion sequenceVersion =
        sequenceVersionRepository.findByYear(sequence, refDate.getYear());
    if (sequenceVersion == null) {
      sequenceVersion =
          new SequenceVersion(
              sequence,
              refDate.monthOfYear().withMinimumValue().dayOfMonth().withMinimumValue(),
              refDate.monthOfYear().withMaximumValue().dayOfMonth().withMaximumValue(),
              1L);
    }

    return sequenceVersion;
  }

  public String getDefaultTitle(Sequence sequence) {
    MetaSelectItem item =
        Beans.get(MetaSelectItemRepository.class)
            .all()
            .filter(
                "self.select.name = ? AND self.value = ?",
                "sequence.generic.code.select",
                sequence.getCode())
            .fetchOne();

    return item.getTitle();
  }

  /**
   * Get draft sequence number.
   *
   * @param model
   * @return
   * @throws AxelorException
   */
  public String getDraftSequenceNumber(Model model) throws AxelorException {
    if (model.getId() == null) {
      throw new AxelorException(
          I18n.get(IExceptionMessage.SEQUENCE_NOT_SAVED_RECORD), IException.INCONSISTENCY);
    }
    return String.format("%s%d", DRAFT_PREFIX, model.getId());
  }

  /**
   * Get draft sequence number with leading zeros.
   *
   * @param model
   * @param padding
   * @return
   */
  public String getDraftSequenceNumber(Model model, int zeroPadding) throws AxelorException {
    if (model.getId() == null) {
      throw new AxelorException(
          I18n.get(IExceptionMessage.SEQUENCE_NOT_SAVED_RECORD), IException.INCONSISTENCY);
    }
    return String.format(
        "%s%s",
        DRAFT_PREFIX, StringTool.fillStringLeft(String.valueOf(model.getId()), '0', zeroPadding));
  }

  /**
   * Check whether a sequence number is empty or draft.
   *
   * <p>Also consider '*' as draft character for backward compatibility.
   *
   * @param sequenceNumber
   * @return
   */
  public boolean isEmptyOrDraftSequenceNumber(String sequenceNumber) {
    return Strings.isNullOrEmpty(sequenceNumber)
        || sequenceNumber.matches(String.format("[\\%s\\*]\\d+", DRAFT_PREFIX));
  }
}
