/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.service;

public interface TranslationService {
  /**
   * Update formated value translations.
   *
   * @param oldKey
   * @param format
   * @param args
   */
  void updateFormatedValueTranslations(String oldKey, String format, Object... args);

  /**
   * Create formated value translations.
   *
   * @param format
   * @param args
   */
  void createFormatedValueTranslations(String format, Object... args);

  /**
   * Remove value translations.
   *
   * @param key
   */
  void removeValueTranslations(String key);

  /**
   * Get the translation of the given key.
   *
   * @param key
   */
  String getTranslation(String key, String language);

  /**
   * Get the translation key of the given message.
   *
   * @param value
   * @param language
   * @return
   */
  String getTranslationKey(String message, String language);

  /**
   * Get the translation of the given value key.
   *
   * @param key
   * @param language
   * @return
   */
  String getValueTranslation(String key, String language);
}
