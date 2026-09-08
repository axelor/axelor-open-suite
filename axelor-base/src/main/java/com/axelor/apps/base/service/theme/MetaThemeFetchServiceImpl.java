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
package com.axelor.apps.base.service.theme;

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaTheme;
import com.axelor.meta.db.ThemeLogoMode;
import com.axelor.meta.db.repo.MetaThemeRepository;
import com.google.common.primitives.Longs;
import jakarta.inject.Inject;
import java.util.Optional;

public class MetaThemeFetchServiceImpl implements MetaThemeFetchService {

  protected final MetaThemeRepository themeRepository;

  @Inject
  public MetaThemeFetchServiceImpl(MetaThemeRepository themeRepository) {
    this.themeRepository = themeRepository;
  }

  @Override
  public MetaTheme getCurrentTheme(User user) {
    return fetchTheme(user).orElse(null);
  }

  @Override
  public ThemeLogoMode getCurrentThemeLogoMode(User user) {
    ThemeLogoMode metaTheme = fetchTheme(user).map(MetaTheme::getLogoMode).orElse(null);
    if (metaTheme != null) {
      return metaTheme;
    }

    String theme = user.getTheme();
    if (StringUtils.notEmpty(theme)) {
      return switch (theme) {
        case "dark" -> ThemeLogoMode.DARK;
        case "light" -> ThemeLogoMode.LIGHT;
        default -> ThemeLogoMode.NONE;
      };
    }

    return Optional.ofNullable(AppSettings.get().get(AvailableAppSettings.APPLICATION_THEME))
        .map(themeRepository::findByName)
        .flatMap(list -> list.stream().findFirst())
        .map(MetaTheme::getLogoMode)
        .orElse(ThemeLogoMode.NONE);
  }

  protected Optional<MetaTheme> fetchTheme(User user) {
    return Optional.ofNullable(user)
        .map(User::getTheme)
        .filter(StringUtils::notBlank)
        .map(Longs::tryParse)
        .flatMap(id -> Optional.ofNullable(themeRepository.find(id)));
  }
}
