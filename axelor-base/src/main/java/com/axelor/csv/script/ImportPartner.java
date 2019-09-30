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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ImportPartner {

  @Inject private PartnerRepository partnerRepo;

  @Inject private MetaFiles metaFiles;

  public Object importPartner(Object bean, Map<String, Object> values) {

    assert bean instanceof Partner;

    Partner partner = (Partner) bean;

    final Path path = (Path) values.get("__path__");
    String fileName = (String) values.get("picture_fileName");
    if (Strings.isNullOrEmpty((fileName))) {
      return bean;
    }

    final File image = path.resolve(fileName).toFile();

    try {
      final MetaFile metaFile = metaFiles.upload(image);
      partner.setPicture(metaFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bean;
  }

  public Object updateContacts(Object bean, Map<String, Object> values) {

    assert bean instanceof Partner;

    Partner partner = (Partner) bean;
    partner.setContactPartnerSet(new HashSet<Partner>());

    List<? extends Partner> partnerList =
        partnerRepo.all().filter("self.mainPartner.id = ?1", partner.getId()).fetch();
    for (Partner pt : partnerList) partner.getContactPartnerSet().add(pt);

    return partner;
  }
}
