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
package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;

public class JournalControlServiceImpl implements JournalControlService {

  protected MoveRepository moveRepository;

  @Inject
  public JournalControlServiceImpl(MoveRepository journalRepository) {
    this.moveRepository = journalRepository;
  }

  @Override
  public boolean isLinkedToMove(Journal journal) {

    return moveRepository.all().filter("self.journal = ?", journal).count() > 0;
  }
}
