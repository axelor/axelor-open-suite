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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
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

  @Inject protected PartnerRepository partnerRepo;

  @Inject protected MetaFiles metaFiles;

  @Inject protected PartnerService partnerService;

  public Object importPartner(Object bean, Map<String, Object> values) {

    assert bean instanceof Partner;

    Partner partner = (Partner) bean;

    partnerService.setPartnerFullName(partner);

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
