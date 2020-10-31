/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.service.administration.sequence;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SequenceComputer implements Callable<String> {

  private static final Logger LOG = LoggerFactory.getLogger(SequenceComputer.class);

  private static final Pattern DATE_PATTERN = Pattern.compile("%(\\w+)%");
  private static final char ZERO = '0';

  private long sequenceId;
  private int nextNum;
  private int padding;
  private int increment;
  private String prefixe;
  private String suffixe;

  public SequenceComputer init(long sequenceId) {
    this.sequenceId = sequenceId;

    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT next_num, padding, to_be_added, prefixe , suffixe "
                    + "FROM base_sequence "
                    + "WHERE id = :id");

    query.setParameter("id", sequenceId);

    Object[] result = (Object[]) query.getSingleResult();
    nextNum = (int) result[0];
    padding = (int) result[1];
    increment = (int) result[2];
    prefixe = (String) result[3];
    suffixe = (String) result[4];

    return this;
  }

  @Override
  public String call() {
    LOG.debug("[Thread : {}] START Compute sequences", Thread.currentThread().getName());

    boolean prefix = !Strings.isNullOrEmpty(prefixe);
    boolean suffix = !Strings.isNullOrEmpty(suffixe);

    StringBuilder stringBuilder = new StringBuilder();
    if (prefix) {
      stringBuilder.append(prefixe);
    }
    stringBuilder.append(Strings.padStart(Integer.toString(nextNum), padding, ZERO));
    if (suffix) {
      stringBuilder.append(suffixe);
    }

    String sequence =
        prefix || suffix ? computeDatePattern(stringBuilder.toString()) : stringBuilder.toString();
    updateSequenceNextNum();

    LOG.debug("[Thread : {}] END Compute sequences", Thread.currentThread().getName());

    return sequence;
  }

  protected String computeDatePattern(String sequence) {
    String computedSequence = sequence;
    // FIXME: Use company of the object related to the sequence
    LocalDate date =
        Beans.get(AppBaseService.class).getTodayDate(AuthUtils.getUser().getActiveCompany());
    Matcher matcher = DATE_PATTERN.matcher(sequence);

    while (matcher.find()) {
      String dateRequest = matcher.group(0);
      computedSequence =
          computedSequence.replaceAll(
              dateRequest, DateTimeFormatter.ofPattern(matcher.group(1)).format(date));
    }

    return computedSequence;
  }

  @Transactional
  protected int updateSequenceNextNum() {
    Query query =
        JPA.em().createNativeQuery("UPDATE base_sequence SET next_num = :nextNum WHERE id = :id");
    query.setParameter("nextNum", nextNum + increment);
    query.setParameter("id", sequenceId);
    return query.executeUpdate();
  }
}
