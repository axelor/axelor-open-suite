/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.script.ScriptAllowed;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScriptAllowed
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
  protected static final int SEQ_MAX_LENGTH = 14;

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final SequenceVersionRepository sequenceVersionRepository;

  protected final SequenceVersionGeneratorService sequenceVersionGeneratorService;

  protected final AppBaseService appBaseService;

  protected final SequenceRepository sequenceRepo;

  protected final SequenceReservationService sequenceReservationService;

  protected final SequenceComputationService sequenceComputationService;

  @Inject
  public SequenceService(
      SequenceVersionRepository sequenceVersionRepository,
      AppBaseService appBaseService,
      SequenceRepository sequenceRepo,
      SequenceVersionGeneratorService sequenceVersionGeneratorService,
      SequenceReservationService sequenceReservationService,
      SequenceComputationService sequenceComputationService) {

    this.sequenceVersionRepository = sequenceVersionRepository;
    this.appBaseService = appBaseService;
    this.sequenceRepo = sequenceRepo;
    this.sequenceVersionGeneratorService = sequenceVersionGeneratorService;
    this.sequenceReservationService = sequenceReservationService;
    this.sequenceComputationService = sequenceComputationService;
  }

  /**
   * Checks if the generated sequence length is within the allowed limit.
   *
   * @param sequence
   * @throws AxelorException
   */
  public void checkSequenceLengthValidity(Sequence sequence) throws AxelorException {
    Company company = sequence.getCompany();
    String nextSeq = computeTestSeq(sequence, appBaseService.getTodayDate(company));

    if (nextSeq.length() > SEQ_MAX_LENGTH) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.SEQUENCE_LENGTH_NOT_VALID));
    }
  }

  /**
   * Retrieves a sequence by its code and associated company.
   *
   * @param code
   * @param company
   * @return {@link Sequence} or null if not found.
   */
  public Sequence getSequence(String code, Company company) {

    if (code == null) {
      return null;
    }
    if (company == null) {
      return sequenceRepo.findByCodeSelect(code);
    }

    return sequenceRepo.find(code, company);
  }

  /**
   * Checks if a sequence exists for the given code and company.
   *
   * @param code
   * @param company
   * @return {@code true} if a sequence exists, {@code false} otherwise.
   */
  public boolean hasSequence(String code, Company company) {

    return getSequence(code, company) != null;
  }

  protected String computeNextSeq(
      SequenceVersion sequenceVersion, Sequence sequence, LocalDate refDate)
      throws AxelorException {

    String seqPrefixe = Objects.toString(sequence.getPrefixe(), "");
    String seqSuffixe = Objects.toString(sequence.getSuffixe(), "");

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
    Long nextNum = sequenceVersion.getNextNum();

    return sequenceComputationService.getSequenceValue(sequence, nextNum);
  }

  protected String findNextAlphanumericSequence(Long nextNum, String pattern) {
    return sequenceComputationService.findNextAlphanumericSequence(nextNum, pattern);
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

  protected String applyCase(String result, SequenceLettersTypeSelect lettersType)
      throws AxelorException {
    return sequenceComputationService.applyCase(result, lettersType);
  }

  protected String convertNextSeqLongToString(long nextNum) {
    return sequenceComputationService.convertNextSeqLongToString(nextNum);
  }

  /**
   * Get or creates a sequence version for the given sequence and reference date.
   *
   * @param sequence
   * @param refDate
   * @return {@link SequenceVersion}.
   */
  public SequenceVersion getVersion(Sequence sequence, LocalDate refDate) {

    log.debug("Reference date : : : : {}", refDate);

    SequenceVersion sequenceVersion = sequenceVersionRepository.findByDate(sequence, refDate);
    if (sequenceVersion == null) {
      sequenceVersion = sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    }

    return sequenceVersion;
  }

  /**
   * Get the default title for a sequence based on its code.
   *
   * @param sequence
   * @return default title
   */
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
   * @param padding
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
        || sequenceNumber.matches(String.format("[\\%s\\*]\\d+", getDraftPrefix()));
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

    fn.append("X".repeat(Math.max(0, sequence.getPadding())));

    if (sequence.getSuffixe() != null) {
      fn.append(sequence.getSuffixe());
    }

    fn.append(" - ");
    fn.append(sequence.getName());

    return fn.toString();
  }

  /**
   * Updates sequence versions by setting the end date for the active version.
   *
   * @param sequence
   * @param todayDate
   * @param endOfDate
   * @return The updated list of sequence versions.
   */
  public List<SequenceVersion> updateSequenceVersions(
      Sequence sequence, LocalDate todayDate, LocalDate endOfDate) {

    List<SequenceVersion> sequenceVersionList = sequence.getSequenceVersionList();
    if (ObjectUtils.isEmpty(sequenceVersionList)) {
      return sequenceVersionList;
    }
    sequenceVersionList.stream()
        .filter(
            version ->
                !version.getStartDate().isAfter(todayDate)
                    && (version.getEndDate() == null || !version.getEndDate().isBefore(todayDate)))
        .max(Comparator.comparing(SequenceVersion::getStartDate))
        .ifPresent(sequenceVersion -> sequenceVersion.setEndDate(endOfDate));

    return sequenceVersionList;
  }

  /**
   * Validates that the sequence prefix does not start with the draft prefix.
   *
   * @param sequence
   * @throws AxelorException
   */
  public void validateSequence(Sequence sequence) throws AxelorException {
    String draftPrefix = getDraftPrefix();

    if (sequence.getPrefixe() != null && sequence.getPrefixe().startsWith(draftPrefix))
      throw new AxelorException(
          sequence,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          BaseExceptionMessage.SEQUENCE_PREFIX,
          draftPrefix);
  }

  /**
   * Generates a sequence number for a given sequence, entity, and field.
   *
   * @param sequence
   * @param objectClass
   * @param fieldName
   * @param model
   * @return The generated sequence number.
   * @throws AxelorException If the sequence is invalid or the number already exists.
   */
  public String getSequenceNumber(
      Sequence sequence, Class<? extends Model> objectClass, String fieldName, Model model)
      throws AxelorException {
    return this.getSequenceNumber(
        sequence,
        appBaseService.getTodayDate(sequence.getCompany()),
        objectClass,
        fieldName,
        model);
  }

  /**
   * Verifies that the sequence pattern length matches the padding.
   *
   * @param sequence
   * @throws AxelorException If the pattern length does not match the padding.
   */
  public void verifyPattern(Sequence sequence) throws AxelorException {
    if (sequence.getPattern() != null && sequence.getPadding() != sequence.getPattern().length()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_PATTERN_LENGTH_NOT_VALID));
    }
  }

  /**
   * Retrieves a sequence number by code, entity, and field.
   *
   * @param code
   * @param objectClass
   * @param fieldName
   * @param model
   * @return The generated sequence number, or null if sequence doesn't exists.
   * @throws AxelorException If the sequence is invalid or the number already exists.
   */
  public String getSequenceNumber(
      String code, Class<? extends Model> objectClass, String fieldName, Model model)
      throws AxelorException {

    return this.getSequenceNumber(code, null, objectClass, fieldName, model);
  }

  /**
   * Retrieves a sequence number by code, company, entity, and field.
   *
   * @param code
   * @param objectClass
   * @param fieldName
   * @param model
   * @return The generated sequence number, or null if sequence doesn't exists.
   * @throws AxelorException If the sequence is invalid or the number already exists.
   */
  public String getSequenceNumber(
      String code,
      Company company,
      Class<? extends Model> objectClass,
      String fieldName,
      Model model)
      throws AxelorException {

    Sequence sequence = getSequence(code, company);

    if (sequence == null) {
      return null;
    }

    return this.getSequenceNumber(
        sequence, appBaseService.getTodayDate(company), objectClass, fieldName, model);
  }

  /**
   * Generates a sequence number for a given sequence, date, entity, and field.
   *
   * <p>This method delegates to {@link SequenceReservationService} which handles:
   *
   * <ul>
   *   <li>Isolated transaction for sequence increment (no long-lived locks)
   *   <li>Transaction-aware reservation lifecycle (confirm on commit, release on rollback)
   *   <li>Reuse of released sequence numbers to minimize gaps
   * </ul>
   *
   * @param sequence the sequence configuration
   * @param refDate the reference date for version selection and patterns
   * @param objectClass the entity class for duplicate checking (can be null)
   * @param fieldName the field name for duplicate checking (can be null)
   * @param model the model instance for Groovy evaluation (can be null)
   * @return the generated sequence number
   * @throws AxelorException if sequence generation fails
   */
  public String getSequenceNumber(
      Sequence sequence,
      LocalDate refDate,
      Class<? extends Model> objectClass,
      String fieldName,
      Model model)
      throws AxelorException {
    return sequenceReservationService.reserveSequenceNumber(
        sequence, refDate, objectClass, fieldName, model);
  }
}
