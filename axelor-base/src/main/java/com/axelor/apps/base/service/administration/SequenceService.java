/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Optional;
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

  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  protected static final String DRAFT_PREFIX = "#";

  protected static final String PATTERN_FULL_YEAR = "%YYYY";
  protected static final String PATTERN_YEAR = "%YY";
  protected static final String PATTERN_MONTH = "%M";
  protected static final String PATTERN_FULL_MONTH = "%FM";
  protected static final String PATTERN_DAY = "%D";
  protected static final String PATTERN_WEEK = "%WY";
  protected static final String PADDING_LETTER = "A";
  protected static final String PADDING_DIGIT = "0";
  protected static final int SEQ_MAX_LENGTH = 14;
  protected static final int NUMBER_OF_LETTERS = 26;

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
      SequenceVersion sequenceVersion, Sequence sequence, LocalDate refDate)
      throws AxelorException {

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");

    String sequenceValue = getSequenceValue(sequenceVersion);

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

  protected String getSequenceValue(SequenceVersion sequenceVersion) throws AxelorException {

    Sequence sequence = sequenceVersion.getSequence();
    SequenceTypeSelect sequenceTypeSelect = sequence.getSequenceTypeSelect();
    Long nextNum = sequenceVersion.getNextNum();

    String padStr;
    String nextSequence;

    switch (sequenceTypeSelect) {
      case NUMBERS:
        padStr = PADDING_DIGIT;
        nextSequence = nextNum.toString();
        break;

      case LETTERS:
        SequenceLettersTypeSelect lettersType = sequence.getSequenceLettersTypeSelect();
        padStr = applyCase(PADDING_LETTER, lettersType);
        nextSequence = findNextLetterSequence(nextNum, lettersType);
        break;

      default:
        throw new AxelorException(
            sequenceVersion,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.SEQUENCE_TYPE_UNHANDLED),
            sequenceTypeSelect);
    }

    return StringUtils.leftPad(nextSequence, sequence.getPadding(), padStr);
  }

  /**
   * Computes a test sequence by computing the next seq without any save. Used for checking validity
   * purpose.
   *
   * @param sequence
   * @param refDate
   * @return the test sequence
   * @throws AxelorException
   */
  public String computeTestSeq(Sequence sequence, LocalDate refDate) throws AxelorException {
    SequenceVersion sequenceVersion = getVersion(sequence, refDate);
    return computeNextSeq(sequenceVersion, sequence, refDate);
  }

  protected String findNextLetterSequence(long nextNum, SequenceLettersTypeSelect lettersType)
      throws AxelorException {

    String result;
    if (nextNum <= 0) {
      throw new IllegalArgumentException("Input should be a strictly positive long.");
    } else if (nextNum == 1) {
      result = "A";
    } else {
      result = convertNextSeqLongToString(nextNum);
    }

    return applyCase(result, lettersType);
  }

  protected static String applyCase(String result, SequenceLettersTypeSelect lettersType)
      throws AxelorException {

    if (lettersType == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_LETTERS_TYPE_IS_NULL));
    }
    switch (lettersType) {
      case UPPERCASE:
        return result;

      case LOWERCASE:
        return result.toLowerCase();

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.SEQUENCE_LETTERS_TYPE_UNHANDLED),
            lettersType);
    }
  }

  protected String convertNextSeqLongToString(long nextNum) {
    if (nextNum == 1) {
      return "";
    }

    int alphabetLength = ALPHABET.length();

    long q = (nextNum - 1) / alphabetLength;
    int r = (int) (nextNum - 1) % alphabetLength;

    return convertNextSeqLongToString(q + 1) + ALPHABET.charAt(r);
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
   * Get draft sequence number prefix.
   *
   * @return
   */
  protected String getDraftPrefix() {
    return Optional.ofNullable(appBaseService.getAppBase().getDraftPrefix()).orElse(DRAFT_PREFIX);
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
    String draftPrefix = getDraftPrefix();
    return String.format("%s%d", draftPrefix, model.getId());
  }

  /**
   * Get draft sequence number with leading zeros.
   *
   * @param model
   * @param zeroPadding
   * @return
   */
  public String getDraftSequenceNumber(Model model, int padding) throws AxelorException {
    if (model.getId() == null) {
      throw new AxelorException(
          model,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SEQUENCE_NOT_SAVED_RECORD));
    }
    String draftPrefix = getDraftPrefix();
    return String.format(
        "%s%s",
        draftPrefix, StringHelper.fillStringLeft(String.valueOf(model.getId()), '0', padding));
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
    SequenceVersion lastSequenceVersion;
    lastSequenceVersion = sequenceVersionRepository.findByDate(sequence, todayDate);

    SequenceVersion finalLastSequenceVersion = lastSequenceVersion;
    sequenceVersionList.stream()
        .filter(sequenceVersion -> sequenceVersion.equals(finalLastSequenceVersion))
        .forEach(sequenceVersion -> sequenceVersion.setEndDate(endOfDate));

    return sequenceVersionList;
  }

  public void validateSequence(Sequence sequence) throws AxelorException {
    String draftPrefix = getDraftPrefix();

    if (sequence.getPrefixe() != null && sequence.getPrefixe().startsWith(draftPrefix))
      throw new AxelorException(
          sequence,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          BaseExceptionMessage.SEQUENCE_PREFIX,
          draftPrefix);
  }
}
