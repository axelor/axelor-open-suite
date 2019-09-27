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
package com.axelor.apps.base.service.template;

import com.axelor.apps.base.db.TemplateContextLine;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.TypedQuery;

public class TemplateContextLineService {

  public Object evaluate(TemplateContextLine line, Model bean) {

    String query = line.getQuery();
    if (Strings.isNullOrEmpty(query)) {
      return null;
    }

    return this.evaluate(query, bean);
  }

  public Object evaluate(String query, Model bean) {
    try {
      Class<?> klass = this.extractClass(query);
      StringBuilder sb = new StringBuilder(query);
      int n = 0, i = sb.indexOf("?");
      while (i > -1) {
        sb.replace(i, i + 1, "?" + (++n));
        i = sb.indexOf("?", i + 1);
      }

      TypedQuery<?> querie = JPA.em().createQuery(sb.toString(), klass);
      for (int j = 1; j <= n; j++) {
        querie.setParameter(n, bean);
      }
      List<?> list = querie.getResultList();
      if (list != null && !list.isEmpty() && list.size() == 1) {
        return list.get(0);
      } else {
        return list;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  private Class<?> extractClass(String query) {
    Pattern pattern = Pattern.compile("(from|FROM)(\\s*)(.+?)(\\s)");
    Matcher matcher = pattern.matcher(query);

    String klassName = "";
    if (matcher.find()) {
      klassName = matcher.group(3).trim();
    }

    MetaModel model = Beans.get(MetaModelRepository.class).findByName(klassName);
    try {
      return Class.forName(model.getFullName());
    } catch (Exception ex) {
    }
    return null;
  }
}
