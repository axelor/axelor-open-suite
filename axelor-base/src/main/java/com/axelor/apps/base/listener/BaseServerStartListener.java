/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.listener;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.openapi.AosSwagger;
import com.axelor.common.ObjectUtils;
import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseServerStartListener {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TIMEZONE_SELECT = "company.timezone.select";

  public void startUpEventListener(@Observes StartupEvent startupEvent) {
    // Initialize swagger
    if (Boolean.parseBoolean(AppSettings.get().get("aos.swagger.enable"))) {
      Beans.get(AosSwagger.class).initSwagger();
      log.info("Initialize swagger");
    }

    // Add all timezones
    addTimezoneSelections();
  }

  @Transactional(rollbackOn = Exception.class)
  public void addTimezoneSelections() {

    MetaSelectRepository metaSelectRepo = Beans.get(MetaSelectRepository.class);
    Set<String> timezoneSelectionSet = ZoneId.getAvailableZoneIds();
    if (ObjectUtils.isEmpty(timezoneSelectionSet)) {
      return;
    }
    MetaSelect timezoneSelect = metaSelectRepo.findByName(TIMEZONE_SELECT);
    if (ObjectUtils.notEmpty(timezoneSelect)) {
      Set<String> existingTimezones =
          timezoneSelect.getItems().stream()
              .map(MetaSelectItem::getValue)
              .collect(Collectors.toSet());
      timezoneSelectionSet.removeAll(existingTimezones);
      if (!timezoneSelectionSet.isEmpty()) {
        addMetaSelectItems(timezoneSelectionSet, timezoneSelect);
        log.info("Loaded Timezones");
      }
    }
  }

  protected void addMetaSelectItems(Set<String> metaSelectItemValueSet, MetaSelect metaSelect) {
    for (String itemValue : metaSelectItemValueSet) {
      MetaSelectItem item = new MetaSelectItem();
      item.setTitle(itemValue);
      item.setValue(itemValue);
      item.setSelect(metaSelect);
      metaSelect.addItem(item);
    }
  }
}
