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
package com.axelor.apps.base.service.app;

import com.axelor.apps.app.db.App;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the module versions from the installed modules, which are up to date at startup. */
public class AppVersionServiceImpl implements AppVersionService {

  /** Semantic version number, such as 10.1.0 or 2.3.4.5. */
  protected static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(\\.\\d+)+)");

  protected final MetaModuleRepository metaModuleRepository;

  @Inject
  public AppVersionServiceImpl(MetaModuleRepository metaModuleRepository) {
    this.metaModuleRepository = metaModuleRepository;
  }

  @Override
  public String getAppVersion(App app) {
    String appModules = app.getModules();
    if (StringUtils.isEmpty(appModules)) {
      return null;
    }

    List<String> versions =
        Arrays.stream(appModules.split(","))
            .map(String::trim)
            .map(metaModuleRepository::findByName)
            .filter(Objects::nonNull)
            .map(MetaModule::getModuleVersion)
            .filter(Objects::nonNull)
            .toList();
    return findMaxVersion(versions);
  }

  protected static String findMaxVersion(Collection<String> versions) {
    return versions.stream()
        .map(VERSION_PATTERN::matcher)
        .filter(Matcher::find)
        .map(matcher -> matcher.group(1))
        .max(Comparator.naturalOrder())
        .orElse(null);
  }
}
