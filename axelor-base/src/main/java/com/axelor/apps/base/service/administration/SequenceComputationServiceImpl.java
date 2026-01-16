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
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.SequenceTypeSelect;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link SequenceComputationService}.
 *
 * <p>This service contains pure computation logic extracted from SequenceService. It has no
 * database access and only performs string formatting and transformations.
 */
@Singleton
public class SequenceComputationServiceImpl implements SequenceComputationService {

  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String PATTERN_FULL_YEAR = "%YYYY";
  private static final String PATTERN_YEAR = "%YY";
  private static final String PATTERN_MONTH = "%M";
  private static final String PATTERN_FULL_MONTH = "%FM";
  private static final String PATTERN_DAY = "%D";
  private static final String PATTERN_WEEK = "%WY";
  private static final String PADDING_LETTER = "A";
  private static final String PADDING_DIGIT = "0";

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public String computeSequenceNumber(
      SequenceVersion sequenceVersion,
      Sequence sequence,
      LocalDate refDate,
      Model model,
      Long nextNum)
      throws AxelorException {

    String seqPrefixe = Objects.toString(sequence.getPrefixe(), "");
    String seqSuffixe = Objects.toString(sequence.getSuffixe(), "");

    if (sequence.getPrefixGroovyOk()) {
      seqPrefixe = Objects.toString(evaluateGroovy(sequence.getPrefixGroovy(), model), "");
    }
    if (sequence.getSuffixGroovyOk()) {
      seqSuffixe = Objects.toString(evaluateGroovy(sequence.getSuffixGroovy(), model), "");
    }

    String sequenceValue = getSequenceValue(sequence, nextNum);

    String nextSeq =
        (seqPrefixe + sequenceValue + seqSuffixe)
            .replace(PATTERN_FULL_YEAR, Integer.toString(refDate.get(ChronoField.YEAR_OF_ERA)))
            .replace(PATTERN_YEAR, refDate.format(DateTimeFormatter.ofPattern("yy")))
            .replace(PATTERN_MONTH, Integer.toString(refDate.getMonthValue()))
            .replace(PATTERN_FULL_MONTH, refDate.format(DateTimeFormatter.ofPattern("MM")))
            .replace(PATTERN_DAY, Integer.toString(refDate.getDayOfMonth()))
            .replace(
                PATTERN_WEEK, Integer.toString(refDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

    log.trace("Computed sequence number: {}", nextSeq);

    return nextSeq;
  }

  @Override
  public String getSequenceValue(Sequence sequence, Long nextNum) throws AxelorException {

    SequenceTypeSelect sequenceTypeSelect = sequence.getSequenceTypeSelect();

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
        nextSequence = findNextLetterSequence(nextNum, sequence);
        break;

      case ALPHANUMERIC:
        padStr = PADDING_DIGIT;
        nextSequence = findNextAlphanumericSequence(nextNum, sequence.getPattern());
        break;

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.SEQUENCE_TYPE_UNHANDLED),
            sequenceTypeSelect);
    }

    return StringUtils.leftPad(nextSequence, sequence.getPadding(), padStr);
  }

  @Override
  public String findNextLetterSequence(long nextNum, Sequence sequence) throws AxelorException {

    SequenceLettersTypeSelect lettersType = sequence.getSequenceLettersTypeSelect();

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

  @Override
  public String findNextAlphanumericSequence(Long nextNum, String pattern) {
    int patternLength = pattern.length();
    StringBuilder sequence = new StringBuilder();
    for (int i = patternLength - 1; i >= 0; i--) {
      int value;
      switch (pattern.charAt(i)) {
        case 'N':
          value = (int) (nextNum % 10);
          nextNum = nextNum - value;
          nextNum = nextNum / 10;
          sequence.insert(0, value);
          break;

        case 'L':
          if (i == patternLength - 1) {
            nextNum = nextNum - 1;
          }
          value = (int) (nextNum % 26);
          nextNum = nextNum - value;
          nextNum = nextNum / 26;
          char temp = (char) ('A' + value);
          sequence.insert(0, temp);
          break;
      }
    }

    return sequence.toString();
  }

  @Override
  public String evaluateGroovy(String groovyExpression, Model model) throws AxelorException {

    if (!Strings.isNullOrEmpty(groovyExpression) && Objects.nonNull(model)) {
      try {
        Context cxt = new Context(Mapper.toMap(model), EntityHelper.getEntityClass(model));
        return String.valueOf(new GroovyScriptHelper(cxt).eval(groovyExpression));

      } catch (Exception e) {
        throw new AxelorException(
            e,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.SEQUENCE_GROOVY_CONFIGURATION));
      }
    }

    return groovyExpression;
  }

  @Override
  public String applyCase(String result, SequenceLettersTypeSelect lettersType)
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

  @Override
  public String convertNextSeqLongToString(long nextNum) {
    if (nextNum == 1) {
      return "";
    }

    int alphabetLength = ALPHABET.length();

    long q = (nextNum - 1) / alphabetLength;
    int r = (int) (nextNum - 1) % alphabetLength;

    return convertNextSeqLongToString(q + 1) + ALPHABET.charAt(r);
  }
}
