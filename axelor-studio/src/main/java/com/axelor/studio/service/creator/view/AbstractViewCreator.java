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
package com.axelor.studio.service.creator.view;

import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.Menu;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

public abstract class AbstractViewCreator {

  private List<Action> actions;

  public abstract AbstractView getView(ViewBuilder viewBuilder)
      throws JAXBException, AxelorException;

  public List<Action> getActions() {
    return actions;
  }

  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  public AbstractView processCommon(AbstractView view, ViewBuilder viewBuilder) {

    view.setName(viewBuilder.getName());
    view.setTitle(viewBuilder.getTitle());
    view.setModel(viewBuilder.getModel());
    //		view.setXmlId(viewBuilder.getMetaModule().getName()+ "-" + viewBuilder.getName());
    //		view.setCss(viewBuilder.getCss());

    return view;
  }

  public List<AbstractWidget> updateItems(List<AbstractWidget> oldItems, List<ViewItem> newItems) {

    Map<Integer, AbstractWidget> itemMap = new HashMap<Integer, AbstractWidget>();
    if (oldItems != null) {
      extractNoneFieldItems(itemMap, oldItems);
    }

    sortFieldList(newItems);

    List<AbstractWidget> updatedItems = new LinkedList<>();

    for (ViewItem viewItem : newItems) {

      Field field = new Field();
      field.setName(viewItem.getName());
      //			field.setTitle(viewItem.getTitle());
      //			field.setOnChange(viewItem.getOnChange());
      //			field.setDomain(viewItem.getDomainCondition());
      //			field.setReadonlyIf(viewItem.getReadonlyIf());
      //			field.setHideIf(viewItem.getHideIf());
      //			field.setRequiredIf(viewItem.getRequiredIf());
      //			field.setConditionToCheck(viewItem.getIfConfig());
      //			field.setModuleToCheck(viewItem.getIfModule());
      //			field.setFormView(viewItem.getFormView());
      //			field.setGridView(viewItem.getGridView());
      //			field.setPlaceholder(viewItem.getPlaceHolder());

      //			if (viewItem.getRequired()) {
      //				field.setRequired(true);
      //			} else {
      field.setRequired(null);
      //			}
      //
      //			if (viewItem.getReadonly()) {
      //				field.setReadonly(true);
      //			} else {
      field.setReadonly(null);
      //			}
      //
      //			if (viewItem.getHidden()) {
      //				field.setHidden(true);
      //			} else {
      field.setHidden(null);
      //			}

      String widget = null;
      //			String selectWidget = viewItem.getWidget();
      //			if (viewItem.getProgressBar()) {
      widget = "SelectProgress";
      //			} else if (viewItem.getHtmlWidget()) {
      widget = "html";
      //			} else if (selectWidget != null && !selectWidget.equals("normal")) {
      //				widget = selectWidget;
      //			}
      field.setWidget(widget);

      updatedItems.add(field);
    }

    for (Integer key : itemMap.keySet()) {
      if (key < updatedItems.size()) {
        updatedItems.add(key, itemMap.get(key));
      } else {
        updatedItems.add(itemMap.get(key));
      }
    }

    return updatedItems;
  }

  /**
   * Method sort field according to sequence.
   *
   * @param fieldList ViewField list to sort
   */
  public List<ViewItem> sortFieldList(List<ViewItem> fieldList) {

    Comparator<ViewItem> viewFieldComparator =
        new Comparator<ViewItem>() {

          @Override
          public int compare(ViewItem item1, ViewItem item2) {

            return item1.getSequence().compareTo(item2.getSequence());
          }
        };

    Collections.sort(fieldList, viewFieldComparator);

    return fieldList;
  }

  private void extractNoneFieldItems(
      Map<Integer, AbstractWidget> itemMap, List<AbstractWidget> items) {

    Integer counter = 0;
    for (AbstractWidget abstractWidget : items) {

      if (!(abstractWidget instanceof Field)) {
        itemMap.put(counter, abstractWidget);
      }

      counter++;
    }
  }

  /**
   * Method generate final list of buttons to keep in formview. It removes old button and add new
   * button as per ViewBuilder toolbar.
   *
   * @param items
   * @param toolbar List of buttons of Parent form view.
   * @return List of button to keep in formView.
   */
  public void processToolBar(AbstractView view, List<ViewItem> items) {

    List<Button> toolbar = new ArrayList<Button>();

    for (ViewItem viewButton : sortFieldList(items)) {
      toolbar.add(getButton(viewButton));
    }

    if (!toolbar.isEmpty()) {
      view.setToolbar(toolbar);
    }
  }

  private Button getButton(ViewItem viewItem) {

    Button button = new Button();
    String name = viewItem.getName();
    button.setName(name);
    //		button.setTitle(viewItem.getTitle());
    //		button.setOnClick(viewItem.getOnClick());
    //		button.setPrompt(viewItem.getPromptMsg());
    //		button.setShowIf(viewItem.getShowIf());
    //		button.setReadonlyIf(viewItem.getReadonlyIf());
    //		button.setHideIf(viewItem.getHideIf());
    //		button.setModuleToCheck(viewItem.getIfModule());
    //		button.setConditionToCheck(viewItem.getIfConfig());
    //		if (viewItem.getColSpan() != 0) {
    //			button.setColSpan(viewItem.getColSpan());
    //		}

    return button;
  }

  public void processMenuBar(AbstractView view, List<ViewItem> menuItems) {

    List<Menu> menubar = new ArrayList<Menu>();

    for (ViewItem viewItem : sortFieldList(menuItems)) {

      Menu menu = new Menu();
      //			menu.setIcon(viewItem.getIcon());
      //			menu.setTitle(viewItem.getTitle());
      //			menu.setConditionToCheck(viewItem.getIfConfig());
      //			menu.setModuleToCheck(viewItem.getIfModule());
      //			if (viewItem.getHideTitle()) {
      //				menu.setShowTitle(false);
      //			}
      //			else {
      //				menu.setShowTitle(true);
      //			}

      List<AbstractWidget> items = new ArrayList<AbstractWidget>();
      //			for (ViewItem menuItem : sortFieldList(viewItem.getMenubarItems())) {
      //				Item item = new Item();
      //				item.setName(menuItem.getName());
      //				item.setAction(menuItem.getOnClick());
      //				item.setTitle(menuItem.getTitle());
      //				item.setHideIf(menuItem.getHideIf());
      //				item.setReadonlyIf(menuItem.getReadonlyIf());
      //				item.setShowIf(menuItem.getShowIf());
      //				item.setModuleToCheck(menuItem.getIfModule());
      //				item.setConditionToCheck(menuItem.getIfConfig());
      //				items.add(item);
      //			}

      menu.setItems(items);
      menubar.add(menu);
    }

    if (!menubar.isEmpty()) {
      view.setMenubar(menubar);
    }
  }
}
