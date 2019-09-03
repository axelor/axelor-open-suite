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
package com.axelor.studio.service.builder;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;

public class ActionViewBuilderService {

  private static final String INDENT = "\t";

  @Inject private StudioMetaService metaService;

  public MetaAction build(ActionBuilder builder) {

    List<ActionBuilderView> views = builder.getActionBuilderViews();
    if (views == null || views.isEmpty()) {
      return null;
    }

    StringBuilder xml = new StringBuilder();

    String model = appendBasic(builder, xml);

    appendViews(views, xml);

    appendParams(builder.getViewParams(), xml);

    appendDomain(builder.getDomainCondition(), builder.getIsJson(), xml);

    appendContext(builder, xml);

    xml.append("\n" + "</action-view>");

    return metaService.updateMetaAction(builder.getName(), "action-view", xml.toString(), model);
  }

  private void appendParams(List<ActionBuilderLine> params, StringBuilder xml) {

    if (params == null) {
      return;
    }

    for (ActionBuilderLine param : params) {
      xml.append("\n" + INDENT + "<view-param name=\"" + param.getName() + "\" ");
      xml.append("value=\"" + StringEscapeUtils.escapeXml(param.getValue()) + "\" />");
    }
  }

  private void appendContext(ActionBuilder builder, StringBuilder xml) {
    boolean addJsonCtx = true;
    if (builder.getLines() != null) {
      for (ActionBuilderLine context : builder.getLines()) {
        if (context.getName().contentEquals("jsonModel")) {
          addJsonCtx = false;
        }
        xml.append("\n" + INDENT + "<context name=\"" + context.getName() + "\" ");
        xml.append("expr=\"eval:" + StringEscapeUtils.escapeXml(context.getValue()) + "\" />");
      }
    }

    if (addJsonCtx && builder.getIsJson() && builder.getModel() != null) {
      xml.append("\n" + INDENT + "<context name=\"jsonModel\" ");
      xml.append("expr=\"eval:" + builder.getModel() + "\" />");
    }
  }

  private void appendDomain(String domain, Boolean isJson, StringBuilder xml) {

    if (isJson) {
      String jsonDomain = "self.jsonModel = :jsonModel";
      if (domain == null) {
        domain = jsonDomain;
      } else if (!domain.contains(jsonDomain)) {
        domain = jsonDomain + " AND (" + domain + ")";
      }
    }

    if (domain != null) {
      xml.append("\n" + INDENT + "<domain>" + StringEscapeUtils.escapeXml(domain) + "</domain>");
    }
  }

  private void appendViews(List<ActionBuilderView> views, StringBuilder xml) {

    views.sort(
        new Comparator<ActionBuilderView>() {
          @Override
          public int compare(ActionBuilderView action1, ActionBuilderView action2) {
            return action1.getSequence().compareTo(action2.getSequence());
          }
        });
    for (ActionBuilderView view : views) {
      xml.append("\n" + INDENT + "<view type=\"" + view.getViewType() + "\" ");
      xml.append("name=\"" + view.getViewName() + "\" />");
    }
  }

  private String appendBasic(ActionBuilder builder, StringBuilder xml) {

    xml.append("<action-view name=\"" + builder.getName() + "\" ");
    xml.append("title=\"" + builder.getTitle() + "\" ");
    xml.append("id=\"studio-" + builder.getName() + "\" ");

    String model = MetaJsonRecord.class.getName();
    if (!builder.getIsJson()) {
      model = builder.getModel();
    }
    xml.append("model=\"" + model + "\">");

    return model;
  }
}
