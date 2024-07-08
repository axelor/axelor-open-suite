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
package com.axelor.apps.base.service.printing.template;

import com.axelor.meta.MetaScanner;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Optional;
import org.apache.commons.lang3.ClassUtils;

public class PrintingGeneratorFactoryProviderImpl implements PrintingGeneratorFactoryProvider {

  protected static final String SELECT_ATTR_PRINT_FACTORY = "print-factory";
  protected static final String PRINTING_TEMPLATE_TYPE_SELECT =
      "base.printing.template.type.select";

  protected static final Cache<Integer, Class<? extends PrintingGeneratorFactory>> CACHE =
      CacheBuilder.newBuilder().maximumSize(100).weakValues().build();

  @Override
  public Class<? extends PrintingGeneratorFactory> get(Integer type) {
    Class<? extends PrintingGeneratorFactory> klass = CACHE.getIfPresent(type);
    if (klass != null) {
      return klass;
    }
    Option option = MetaStore.getSelectionItem(PRINTING_TEMPLATE_TYPE_SELECT, type.toString());
    klass =
        Optional.ofNullable(option)
            .map(Option::getData)
            .map(map -> map.get(SELECT_ATTR_PRINT_FACTORY))
            .map(Object::toString)
            .map(this::findClass)
            .orElse(null);
    if (klass != null) {
      CACHE.put(type, klass);
    }
    return klass;
  }

  protected Class<? extends PrintingGeneratorFactory> findClass(String klassName) {
    return MetaScanner.findSubTypesOf(PrintingGeneratorFactory.class)
        .within(ClassUtils.getPackageName(klassName)).find().stream()
        .filter(c -> c.getName().equals(klassName))
        .findFirst()
        .orElse(null);
  }
}
