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
package com.axelor.studio.service.importer;

import com.axelor.common.Inflector;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSequence;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSequenceRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.CommonService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormulaImporterService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ActionBuilderRepository actionBuilderRepo;

  @Inject private MetaSequenceRepository metaSequenceRepo;

  @Inject private ViewItemRepository viewItemRepo;

  @Transactional
  public List<String> importFormula(
      Map<String, String> valMap, ViewBuilder viewBuilder, ViewItem viewItem) {

    String formula = valMap.get(CommonService.FORMULA);
    String event = valMap.get(CommonService.EVENT);

    if (formula == null || event == null) {
      return new ArrayList<String>();
    }

    String name = "action-" + viewBuilder.getName() + "-set-" + viewItem.getName();
    MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(viewBuilder.getModel());

    ActionBuilder actionBuilder = getActionBuilder(viewBuilder, name, 1);

    List<String> formulas = getFormulas(formula);

    log.debug("Formulas: {}", formulas);

    ActionBuilderLine line = createActionLine(actionBuilder, formulas, metaModel, viewItem);

    if (line != null) {
      actionBuilder.addLine(line);
      if (event.trim().equals("new") && line.getConditionText() == null) {
        //        viewItem.setDefaultValue(line.getValue());
      } else {
        actionBuilderRepo.save(actionBuilder);
        return addEvents(viewBuilder, event, name);
      }
    }

    return new ArrayList<String>();
  }

  private ActionBuilderLine createActionLine(
      ActionBuilder actionBuilder, List<String> formulas, MetaModel metaModel, ViewItem viewItem) {

    ActionBuilderLine line = null;

    for (int i = 0; i < formulas.size(); i++) {

      String expr = formulas.get(i).trim();

      if (i % 2 == 0) {
        line = new ActionBuilderLine();
        //        line.setMetaField(viewItem.getMetaField());
        line.setName(viewItem.getName());
        if (expr.startsWith("seq(")) {
          String seqName = createSequence(expr, metaModel, viewItem);
          if (seqName != null) {
            line.setValue(
                "com.axelor.creator.service.builder.ActionBuilderService.getSequence('"
                    + seqName
                    + "')");
          }
        } else if (expr.startsWith("sum(")) {
          String[] sum = expr.split(":");
          if (sum.length > 1) {
            line.setValue(sum[0] + ")");
            line.setFilter(sum[1].substring(0, sum[1].length() - 1));
          } else {
            line.setValue(expr);
          }
        }
      } else {
        line.setConditionText(expr);
      }
    }

    return line;
  }

  @Transactional
  public List<String> importValidation(
      Map<String, String> valMap, String type, ViewBuilder viewBuilder, MetaModel model) {

    String formula = valMap.get(CommonService.FORMULA);
    String event = valMap.get(CommonService.EVENT);

    if (Strings.isNullOrEmpty(formula) || Strings.isNullOrEmpty(event)) {
      return new ArrayList<String>();
    }

    //    String name =
    //        "action-"
    //            + viewBuilder.getMetaModule().getName()
    //            + "-"
    //            + viewBuilder.getName()
    //            + "-"
    //            + type;
    String name = "action-" + viewBuilder.getName() + "-" + viewBuilder.getName() + "-" + type;

    ActionBuilder action = getActionBuilder(viewBuilder, name, 5);

    String[] items = formula.split(",");
    ActionBuilderLine line = new ActionBuilderLine();
    List<String> conditions = new ArrayList<String>();
    for (int i = 0; i < items.length; i++) {
      if (i % 2 == 0) {
        String condition = null;
        if (items[i].equals("else")) {
          condition = "!(" + Joiner.on(" && ").join(conditions) + ")";
        }
        line.setConditionText(condition);
      } else {
        line.setValidationTypeSelect(type);
        line.setValidationMsg(items[i]);
        action.addLine(line);
        line = new ActionBuilderLine();
      }
    }

    if (action.getLines() != null && !action.getLines().isEmpty()) {
      actionBuilderRepo.save(action);
    }

    return addEvents(viewBuilder, event, name);
  }

  private List<String> getFormulas(String formula) {

    List<String> formulas = new ArrayList<String>();

    for (String expr : formula.split(",")) {
      if (expr.startsWith("max:")) continue;
      if (expr.startsWith("min:")) continue;
      if (expr.startsWith("mappedBy:")) continue;
      formulas.add(expr);
    }

    return formulas;
  }

  private ActionBuilder getActionBuilder(ViewBuilder viewBuilder, String name, int type) {

    //    ActionBuilder actionBuilder =
    //        actionBuilderRepo
    //            .all()
    //            .filter(
    //                "self.name = ?1 and self.metaModule.name = ?2",
    //                name,
    //                viewBuilder.getMetaModule().getName())
    //            .fetchOne();
    //    if (actionBuilder == null) {
    //      actionBuilder = new ActionBuilder(name);
    //      actionBuilder.setMetaModule(viewBuilder.getMetaModule());
    //    } else {
    //      actionBuilder.clearLines();
    //    }
    //
    //    actionBuilder.setTypeSelect(type);
    //    actionBuilder.setModel(viewBuilder.getMetaModel().getFullName());

    return null;
  }

  @Transactional
  public List<String> addEvents(ViewBuilder viewBuilder, String events, String action) {

    List<String> eventList = new ArrayList<String>();
    eventList.add(action);

    for (String event : events.split(",")) {

      if (event.equals("new")) {
        continue;
      }
      if (event.equals("save")) {
        //        String onSave = viewBuilder.getOnSave();
        //        viewBuilder.setOnSave(ActionCreatorService.getUpdatedAction(onSave, action));
      } else {
        ViewItem viewItem =
            viewItemRepo
                .all()
                .filter(
                    "self.name = ?1 "
                        + "and (self.viewPanel.viewBuilder.id = ?2 "
                        + "OR self.viewPanel.viewBuilderSideBar.id = ?2 "
                        + "OR self.viewBuilderToolbar.id = ?2)",
                    event,
                    viewBuilder.getId())
                .fetchOne();
        log.debug("View item found: {}", viewItem);
        if (viewItem != null) {
          //          if (viewItem.getTypeSelect() == 0) {
          //            viewItem.setOnChange(
          //                ActionCreatorService.getUpdatedAction(viewItem.getOnChange(), action));
          //          } else if (viewItem.getTypeSelect() == 1) {
          //            viewItem.setOnClick(
          //                ActionCreatorService.getUpdatedAction(viewItem.getOnClick(), action));
          //          }
          viewItemRepo.save(viewItem);
        } else {
          eventList.add(event);
        }
      }
    }

    log.debug("Event List: {}", eventList);
    return eventList;
  }

  @Transactional
  public String createSequence(String formula, MetaModel metaModel, ViewItem viewItem) {

    formula = formula.trim().replace("seq(", "");
    if (formula.endsWith(")")) {
      formula = formula.substring(0, formula.length() - 1);
    }
    String[] sequence = formula.split(":");
    if (sequence.length > 1) {
      String model = Inflector.getInstance().dasherize(metaModel.getName()).replace("-", ".");
      String field = Inflector.getInstance().dasherize(viewItem.getName()).replace("-", ".");

      String name = "sequence." + model + "." + field;
      MetaSequence metaSequence = metaSequenceRepo.findByName(name);
      if (metaSequence == null) {
        metaSequence = new MetaSequence(name);
      }
      //      metaSequence.setMetaModel(metaModel);
      metaSequence.setPadding(new Integer(sequence[0]));
      if (sequence.length > 1) {
        metaSequence.setPrefix(sequence[1]);
      }
      if (sequence.length > 2) {
        metaSequence.setSuffix(sequence[2]);
      }

      metaSequence = metaSequenceRepo.save(metaSequence);

      return "sequence." + model + "." + field;
    }

    return null;
  }
}
