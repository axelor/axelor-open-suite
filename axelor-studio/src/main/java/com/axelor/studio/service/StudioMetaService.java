/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudioMetaService {

  private final Logger log = LoggerFactory.getLogger(StudioMetaService.class);

  @Inject private MetaActionRepository metaActionRepo;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private MetaMenuRepository metaMenuRepo;

  @Inject private MenuBuilderRepository menuBuilderRepo;

  @Inject private MetaModelRepository metaModelRepo;

  /**
   * Removes MetaActions from comma separated names in string.
   *
   * @param actionNames Comma separated string of action names.
   */
  @Transactional
  public void removeMetaActions(String xmlIds) {

    log.debug("Removing actions: {}", xmlIds);
    if (xmlIds == null) {
      return;
    }

    List<MetaAction> metaActions =
        metaActionRepo
            .all()
            .filter("self.xmlId in ?1 OR self.name in ?1 ", Arrays.asList(xmlIds.split(",")))
            .fetch();

    for (MetaAction action : metaActions) {
      List<MetaMenu> menus = metaMenuRepo.all().filter("self.action = ?1", action).fetch();
      for (MetaMenu metaMenu : menus) {
        metaMenu.setAction(null);
        metaMenuRepo.save(metaMenu);
      }
      metaActionRepo.remove(action);
    }
  }

  @Transactional
  public MetaAction updateMetaAction(
      String name, String actionType, String xml, String model, String xmlId) {

    MetaAction action = metaActionRepo.findByID(xmlId);

    if (action == null) {
      action = new MetaAction(name);
      action.setXmlId(xmlId);
      Integer priority = getPriority(MetaAction.class.getSimpleName(), name);
      action.setPriority(priority);
    }
    action.setType(actionType);
    action.setModel(model);
    action.setXml(xml);

    return metaActionRepo.save(action);
  }

  /**
   * Creates or Updates metaView from AbstractView.
   *
   * @param viewIterator ViewBuilder iterator
   */
  @Transactional
  public MetaView generateMetaView(AbstractView view) {

    String name = view.getName();
    String xmlId = view.getXmlId();
    String model = view.getModel();
    String viewType = view.getType();

    log.debug("Search view name: {}, xmlId: {}", name, xmlId);

    MetaView metaView;
    if (xmlId != null) {
      metaView =
          metaViewRepo
              .all()
              .filter(
                  "self.name = ?1 and self.xmlId = ?2 and self.type = ?3", name, xmlId, viewType)
              .fetchOne();
    } else {
      metaView =
          metaViewRepo.all().filter("self.name = ?1 and self.type = ?2", name, viewType).fetchOne();
    }

    log.debug("Meta view found: {}", metaView);

    if (metaView == null) {
      metaView =
          metaViewRepo
              .all()
              .filter("self.name = ?1 and self.type = ?2", name, viewType)
              .order("-priority")
              .fetchOne();
      Integer priority = 20;
      if (metaView != null) {
        priority = metaView.getPriority() + 1;
      }
      metaView = new MetaView();
      metaView.setName(name);
      metaView.setXmlId(xmlId);
      metaView.setModel(model);
      metaView.setPriority(priority);
      metaView.setType(viewType);
      metaView.setTitle(view.getTitle());
    }

    String viewXml = XMLViews.toXml(view, true);
    metaView.setXml(viewXml);
    return metaViewRepo.save(metaView);
  }

  public String updateAction(String oldAction, String newAction, boolean remove) {

    if (oldAction == null) {
      return newAction;
    }
    if (newAction == null) {
      return oldAction;
    }

    if (remove) {
      oldAction = oldAction.replace(newAction, "");
    } else if (!oldAction.contains(newAction)) {
      oldAction = oldAction + "," + newAction;
    }

    oldAction = oldAction.replace(",,", ",");
    if (oldAction.isEmpty()) {
      return null;
    }

    return oldAction;
  }

  public MetaMenu createMenu(MenuBuilder builder) {
    //    String xmlId = XML_ID_PREFIX + builder.getName();
    String xmlId = builder.getXmlId();
    MetaMenu menu = builder.getMetaMenu();

    if (menu == null) {
      menu = metaMenuRepo.findByID(xmlId);
    } else {
      menu.setXmlId(xmlId);
    }

    if (menu == null) {
      menu = new MetaMenu(builder.getName());
      menu.setXmlId(xmlId);
      Integer priority = getPriority(MetaMenu.class.getSimpleName(), menu.getName());
      menu.setPriority(priority);
      menu.setTitle(builder.getTitle());
      menu = metaMenuRepo.save(menu);
    }

    menu.setTitle(builder.getTitle());
    menu.setIcon(builder.getIcon());
    menu.setIconBackground(builder.getIconBackground());
    menu.setOrder(builder.getOrder());
    menu.setParent(builder.getParentMenu());

    if (builder.getGroups() != null) {
      Set<Group> groups = new HashSet<>();
      groups.addAll(builder.getGroups());
      menu.setGroups(groups);
    }

    if (builder.getRoles() != null) {
      Set<Role> roles = new HashSet<>();
      roles.addAll(builder.getRoles());
      menu.setRoles(roles);
    }

    String condition = builder.getConditionToCheck();
    if (builder.getAppBuilder() != null) {
      if (condition != null) {
        condition =
            "__config__.app.isApp('"
                + builder.getAppBuilder().getCode()
                + "') && ("
                + condition
                + ")";
      } else {
        condition = "__config__.app.isApp('" + builder.getAppBuilder().getCode() + "')";
      }
    }
    menu.setConditionToCheck(condition);
    menu.setModuleToCheck(builder.getModuleToCheck());
    menu.setLeft(builder.getLeft());
    menu.setTop(builder.getTop());
    menu.setHidden(builder.getHidden());
    menu.setMobile(builder.getMobile());

    menu.setTag(builder.getTag());
    menu.setTagCount(builder.getTagCount());
    menu.setTagGet(builder.getTagGet());
    menu.setTagStyle(builder.getTagStyle());

    menu.setLink(builder.getLink());
    if (builder.getMetaModule() != null) {
      menu.setModule(builder.getMetaModule().getName());
    }

    return menu;
  }

  @Transactional
  public void removeMetaMenu(MetaMenu metaMenu) {
    Preconditions.checkNotNull(metaMenu, "metaMenu cannot be null.");

    List<MetaMenu> subMenus = metaMenuRepo.all().filter("self.parent = ?1", metaMenu).fetch();
    for (MetaMenu subMenu : subMenus) {
      subMenu.setParent(null);
    }
    List<MenuBuilder> subBuilders =
        menuBuilderRepo.all().filter("self.parentMenu = ?1", metaMenu).fetch();
    for (MenuBuilder subBuilder : subBuilders) {
      subBuilder.setParentMenu(null);
      menuBuilderRepo.save(subBuilder);
    }

    metaMenuRepo.remove(metaMenu);
  }

  private Integer getPriority(String object, String name) {
    String query =
        String.format("SELECT MAX(obj.priority) FROM %s obj WHERE obj.name = :name", object);

    try {
      Optional<Integer> priorityOpt =
          Optional.ofNullable(
              JPA.em()
                  .createQuery(query, Integer.class)
                  .setParameter("name", name)
                  .getSingleResult());
      return priorityOpt.orElse(0) + 1;
    } catch (NoResultException e) {
      return 0;
    }
  }

  @Transactional
  public void trackJsonField(MetaJsonModel jsonModel) {

    String messageBody = "";

    List<MetaJsonField> metaJsonFieldList = jsonModel.getFields();

    jsonModel = Beans.get(MetaJsonModelRepository.class).find(jsonModel.getId());

    List<MetaJsonField> jsonFieldList = new ArrayList<MetaJsonField>(jsonModel.getFields());

    if (metaJsonFieldList.equals(jsonFieldList)) {
      return;
    }

    List<MetaJsonField> commonJsonFieldList = new ArrayList<>(jsonFieldList);
    commonJsonFieldList.retainAll(metaJsonFieldList);

    metaJsonFieldList.removeAll(jsonFieldList);
    if (!metaJsonFieldList.isEmpty()) {
      messageBody =
          metaJsonFieldList.stream().map(list -> list.getName()).collect(Collectors.joining(", "));
      trackingFields(jsonModel, messageBody, "Field added");
    }

    jsonFieldList.removeAll(commonJsonFieldList);
    if (!jsonFieldList.isEmpty()) {
      messageBody =
          jsonFieldList.stream().map(list -> list.getName()).collect(Collectors.joining(", "));
      trackingFields(jsonModel, messageBody, "Field removed");
    }
  }

  @Transactional
  public void trackingFields(
      AuditableModel auditableModel, String messageBody, String messageSubject) {

    User user = AuthUtils.getUser();
    MailMessage message = new MailMessage();
    Mapper mapper = Mapper.of(auditableModel.getClass());

    message.setSubject(messageSubject);
    message.setAuthor(user);
    message.setBody(messageBody);
    message.setRelatedId(auditableModel.getId());
    message.setRelatedModel(EntityHelper.getEntityClass(auditableModel).getName());
    message.setType(MailConstants.MESSAGE_TYPE_NOTIFICATION);
    message.setRelatedName(mapper.getNameField().get(auditableModel).toString());

    Beans.get(MailMessageRepository.class).save(message);
  }

  @Transactional
  public void trackJsonField(MetaJsonField metaJsonField) {

    MetaModel metaModel =
        metaModelRepo.all().filter("self.fullName = ?1", metaJsonField.getModel()).fetchOne();

    trackingFields(metaModel, metaJsonField.getName(), "Field added");
  }
}
