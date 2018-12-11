/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.creator;

import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.service.ViewWriterService;
import com.axelor.studio.service.builder.ActionScriptBuilderService;
import com.axelor.studio.service.builder.ActionViewBuilderService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ActionCreatorService {

  @Inject private ActionBuilderRepository actionBuilderRepo;

  @Inject private ActionViewBuilderService actionViewBuilderService;

  @Inject private ActionScriptBuilderService actionScriptBuilderService;

  @Inject private ViewWriterService viewWriterService;

  private StringBuffer actionBuffer = null;

  public void writeActions(String module, File viewDir) throws IOException {

    actionBuffer = new StringBuffer();
    List<ActionBuilder> actionBuilders =
        actionBuilderRepo.all().filter("self.metaModule.name = ?1", module).fetch();

    processActionBuilder(actionBuilders.iterator());

    File actionFile = new File(viewDir, "Action.xml");
    if (!actionBuilders.isEmpty()) {
      writeActionFile(actionFile);
    }
  }

  private void processActionBuilder(Iterator<ActionBuilder> actionIter) {

    if (!actionIter.hasNext()) {
      return;
    }

    ActionBuilder actionBuilder = actionIter.next();
    Integer actionType = actionBuilder.getTypeSelect();

    if (actionType < 3) {
      actionBuffer.append("\n" + actionViewBuilderService.build(actionBuilder).getXml());
    } else if (actionType == 3) {
      actionBuffer.append("\n" + actionScriptBuilderService.build(actionBuilder).getXml());
    }

    processActionBuilder(actionIter);
  }

  private void writeActionFile(File actionFile) throws IOException {

    FileWriter fileWriter = new FileWriter(actionFile);

    String xml = viewWriterService.prepareXML(actionBuffer.toString());

    fileWriter.write(xml);

    fileWriter.close();
  }

  public static String getUpdatedAction(String oldAction, String action) {

    if (Strings.isNullOrEmpty(oldAction)) {
      return action;
    }
    if (Strings.isNullOrEmpty(action)) {
      return oldAction;
    }

    List<String> oldActions = new ArrayList<String>();
    oldActions.addAll(Arrays.asList(oldAction.split(",")));

    List<String> newActions = new ArrayList<String>();
    newActions.addAll(Arrays.asList(action.split(",")));
    newActions.removeAll(oldActions);

    oldActions.addAll(newActions);

    return Joiner.on(",").join(oldActions);
  }
}
