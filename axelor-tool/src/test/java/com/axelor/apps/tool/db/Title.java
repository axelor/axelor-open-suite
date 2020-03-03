/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CONTACT_TITLE")
public class Title extends Model {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_TITLE_SEQ")
  @SequenceGenerator(
      name = "CONTACT_TITLE_SEQ",
      sequenceName = "CONTACT_TITLE_SEQ",
      allocationSize = 1)
  private Long id;

  @NotNull
  @Column(unique = true)
  private String code;

  @NotNull
  @Column(unique = true)
  private String name;

  public Title(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    ToStringHelper tsh = MoreObjects.toStringHelper(getClass());

    tsh.add("id", getId());
    tsh.add("code", getCode());
    tsh.add("name", getName());

    return tsh.omitNullValues().toString();
  }

  public static Query<Title> all() {
    return JPA.all(Title.class);
  }
}
