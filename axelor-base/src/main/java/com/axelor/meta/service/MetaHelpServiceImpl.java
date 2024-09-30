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
package com.axelor.meta.service;

import com.axelor.apps.base.db.Localization;
import com.axelor.meta.db.MetaHelp;
import com.axelor.meta.db.repo.MetaHelpRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MetaHelpServiceImpl implements MetaHelpService {

  protected MetaHelpRepository metaHelpRepository;

  @Inject
  public MetaHelpServiceImpl(MetaHelpRepository metaHelpRepository) {
    this.metaHelpRepository = metaHelpRepository;
  }

  @Override
  public void findOrCreateMetaHelp(Localization localization) {
    String code = localization.getCode();
    String lang = code.substring(0, 2);
    String language = code.toLowerCase().replace('_', '-');

    List<MetaHelp> metaHelpList = getMetaHelpList(lang);
    metaHelpList.stream()
        .filter(metaHelp -> !findMetaHelp(metaHelp, language))
        .forEach(
            metaHelp -> {
              createMetaHelp(metaHelp, language);
            });
  }

  protected List<MetaHelp> getMetaHelpList(String language) {
    return metaHelpRepository
        .all()
        .filter("self.language = :language")
        .bind("language", language)
        .fetch();
  }

  protected boolean findMetaHelp(MetaHelp metaHelp, String language) {
    String modelFilter = metaHelp.getModel() == null ? "self.model IS NULL" : "self.model = :model";

    return metaHelpRepository
            .all()
            .filter(
                "self.language = :language AND self.field = :field AND self.view = :view AND "
                    + modelFilter)
            .bind("language", language)
            .bind("field", metaHelp.getField())
            .bind("model", metaHelp.getModel())
            .bind("view", metaHelp.getView())
            .fetchOne()
        != null;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createMetaHelp(MetaHelp metaHelp, String language) {
    MetaHelp newMetaHelp = metaHelpRepository.copy(metaHelp, false);
    newMetaHelp.setLanguage(language);
    metaHelpRepository.save(newMetaHelp);
  }
}
