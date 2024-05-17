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
package com.axelor.apps.project.utils;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTaskUtilsServiceImpl implements ProjectTaskUtilsService {

  private static final String TASK_LINK = "<a href=\"#/ds/all.open.project.tasks/edit/%s\">@%s</a>";

  @Override
  public String getTaskLink(String value) {
    if (StringUtils.isEmpty(value)) {
      return value;
    }
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = Pattern.compile("@([^\\s]+)").matcher(value);
    Matcher nonMatcher = Pattern.compile("@([^\\s]+)(?=<\\/a>)").matcher(value);
    while (matcher.find()) {
      String matchedValue = matcher.group(1);
      String ticketNumber = matchedValue.replaceAll("\\<.*?\\>", "");
      if (nonMatcher.find() && ticketNumber.equals(nonMatcher.group(1))) {
        continue;
      }
      ProjectTask task =
          JPA.all(ProjectTask.class).filter("self.ticketNumber = ?1", ticketNumber).fetchOne();
      if (task != null) {
        matcher.appendReplacement(buffer, String.format(TASK_LINK, task.getId(), matchedValue));
      }
    }

    String result = buffer.toString();
    return StringUtils.isEmpty(result) ? value : result;
  }
}
