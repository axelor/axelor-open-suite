package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import org.apache.commons.lang3.StringUtils;

public class SequenceDateCheckServiceImpl implements SequenceDateCheckService {

  protected static final String PATTERN_YEAR = "%YY";
  protected static final String PATTERN_FULL_YEAR = "%YYYY";
  protected static final String PATTERN_MONTH = "%M";
  protected static final String PATTERN_FULL_MONTH = "%FM";

  /**
   * * Validates if the sequence contains a year pattern when yearly reset is enabled.
   *
   * @param sequence
   */
  public void isYearValid(Sequence sequence) throws AxelorException {

    boolean yearlyResetOk = sequence.getYearlyResetOk();

    if (!yearlyResetOk) {
      return;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");
    String seq = seqPrefixe + seqSuffixe;

    if (!seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_YEAR_VALIDITY_ERROR));
    }
  }

  /**
   * Validates if the sequence contains a month and year pattern when monthly reset is enabled.
   *
   * @param sequence
   */
  public void isMonthValid(Sequence sequence) throws AxelorException {

    boolean monthlyResetOk = sequence.getMonthlyResetOk();

    if (!monthlyResetOk) {
      return;
    }

    String seqPrefixe = StringUtils.defaultString(sequence.getPrefixe(), "");
    String seqSuffixe = StringUtils.defaultString(sequence.getSuffixe(), "");
    String seq = seqPrefixe + seqSuffixe;

    if ((!seq.contains(PATTERN_MONTH) && !seq.contains(PATTERN_FULL_MONTH))
        || (!seq.contains(PATTERN_YEAR) && !seq.contains(PATTERN_FULL_YEAR))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_MONTH_VALIDITY_ERROR));
    }
  }
}
