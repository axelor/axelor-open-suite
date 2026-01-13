/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.service;

import com.axelor.auth.db.User;
import com.axelor.dms.db.DMSFile;
import com.google.inject.persist.Transactional;

public class UserDMSFileServiceImpl implements UserDMSFileService {

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addDMSFileToFavorites(DMSFile dmsFile, User user) {
    if (dmsFile.getIsDirectory()) {
      user.addFavouriteFolderSetItem(dmsFile);
    } else {
      user.addFavouriteFileSetItem(dmsFile);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void removeDMSFileFromFavorites(DMSFile dmsFile, User user) {
    if (dmsFile.getIsDirectory()) {
      user.removeFavouriteFolderSetItem(dmsFile);
    } else {
      user.removeFavouriteFileSetItem(dmsFile);
    }
  }
}
