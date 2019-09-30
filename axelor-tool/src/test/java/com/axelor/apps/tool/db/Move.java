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
package com.axelor.apps.tool.db;

import com.axelor.db.JPA;
import com.axelor.db.JpaModel;
import com.axelor.db.Query;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "TEST_MOVE")
public class Move extends JpaModel {

  @OneToMany(
    fetch = FetchType.LAZY,
    mappedBy = "move",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<MoveLine> moveLines;

  @ManyToOne(
    fetch = FetchType.LAZY,
    cascade = {CascadeType.PERSIST, CascadeType.MERGE}
  )
  private Invoice invoice;

  public List<MoveLine> getMoveLines() {
    return moveLines;
  }

  public void setMoveLines(List<MoveLine> moveLines) {
    this.moveLines = moveLines;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice invoice) {
    this.invoice = invoice;
  }

  public Move persist() {
    return JPA.persist(this);
  }

  public Move merge() {
    return JPA.merge(this);
  }

  public Move save() {
    return JPA.save(this);
  }

  public void remove() {
    JPA.remove(this);
  }

  public void refresh() {
    JPA.refresh(this);
  }

  public void flush() {
    JPA.flush();
  }

  public static Move find(Long id) {
    return JPA.find(Move.class, id);
  }

  public static Query<Move> all() {
    return JPA.all(Move.class);
  }
}
