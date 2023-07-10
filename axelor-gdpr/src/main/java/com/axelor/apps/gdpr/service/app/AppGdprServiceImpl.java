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
package com.axelor.apps.gdpr.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Anonymizer;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.gdpr.exception.GdprExceptionMessage;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppGdpr;
import java.util.Objects;

public class AppGdprServiceImpl implements AppGdprService {
  @Override
  public AppGdpr getAppGDPR() {
    return Query.of(AppGdpr.class).fetchOne();
  }

  @Override
  public Anonymizer getGdprAnonymizer() throws AxelorException {
    Anonymizer anonymizer = getAppGDPR().getAnonymizer();
    if (Objects.isNull(anonymizer)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(GdprExceptionMessage.APP_GDPR_NO_ANONYMIZER_FOUND));
    }
    return anonymizer;
  }
}
