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
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.db.Model;
import java.time.LocalDate;

/**
 * Service for computing sequence numbers from raw values.
 *
 * <p>This service contains pure computation logic with no database access. It is responsible for
 * formatting sequence numbers based on the sequence configuration (type, prefix, suffix, patterns).
 */
public interface SequenceComputationService {

  /**
   * Computes the full sequence number string from the given next number value.
   *
   * <p>This method handles:
   *
   * <ul>
   *   <li>Numeric sequences (padding with zeros)
   *   <li>Letter sequences (base-26 encoding)
   *   <li>Alphanumeric sequences (pattern-based)
   *   <li>Groovy prefix/suffix evaluation
   *   <li>Date pattern replacement (%YYYY, %YY, %M, %FM, %D, %WY)
   * </ul>
   *
   * @param sequenceVersion the sequence version being used
   * @param sequence the sequence configuration
   * @param refDate the reference date for pattern replacement
   * @param model the model instance for Groovy evaluation (can be null)
   * @param nextNum the raw next number value to format
   * @return the formatted sequence number string
   * @throws AxelorException if computation fails (e.g., Groovy error)
   */
  String computeSequenceNumber(
      SequenceVersion sequenceVersion,
      Sequence sequence,
      LocalDate refDate,
      Model model,
      Long nextNum)
      throws AxelorException;

  /**
   * Gets the formatted sequence value (numeric, letter, or alphanumeric portion only).
   *
   * <p>This is the core value without prefix/suffix or date patterns.
   *
   * @param sequence the sequence configuration
   * @param nextNum the raw next number value
   * @return the formatted sequence value
   * @throws AxelorException if the sequence type is not supported
   */
  String getSequenceValue(Sequence sequence, Long nextNum) throws AxelorException;

  /**
   * Finds the next letter sequence for the given number.
   *
   * <p>Converts a number to a base-26 letter representation (A, B, ..., Z, AA, AB, ...).
   *
   * @param nextNum the number to convert (must be positive)
   * @param sequence the sequence configuration (for letter case)
   * @return the letter sequence string
   * @throws AxelorException if the letter type is not configured
   */
  String findNextLetterSequence(long nextNum, Sequence sequence) throws AxelorException;

  /**
   * Finds the next alphanumeric sequence for the given number.
   *
   * <p>Uses the pattern from the sequence configuration where N = digit and L = letter.
   *
   * @param nextNum the number to convert
   * @param pattern the alphanumeric pattern (e.g., "NNLN" for 2 digits, 1 letter, 1 digit)
   * @return the alphanumeric sequence string
   */
  String findNextAlphanumericSequence(Long nextNum, String pattern);

  /**
   * Evaluates a Groovy expression for prefix or suffix.
   *
   * @param groovyExpression the Groovy script to evaluate
   * @param model the model context for the script (can be null)
   * @return the evaluated string result, or the original expression if model is null
   * @throws AxelorException if Groovy evaluation fails
   */
  String evaluateGroovy(String groovyExpression, Model model) throws AxelorException;

  /**
   * Applies the case transformation based on the letters type.
   *
   * @param result the string to transform
   * @param lettersType the letters type (uppercase or lowercase)
   * @return the transformed string
   * @throws AxelorException if letters type is null or unsupported
   */
  String applyCase(String result, SequenceLettersTypeSelect lettersType) throws AxelorException;

  /**
   * Converts a number to a base-26 letter representation.
   *
   * @param nextNum the number to convert
   * @return the letter representation
   */
  String convertNextSeqLongToString(long nextNum);
}
