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
package com.axelor.meta.service;

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.common.csv.CSVFile;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaGroupMenuAssistant;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaGroupMenuAssistantRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaGroupMenuAssistantService {

  protected MetaGroupMenuAssistantRepository menuAssistantRepository;
  protected GroupRepository groupRepository;
  protected MetaMenuRepository menuRepository;
  protected RoleRepository roleRepository;
  protected MetaFiles metaFiles;

  @Inject
  public MetaGroupMenuAssistantService(
      MetaGroupMenuAssistantRepository menuAssistantRepository,
      GroupRepository groupRepository,
      MetaMenuRepository menuRepository,
      RoleRepository roleRepository,
      MetaFiles metaFiles) {
    this.menuAssistantRepository = menuAssistantRepository;
    this.groupRepository = groupRepository;
    this.menuRepository = menuRepository;
    this.roleRepository = roleRepository;
    this.metaFiles = metaFiles;
  }

  private List<String> badGroups = new ArrayList<>();

  private List<String> badRoles = new ArrayList<>();

  private String errorLog = "";

  private Set<MetaMenu> updatedMenus = Sets.newHashSet();

  private ResourceBundle bundle;

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected String getFileName(MetaGroupMenuAssistant groupMenuAssistant) {

    String userCode = groupMenuAssistant.getCreatedBy().getCode();
    String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    return "GroupMenu" + "-" + userCode + "-" + dateString + ".csv";
  }

  protected MetaFile getMetaFile(String fileName, File groupMenuFile) throws IOException {
    MetaFile metaFile = new MetaFile();
    metaFile.setFileName(fileName);
    return metaFiles.upload(groupMenuFile, metaFile);
  }

  protected void setBundle(Locale locale) {
    bundle = I18n.getBundle(locale);
  }

  protected ResourceBundle getBundle() {
    if (bundle == null) {
      bundle = I18n.getBundle(AppFilter.getLocale());
    }
    return bundle;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createGroupMenuFile(MetaGroupMenuAssistant groupMenuAssistant) throws IOException {

    setBundle(new Locale(groupMenuAssistant.getLanguage()));
    File groupMenuFile = MetaFiles.createTempFile("MenuGroup", ".csv").toFile();

    try {

      List<String[]> rows = createHeader(groupMenuAssistant);

      addMenuRows(groupMenuAssistant, rows);

      addGroupAccess(rows);

      CSVFile csvFormat = CSVFile.DEFAULT.withDelimiter(';').withQuoteAll();
      try (CSVPrinter printer = csvFormat.write(groupMenuFile)) {
        printer.printRecords(rows);
      }

      groupMenuAssistant.setMetaFile(getMetaFile(getFileName(groupMenuAssistant), groupMenuFile));
      menuAssistantRepository.save(groupMenuAssistant);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private List<String[]> createHeader(MetaGroupMenuAssistant groupMenuAssistant)
      throws IOException {

    MetaFile metaFile = groupMenuAssistant.getMetaFile();
    List<String[]> rows = new ArrayList<>();
    if (metaFile != null) {
      File csvFile = MetaFiles.getPath(metaFile).toFile();
      logger.debug("File name: {}", csvFile.getAbsolutePath());
      logger.debug("File length: {}", csvFile.length());
      CSVFile csvFormat = CSVFile.DEFAULT.withDelimiter(';').withQuoteAll();
      CSVParser csvParser = csvFormat.parse(csvFile, StandardCharsets.UTF_8);
      for (CSVRecord record : csvParser.getRecords()) {
        rows.add(CSVFile.values(record));
      }
      logger.debug("Rows size: {}", rows.size());
    }

    if (!rows.isEmpty()) {
      rows.set(0, getGroupRow(rows.get(0), groupMenuAssistant));
    } else {
      rows.add(getGroupRow(null, groupMenuAssistant));
    }

    return rows;
  }

  private String[] getGroupRow(String[] row, MetaGroupMenuAssistant groupMenuAssistant) {

    List<String> groupList = new ArrayList<>();
    if (row != null) {
      groupList.addAll(Arrays.asList(row));
    } else {
      groupList.add(getBundle().getString("Name"));
      groupList.add(getBundle().getString("Title"));
    }

    for (Group group : groupMenuAssistant.getGroupSet()) {
      String code = group.getCode();
      if (!groupList.contains(code)) {
        groupList.add(code);
      }
    }

    for (Role role : groupMenuAssistant.getRoleSet()) {
      String name = role.getName();
      if (!groupList.contains(name)) {
        groupList.add(name);
      }
    }

    return groupList.toArray(new String[groupList.size()]);
  }

  protected void addMenuRows(MetaGroupMenuAssistant groupMenuAssistant, List<String[]> rows) {
    String[] groupRow = rows.get(0);
    rows.remove(0);
    Set<String> names = new HashSet<>();

    for (String[] line : rows) {
      names.add(line[0]);
    }

    for (MetaMenu metaMenu : groupMenuAssistant.getMenuSet()) {

      String name = metaMenu.getName();

      if (names.contains(name)) {
        continue;
      }

      String title = metaMenu.getTitle();
      String translation = getBundle().getString(title);

      if (!Strings.isNullOrEmpty(translation)) {
        title = translation;
      }

      String[] menu = new String[groupRow.length];
      menu[0] = name;
      menu[1] = title;
      rows.add(menu);
    }

    Collections.sort(rows, (first, second) -> first[0].compareTo(second[0]));

    rows.add(0, groupRow);
  }

  protected void addGroupAccess(List<String[]> rows) {

    ListIterator<String[]> rowIter = rows.listIterator();

    String[] header = rowIter.next();

    while (rowIter.hasNext()) {
      String[] row = rowIter.next();
      MetaMenu menu =
          menuRepository.all().filter("self.name = ?1", row[0]).order("-priority").fetchOne();

      if (row.length < header.length) {
        row = Arrays.copyOf(row, header.length);
        rowIter.set(row);
      }

      for (int i = 2; i < header.length; i++) {
        for (Group group : menu.getGroups()) {
          if (header[i] != null && header[i].equals(group.getCode())) {
            row[i] = "x";
          }
        }
        for (Role role : menu.getRoles()) {
          if (header[i] != null && header[i].equals(role.getName())) {
            row[i] = "x";
          }
        }
      }
    }
  }

  private Map<String, Object> checkGroups(String[] groupRow) {

    Map<String, Object> groupMap = new HashMap<>();

    for (Integer glen = 2; glen < groupRow.length; glen++) {

      Group group = groupRepository.all().filter("self.code = ?1", groupRow[glen]).fetchOne();

      if (group == null) {
        badGroups.add(groupRow[glen]);
      } else {
        groupMap.put(groupRow[glen], group);
      }
    }

    return groupMap;
  }

  private Map<String, Role> checkRoles(String[] roleRow) {

    Map<String, Role> roleMap = new HashMap<>();

    for (Integer rlen = 2; rlen < roleRow.length; rlen++) {

      Role role = roleRepository.all().filter("self.name = ?1", roleRow[rlen]).fetchOne();

      if (role == null) {
        badRoles.add(roleRow[rlen]);
      } else {
        roleMap.put(roleRow[rlen], role);
      }
    }

    return roleMap;
  }

  public String importGroupMenu(MetaGroupMenuAssistant groupMenuAssistant) {
    try {
      MetaFile metaFile = groupMenuAssistant.getMetaFile();

      if (metaFile != null) {
        File csvFile = MetaFiles.getPath(metaFile).toFile();

        CSVFile csvFormat =
            CSVFile.DEFAULT.withDelimiter(';').withQuoteAll().withFirstRecordAsHeader();
        CSVParser csvParser = csvFormat.parse(csvFile);

        setBundle(new Locale(groupMenuAssistant.getLanguage()));

        List<String> headerNames = csvParser.getHeaderNames();
        String[] groupRow = headerNames.toArray(new String[headerNames.size()]);
        if (groupRow == null || groupRow.length < 3) {
          return I18n.get(IMessage.BAD_FILE);
        }

        Map<String, Object> groupMap = checkGroups(groupRow);
        groupMap.putAll(checkRoles(groupRow));
        badGroups.removeAll(groupMap.keySet());
        badRoles.removeAll(groupMap.keySet());
        if (!badGroups.isEmpty()) {
          errorLog += "\n" + String.format(I18n.get(IMessage.NO_GROUP), badGroups);
        }
        if (!badRoles.isEmpty()) {
          errorLog += "\n" + String.format(I18n.get(IMessage.NO_ROLE), badRoles);
        }
        Group admin = groupRepository.findByCode("admins");

        for (CSVRecord record : csvParser.getRecords()) {
          importMenus(CSVFile.values(record), groupRow, groupMap, admin);
        }
        saveMenus();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      errorLog += "\n" + String.format(I18n.get(IMessage.ERR_IMPORT_WITH_MSG), e.getMessage());
    }

    return errorLog;
  }

  @Transactional
  public void saveMenus() {

    for (MetaMenu menu : updatedMenus) {
      menuRepository.save(menu);
    }
  }

  protected void importMenus(
      String[] row, String[] groupRow, Map<String, Object> groupMap, Group admin) {

    List<MetaMenu> menus =
        menuRepository.all().filter("self.name = ?1", row[0]).order("-priority").fetch();

    if (menus.isEmpty()) {
      errorLog += "\n" + String.format(I18n.get(IMessage.NO_MENU), row[0]);
      return;
    }

    for (MetaMenu menu : menus) {
      boolean noAccess = true;

      for (Integer mIndex = 2; mIndex < row.length; mIndex++) {

        String code = groupRow[mIndex];
        Object object = groupMap.get(code);

        Role role = null;
        Group group = null;
        if (object instanceof Group) {
          group = (Group) object;
        } else if (object instanceof Role) {
          role = (Role) object;
        }

        if (row[mIndex].equalsIgnoreCase("x")) {
          noAccess = false;
          if (group != null) {
            menu.addGroup(group);
          }
          if (role != null) {
            menu.addRole(role);
          }
          updatedMenus.add(menu);
        } else if (group != null && menu.getGroups().contains(group)) {
          menu.removeGroup(group);
          updatedMenus.add(menu);
        } else if (role != null && menu.getRoles().contains(role)) {
          menu.removeRole(role);
          updatedMenus.add(menu);
        }
      }

      if (noAccess && admin != null) {
        menu.addGroup(admin);
      }
    }
  }
}
