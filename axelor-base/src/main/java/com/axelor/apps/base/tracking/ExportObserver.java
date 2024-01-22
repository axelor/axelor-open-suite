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
package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.RequestEvent;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;

public class ExportObserver {

  void onExport(@Observes @Named(RequestEvent.EXPORT) PostRequest event) throws IOException {

    List<GlobalTrackingConfigurationLine> gtcLines =
        Beans.get(AppBaseService.class).getAppBase().getGlobalTrackingConfigurationLines();

    if (CollectionUtils.isEmpty(gtcLines)) {
      return;
    }
    MetaModel model =
        Beans.get(MetaModelRepository.class)
            .all()
            .filter("self.fullName = ?", event.getRequest().getBeanClass().getName())
            .fetchOne();

    if (gtcLines.stream().anyMatch(l -> l.getMetaModel().equals(model) && l.getTrackExport())) {

      @SuppressWarnings("unchecked")
      final Map<String, Object> data = (Map<String, Object>) event.getResponse().getData();
      if (data == null || data.get("fileName") == null) {
        return;
      }
      final String fileName = (String) data.get("fileName");
      final Path filePath = MetaFiles.findTempFile(fileName);
      MetaFile mf = new MetaFile();
      mf.setFileName(fileName);
      Beans.get(MetaFiles.class).upload(new FileInputStream(filePath.toFile()), mf);
      Beans.get(GlobalTrackingLogService.class).createExportLog(model, mf);
    }
  }
}
