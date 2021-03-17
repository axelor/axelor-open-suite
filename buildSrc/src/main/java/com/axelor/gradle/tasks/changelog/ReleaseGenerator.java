/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2020 Axelor (<http://axelor.com>).
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
package com.axelor.gradle.tasks.changelog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ReleaseGenerator {

  private static final String NEW_LINE = System.lineSeparator();

  public String generate(Release release) {
    StringBuilder releaseContent = new StringBuilder();

    appendHeader(releaseContent, release);
    appendEntries(releaseContent, release);

    return releaseContent.toString();
  }

  private void appendEntries(StringBuilder content, Release release) {
    if (release.getEntries() == null) {
      return;
    }
    SortedMap<String, EntryType> sortedTypes = new TreeMap<>();
    for (EntryType type : EntryType.values()) {
      sortedTypes.put(type.getValue(), type);
    }

    for (EntryType type : sortedTypes.values()) {
      if (release.getEntries().containsKey(type)) {
        appendEntriesPerType(content, type, release.getEntries().get(type));
      }
    }
  }

  private void appendEntriesPerType(
      StringBuilder content, EntryType type, List<ChangelogEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return;
    }
    content.append("#### ").append(type.getValue()).append(NEW_LINE).append(NEW_LINE);
    for (ChangelogEntry entry : entries) {
      content.append(MessageFormat.format("* {0}", entry.getTitle())).append(NEW_LINE);
      if (entry.getDescription() != null && !"".equals(entry.getDescription())) {
        List<String> lines =
            new ArrayList<>(Arrays.asList(entry.getDescription().trim().split("\n")));
        String details = String.join(NEW_LINE, lines);
        content.append(NEW_LINE).append(details).append(NEW_LINE).append(NEW_LINE);
      }
    }
    content.append(NEW_LINE);
  }

  private void appendHeader(StringBuilder content, Release release) {
    content
        .append(MessageFormat.format("## [{0}] ({1})", release.getVersion(), release.getDate()))
        .append(NEW_LINE)
        .append(NEW_LINE);
  }

  private boolean isBlank(CharSequence value) {
    if (value == null || value.length() == 0) {
      return true;
    }
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isWhitespace(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
