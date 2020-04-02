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
package com.axelor.meta.service;

import com.axelor.app.AppSettings;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaGroupMenuAssistant;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaGroupMenuAssistantRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaGroupMenuAssistantService {

  @Inject private MetaGroupMenuAssistantRepository menuAssistantRepository;

  @Inject private GroupRepository groupRepository;

  @Inject private MetaMenuRepository menuRepository;

  private List<String> badGroups = new ArrayList<>();

  private String errorLog = "";

  private List<MetaMenu> updatedMenu = new ArrayList<>();

  private ResourceBundle bundle;

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String getFileName(MetaGroupMenuAssistant groupMenuAssistant) {

    String userCode = groupMenuAssistant.getCreatedBy().getCode();
    String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    String fileName = "GroupMenu" + "-" + userCode + "-" + dateString + ".csv";

    return fileName;
  }

  private void setBundle(Locale locale) {
    bundle = I18n.getBundle(locale);
  }

  private ResourceBundle getBundle() {
    if (bundle == null) {
      bundle = I18n.getBundle(new Locale("en"));
    }
    return bundle;
  }

  public void createGroupMenuFile(MetaGroupMenuAssistant groupMenuAssistant) {

    setBundle(new Locale(groupMenuAssistant.getLanguage()));
    AppSettings appSettings = AppSettings.get();
    File groupMenuFile =
        new File(appSettings.get("file.upload.dir"), getFileName(groupMenuAssistant));

    try {

      List<String[]> rows = createHeader(groupMenuAssistant);

      addMenuRows(groupMenuAssistant, rows);

      addGroupAccess(rows);

      try (CSVWriter csvWriter =
          new CSVWriter(new FileWriterWithEncoding(groupMenuFile, "utf-8"), ';')) {
        csvWriter.writeAll(rows);
      }

      createMetaFile(groupMenuFile, groupMenuAssistant);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private List<String[]> createHeader(MetaGroupMenuAssistant groupMenuAssistant)
      throws IOException {

    MetaFile metaFile = groupMenuAssistant.getMetaFile();
    List<String[]> rows = new ArrayList<String[]>();
    if (metaFile != null) {
      File csvFile = MetaFiles.getPath(metaFile).toFile();
      logger.debug("File name: {}", csvFile.getAbsolutePath());
      logger.debug("File length: {}", csvFile.length());
      try (CSVReader csvReader =
          new CSVReader(
              new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8), ';')) {
        rows = csvReader.readAll();
        logger.debug("Rows size: {}", rows.size());
      }
    }
    if (!rows.isEmpty()) {
      rows.set(0, getGroupRow(rows.get(0), groupMenuAssistant.getGroupSet()));
    } else {
      rows.add(getGroupRow(null, groupMenuAssistant.getGroupSet()));
    }

    return rows;
  }

  private String[] getGroupRow(String[] row, Set<Group> groupSet) throws IOException {

    List<String> groupList = new ArrayList<String>();
    if (row != null) {
      groupList.addAll(Arrays.asList(row));
    } else {
      groupList.add(getBundle().getString("Name"));
      groupList.add(getBundle().getString("Title"));
    }

    for (Group group : groupSet) {
      String code = group.getCode();
      if (!groupList.contains(code)) {
        groupList.add(code);
      }
    }

    return groupList.toArray(new String[groupList.size()]);
  }

  private void addMenuRows(MetaGroupMenuAssistant groupMenuAssistant, List<String[]> rows) {
    List<String> names = new ArrayList<>();
    String[] groupRow = rows.get(0);
    rows.remove(0);

    for (String[] row : rows) {
      names.add(row[0]);
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

    Collections.sort(
        rows,
        new Comparator<String[]>() {

          @Override
          public int compare(String[] first, String[] second) {
            return first[0].compareTo(second[0]);
          }
        });

    rows.add(0, groupRow);
  }

  private void addGroupAccess(List<String[]> rows) {

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
      }
    }
  }

  @Transactional
  public void createMetaFile(File groupMenuFile, MetaGroupMenuAssistant groupMenuAssistant) {

    MetaFile metaFile = new MetaFile();
    metaFile.setFileName(groupMenuFile.getName());
    metaFile.setFilePath(groupMenuFile.getName());
    groupMenuAssistant.setMetaFile(metaFile);

    menuAssistantRepository.save(groupMenuAssistant);
  }

  private Map<String, Group> checkGroups(String[] groupRow) {

    Map<String, Group> groupMap = new HashMap<String, Group>();

    for (Integer glen = 2; glen < groupRow.length; glen++) {

      Group group = groupRepository.all().filter("self.code = ?1", groupRow[glen]).fetchOne();

      if (group == null) {
        badGroups.add(groupRow[glen]);
      } else {
        groupMap.put(groupRow[glen], group);
      }
    }

    if (!badGroups.isEmpty()) {
      errorLog += "\n" + String.format(I18n.get(IMessage.NO_GROUP), badGroups);
    }

    return groupMap;
  }

  public String importGroupMenu(MetaGroupMenuAssistant groupMenuAssistant) {

    try {
      setBundle(new Locale(groupMenuAssistant.getLanguage()));
      MetaFile metaFile = groupMenuAssistant.getMetaFile();
      File csvFile = MetaFiles.getPath(metaFile).toFile();

      try (CSVReader csvReader =
          new CSVReader(
              new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8), ';')) {

        String[] groupRow = csvReader.readNext();
        if (groupRow == null || groupRow.length < 3) {
          return I18n.get(IMessage.BAD_FILE);
        }

        Map<String, Group> groupMap = checkGroups(groupRow);
        Group admin = groupRepository.findByCode("admins");
        importMenus(csvReader, groupRow, groupMap, admin);
      }

      saveMenus();

    } catch (Exception e) {
      TraceBackService.trace(e);
      errorLog += "\n" + String.format(I18n.get(IMessage.ERR_IMPORT_WITH_MSG), e.getMessage());
    }

    return errorLog;
  }

  @Transactional
  public void saveMenus() {

    for (MetaMenu menu : updatedMenu) {
      menuRepository.save(menu);
    }
  }

  private void importMenus(
      CSVReader csvReader, String[] groupRow, Map<String, Group> groupMap, Group admin)
      throws IOException {

    String[] row = csvReader.readNext();
    if (row == null) {
      return;
    }

    List<MetaMenu> menus =
        menuRepository.all().filter("self.name = ?1", row[0]).order("-priority").fetch();

    if (menus.isEmpty()) {
      errorLog += "\n" + String.format(I18n.get(IMessage.NO_MENU), row[0]);
      return;
    }

    Iterator<MetaMenu> menuIter = menus.iterator();

    while (menuIter.hasNext()) {

      MetaMenu menu = menuIter.next();

      boolean noAccess = true;

      for (Integer mIndex = 2; mIndex < row.length; mIndex++) {

        String groupCode = groupRow[mIndex];

        if (groupMap.containsKey(groupCode)) {
          Group group = groupMap.get(groupCode);
          if (row[mIndex].equalsIgnoreCase("x")) {
            noAccess = false;
            menu.addGroup(group);
            if (!updatedMenu.contains(menu)) {
              updatedMenu.add(menu);
            }
          } else if (menu.getGroups().contains(group)) {
            menu.removeGroup(group);
            if (!updatedMenu.contains(menu)) {
              updatedMenu.add(menu);
            }
          }
        }
      }

      if (noAccess && admin != null) {
        menu.addGroup(admin);
      }
    }

    importMenus(csvReader, groupRow, groupMap, admin);
  }
}
