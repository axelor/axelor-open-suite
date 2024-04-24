/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.SequenceTypeSelect;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.utils.StringTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
@Singleton
public class SequenceService {

  protected static final String DRAFT_PREFIX = "#";

  protected static final String PATTERN_FULL_YEAR = "%YYYY";
  protected static final String PATTERN_YEAR = "%YY";
  protected static final String PATTERN_MONTH = "%M";
  protected static final String PATTERN_FULL_MONTH = "%FM";
  protected static final String PATTERN_DAY = "%D";
  protected static final String PATTERN_WEEK = "%WY";
  protected static final String PADDING_STRING = "0";
  protected static final int SEQ_MAX_LENGTH = 14;

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final SequenceVersionRepository sequenceVersionRepository;

  protected final SequenceVersionGeneratorService sequenceVersionGeneratorService;

  protected final AppBaseService appBaseService;

  protected final SequenceRepository sequenceRepo;

  @Inject
  public SequenceService(
      SequenceVersionRepository sequenceVersionRepository,
      AppBaseService appBaseService,
      SequenceRepository sequenceRepo,
      SequenceVersionGeneratorService sequenceVersionGeneratorService) {

    this.sequenceVersionRepository = sequenceVersionRepository;
    this.appBaseService = appBaseService;
    this.sequenceRepo = sequenceRepo;
    this.sequenceVersionGeneratorService = sequenceVersionGeneratorService;
  }

  public static boolean isYearValid(Sequence sequence) {

    boolean yearlyResetOk = sequence.getYearlyResetOk();

    if (!yearlyResetOk) {
      return true;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");
    String seq = seqPrefixe + seqSuffixe;

    return seq.contains(PATTERN_YEAR) || seq.contains(PATTERN_FULL_YEAR);
  }

  public static boolean isMonthValid(Sequence sequence) {

    boolean monthlyResetOk = sequence.getMonthlyResetOk();

    if (!monthlyResetOk) {
      return true;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");
    String seq = seqPrefixe + seqSuffixe;

    return (seq.contains(PATTERN_MONTH) || seq.contains(PATTERN_FULL_MONTH))
        && (seq.contains(PATTERN_YEAR) || seq.contains(PATTERN_FULL_YEAR));
  }

  public void checkSequenceLengthValidity(Sequence sequence) throws AxelorException {
    Company company = sequence.getCompany();
    String nextSeq = computeTestSeq(sequence, appBaseService.getTodayDate(company));

    if (nextSeq.length() > SEQ_MAX_LENGTH) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SEQUENCE_LENGTH_NOT_VALID));
    }
  }

  public Sequence getSequence(String code, Company company) {

    if (code == null) {
      return null;
    }
    if (company == null) {
      return sequenceRepo.findByCodeSelect(code);
    }

    return sequenceRepo.find(code, company);
  }

  public String getSequenceNumber(String code, Class objectClass, String fieldName)
      throws AxelorException {

    return this.getSequenceNumber(code, null, objectClass, fieldName);
  }

  public String getSequenceNumber(String code, Company company, Class objectClass, String fieldName)
      throws AxelorException {

    Sequence sequence = getSequence(code, company);

    if (sequence == null) {
      return null;
    }

    return this.getSequenceNumber(
        sequence, appBaseService.getTodayDate(company), objectClass, fieldName);
  }

  public boolean hasSequence(String code, Company company) {

    return getSequence(code, company) != null;
  }

  public String getSequenceNumber(Sequence sequence, Class objectClass, String fieldName)
      throws AxelorException {
    return getSequenceNumber(
        sequence, appBaseService.getTodayDate(sequence.getCompany()), objectClass, fieldName);
  }

  /**
   * Method returning a sequence number from a given generic sequence and a date
   *
   * @param sequence
   * @param refDate
   * @return
   */
  @Transactional(rollbackOn = {Exception.class})
  public String getSequenceNumber(
      Sequence sequence, LocalDate refDate, Class objectClass, String fieldName)
      throws AxelorException {
    Sequence seq =
        JPA.em()
            .createQuery("SELECT self FROM Sequence self WHERE id = :id", Sequence.class)
            .setParameter("id", sequence.getId())
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setFlushMode(FlushModeType.COMMIT)
            .getSingleResult();
    SequenceVersion sequenceVersion = getVersion(seq, refDate);
    String nextSeq = computeNextSeq(sequenceVersion, seq, refDate);

    if (appBaseService.getAppBase().getCheckExistingSequenceOnGeneration()
        && objectClass != null
        && !Strings.isNullOrEmpty(fieldName)) {
      this.isSequenceAlreadyExisting(objectClass, fieldName, nextSeq, seq);
    }

    sequenceVersion.setNextNum(sequenceVersion.getNextNum() + seq.getToBeAdded());
    if (sequenceVersion.getId() == null) {
      sequenceVersionRepository.save(sequenceVersion);
    }
    return nextSeq;
  }

  protected void isSequenceAlreadyExisting(
      Class objectClass, String fieldName, String nextSeq, Sequence seq) throws AxelorException {
    String table = objectClass.getSimpleName();
    boolean isSequenceAlreadyExisting =
        CollectionUtils.isNotEmpty(
            JPA.em()
                .createQuery(
                    "SELECT self FROM " + table + " self WHERE " + fieldName + " = :nextSeq",
                    objectClass)
                .setParameter("nextSeq", nextSeq)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setFlushMode(FlushModeType.COMMIT)
                .getResultList());
    if (isSequenceAlreadyExisting) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_ALREADY_EXISTS),
          nextSeq,
          seq.getFullName());
    }
  }

  protected String computeNextSeq(
      SequenceVersion sequenceVersion, Sequence sequence, LocalDate refDate) {

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");
    String sequenceValue;

    if (sequence.getSequenceTypeSelect() == SequenceTypeSelect.NUMBERS) {
      sequenceValue =
          StringUtils.leftPad(
              sequenceVersion.getNextNum().toString(), sequence.getPadding(), PADDING_STRING);
    } else {
      sequenceValue = findNextLetterSequence(sequenceVersion);
    }
    String nextSeq =
        (seqPrefixe + sequenceValue + seqSuffixe)
            .replace(PATTERN_FULL_YEAR, Integer.toString(refDate.get(ChronoField.YEAR_OF_ERA)))
            .replace(PATTERN_YEAR, refDate.format(DateTimeFormatter.ofPattern("yy")))
            .replace(PATTERN_MONTH, Integer.toString(refDate.getMonthValue()))
            .replace(PATTERN_FULL_MONTH, refDate.format(DateTimeFormatter.ofPattern("MM")))
            .replace(PATTERN_DAY, Integer.toString(refDate.getDayOfMonth()))
            .replace(
                PATTERN_WEEK, Integer.toString(refDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

    log.debug("nextSeq : : : : {}", nextSeq);

    return nextSeq;
  }

  /**
   * Compute a test sequence by computing the next seq without any save Use for checking validity
   * purpose
   *
   * @param sequence
   * @param refDate
   * @return the test sequence
   */
  public String computeTestSeq(Sequence sequence, LocalDate refDate) {
    SequenceVersion sequenceVersion = getVersion(sequence, refDate);
    return computeNextSeq(sequenceVersion, sequence, refDate);
  }

  protected String findNextLetterSequence(SequenceVersion sequenceVersion) {
    long n = sequenceVersion.getNextNum();
    char[] buf = new char[(int) Math.floor(Math.log(25 * (n + 1)) / Math.log(26))];
    for (int i = buf.length - 1; i >= 0; i--) {
      n--;
      buf[i] = (char) ('A' + n % 26);
      n /= 26;
    }
    if (sequenceVersion.getSequence().getSequenceLettersTypeSelect()
        == SequenceLettersTypeSelect.UPPERCASE) {
      return new String(buf);
    }
    return new String(buf).toLowerCase();
  }

  public SequenceVersion getVersion(Sequence sequence, LocalDate refDate) {

    log.debug("Reference date : : : : {}", refDate);

    SequenceVersion sequenceVersion = sequenceVersionRepository.findByDate(sequence, refDate);
    if (sequenceVersion == null) {
      sequenceVersion = sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
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
                sequence.getCodeSelect())
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
          model,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SEQUENCE_NOT_SAVED_RECORD));
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
          model,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SEQUENCE_NOT_SAVED_RECORD));
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

  /**
   * Computes sequence full name
   *
   * @param sequence Sequence to compute full name
   */
  public String computeFullName(Sequence sequence) {
    StringBuilder fn = new StringBuilder();

    if (sequence.getPrefixe() != null) {
      fn.append(sequence.getPrefixe());
    }

    for (int i = 0; i < sequence.getPadding(); i++) {
      fn.append("X");
    }

    if (sequence.getSuffixe() != null) {
      fn.append(sequence.getSuffixe());
    }

    fn.append(" - ");
    fn.append(sequence.getName());

    return fn.toString();
  }

  public List<SequenceVersion> updateSequenceVersions(
      Sequence sequence, LocalDate todayDate, LocalDate endOfDate) {

    List<SequenceVersion> sequenceVersionList = sequence.getSequenceVersionList();
    if (ObjectUtils.isEmpty(sequenceVersionList)) {
      return sequenceVersionList;
    }
    sequenceVersionList.stream()
        .filter(
            version ->
                version.getStartDate().compareTo(todayDate) <= 0
                    && (version.getEndDate() == null
                        || version.getEndDate().compareTo(todayDate) >= 0))
        .max(Comparator.comparing(SequenceVersion::getStartDate))
        .ifPresent(sequenceVersion -> sequenceVersion.setEndDate(endOfDate));

    return sequenceVersionList;
  }
}
