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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class SimulatedMoveServiceImpl implements SimulatedMoveService {

  protected MoveRepository moveRepo;
  protected JournalRepository journalRepo;

  @Inject
  public SimulatedMoveServiceImpl(MoveRepository moveRepo, JournalRepository journalRepo) {
    this.moveRepo = moveRepo;
    this.journalRepo = journalRepo;
  }

  public void deactivateSimulatedMoves(Company company) {
    // Removing simulated moves for the company
    Query<Move> queryMove =
        moveRepo
            .all()
            .filter("self.company = :_company AND self.statusSelect = :_status")
            .bind("_company", company)
            .bind("_status", MoveRepository.STATUS_SIMULATED)
            .order("id");

    int fetchLimit = 10;

    List<Move> moveList;
    while (!(moveList = queryMove.fetch(fetchLimit)).isEmpty()) {
      for (Move move : moveList) {
        removeMove(move);
      }
      JPA.clear();
    }

    // Updating journals of the company
    Query<Journal> queryJournal =
        journalRepo
            .all()
            .filter("self.company = :_company and self.authorizeSimulatedMove = true")
            .bind("_company", company)
            .order("id");

    List<Journal> journalList;
    while (!(journalList = queryJournal.fetch(fetchLimit)).isEmpty()) {
      for (Journal journal : journalList) {
        updateJournal(journal);
      }
      JPA.clear();
    }
  }

  @Transactional
  protected void removeMove(Move move) {
    moveRepo.remove(move);
  }

  @Transactional
  protected void updateJournal(Journal journal) {
    journal.setAuthorizeSimulatedMove(false);
    journalRepo.save(journal);
  }
}
