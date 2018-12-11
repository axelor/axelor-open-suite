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
package com.axelor.studio.service;

import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.AbstractView;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

public class ViewWriterService {

  /**
   * Write view and action xml into new viewFile.
   *
   * @param modelName Name of model
   * @param views List of AbstractView of models.
   * @param actions List of Actions of model.
   * @throws IOException
   * @throws JAXBException
   */
  public void writeView(File viewDir, String modelName, AbstractView view, List<Action> actions)
      throws IOException, JAXBException {

    ObjectViews objectViews = new ObjectViews();

    File viewFile = new File(viewDir, modelName + ".xml");
    if (viewFile.exists()) {
      String xml = Files.asCharSource(viewFile, Charsets.UTF_8).read();
      if (!Strings.isNullOrEmpty(xml)) {
        objectViews = XMLViews.fromXML(xml);
      }
    }

    if (objectViews == null) {
      objectViews = new ObjectViews();
    }

    if (view != null) {
      List<AbstractView> views = filterOldViews(view, objectViews.getViews());
      objectViews.setViews(views);
    }

    if (actions != null && !actions.isEmpty()) {
      actions = filterOldActions(actions, objectViews.getActions());
      objectViews.setActions(actions);
    }

    if (view != null || (actions != null && !actions.isEmpty())) {
      XMLViews.marshal(objectViews, new FileWriter(viewFile));
    }
  }

  /**
   * Write xml file with proper xml header.
   *
   * @param file File to write.
   * @param xmlWriter XmlWriter containing view xml.
   * @throws IOException
   */
  public void writeFile(File file, StringWriter xmlWriter) throws IOException {

    FileWriter fileWriter = new FileWriter(file);
    fileWriter.write(prepareXML(xmlWriter.toString()));
    fileWriter.close();
  }

  /**
   * Replace old views from extracted views of existing view file with new AbstractViews.
   *
   * @param views List of new AbstractView created.
   * @param oldViews Old List of AbstractView from existing view file.
   * @return Updated list of old and new AbstractView.
   */
  private List<AbstractView> filterOldViews(AbstractView view, List<AbstractView> oldViews) {

    if (oldViews == null) {
      oldViews = new ArrayList<AbstractView>();
      oldViews.add(view);
      return oldViews;
    }
    Iterator<AbstractView> oldViewIter = oldViews.iterator();
    while (oldViewIter.hasNext()) {
      AbstractView oldView = oldViewIter.next();
      if (oldView.getName().equals(view.getName())) {
        oldViews.remove(oldView);
        break;
      }
    }

    oldViews.add(view);

    return oldViews;
  }

  /**
   * Replace old Action from ViewFile with New Action.
   *
   * @param actions List of new Actions created
   * @param oldActions List of old Actions extracted from file.
   * @return List of updated list containing both old and new actions.
   */
  private List<Action> filterOldActions(List<Action> actions, List<Action> oldActions) {

    if (oldActions == null) {
      return actions;
    }

    for (Action action : actions) {

      if (action == null) {
        continue;
      }

      Iterator<Action> oldActionIter = oldActions.iterator();
      while (oldActionIter.hasNext()) {
        Action oldAction = oldActionIter.next();
        if (oldAction.getName().equals(action.getName())) {
          oldActions.remove(oldAction);
          break;
        }
      }
    }

    oldActions.addAll(actions);

    return oldActions;
  }

  /**
   * Method to format xml string with proper header.
   *
   * @param xml Xml string to use.
   * @return Formatted xml.
   */
  public String prepareXML(String xml) {

    StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<object-views")
        .append(" xmlns='")
        .append(ObjectViews.NAMESPACE)
        .append("'")
        .append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
        .append(" xsi:schemaLocation='")
        .append(ObjectViews.NAMESPACE)
        .append(" ")
        .append(ObjectViews.NAMESPACE + "/" + "object-views_" + ObjectViews.VERSION + ".xsd")
        .append("'")
        .append(">\n")
        .append(xml)
        .append("\n</object-views>");

    return sb.toString();
  }
}
