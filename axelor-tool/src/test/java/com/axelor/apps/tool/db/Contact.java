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
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.VirtualColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CONTACT_CONTACT")
public class Contact extends JpaModel {

  @ManyToOne(
    cascade = {CascadeType.PERSIST, CascadeType.MERGE},
    fetch = FetchType.LAZY
  )
  private Title title;

  @NotNull private String firstName;

  @NotNull private String lastName;

  @Widget(search = {"firstName", "lastName"})
  @NameColumn
  @VirtualColumn
  @Access(AccessType.PROPERTY)
  private String fullName;

  @NotNull private String email;

  private String phone;

  private LocalDate dateOfBirth;

  @OneToMany(
    mappedBy = "contact",
    cascade = CascadeType.ALL,
    fetch = FetchType.LAZY,
    orphanRemoval = true
  )
  private List<Address> addresses;

  @ManyToMany(
    cascade = {CascadeType.PERSIST, CascadeType.MERGE},
    fetch = FetchType.LAZY
  )
  private Set<Group> groups;

  @Widget(title = "Photo", help = "Max size 4MB.")
  @Lob
  @Basic(fetch = FetchType.LAZY)
  private byte[] image;

  @Widget(multiline = true)
  private String notes;

  private BigDecimal payeurQuality;

  @Widget(selection = "select.language")
  private String language;

  public Contact() {}

  public Contact(String firstName) {
    this.firstName = firstName;
  }

  public Contact(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Title getTitle() {
    return title;
  }

  public void setTitle(Title title) {
    this.title = title;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFullName() {
    return fullName = calculateFullName();
  }

  protected String calculateFullName() {
    fullName = firstName + " " + lastName;
    if (this.title != null) {
      return this.title.getName() + " " + fullName;
    }
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public Group getGroup(int index) {
    if (groups == null) return null;
    return Lists.newArrayList(groups).get(index);
  }

  public Set<Group> getGroups() {
    return groups;
  }

  public void setGroups(Set<Group> groups) {
    this.groups = groups;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public BigDecimal getPayeurQuality() {
    return payeurQuality;
  }

  public void setPayeurQuality(BigDecimal payeurQuality) {
    this.payeurQuality = payeurQuality;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLanguageTitle() {
    //		MetaSelectItem item = MetaSelectItem
    //				.filter("self.select.name = ?1 AND self.value = ?2",
    //						"select.language", this.language).fetchOne();
    //
    //		if (item != null) {
    //			return item.getTitle();
    //		}

    return "french";
  }

  @Override
  public String toString() {
    ToStringHelper tsh = MoreObjects.toStringHelper(getClass());

    tsh.add("id", getId());
    tsh.add("fullName", getFirstName());
    tsh.add("email", getEmail());

    return tsh.omitNullValues().toString();
  }

  public Contact find(Long id) {
    return JPA.find(Contact.class, id);
  }

  public static Contact edit(Map<String, Object> values) {
    return JPA.edit(Contact.class, values);
  }

  public static Query<Contact> all() {
    return JPA.all(Contact.class);
  }
}
