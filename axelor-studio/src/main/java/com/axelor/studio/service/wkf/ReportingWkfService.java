package com.axelor.studio.service.wkf;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.ActionBuilderLineRepository;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepo;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.service.builder.ActionBuilderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;

public class ReportingWkfService {

  @Inject private MenuBuilderRepo menuBuilderRepo;
  @Inject private ActionBuilderService actionBuilderService;
  @Inject private WkfRepository wkfRepo;

  private static final String REPORTING_MENU_NAME = "Reporting";
  private static final String WORKFLOW_DASHBOARD_MENU_NAME = "Workflow dashboard";
  private static final String PROCESS_TRACKING_MENU_NAME = "Process tracking";

  /**
   * Sets the "Reporting" menu related to the work-flow. The menu will be located under the parent
   * menu specified in work-flow context. If the menu doesn't exist yet, it is created. Otherwise
   * its name and parent menu are updated. If the parent menu field is empty in the context, then
   * the menu is deleted.
   *
   * @param contextWkf
   */
  @Transactional
  public void setReportingMenu(Wkf contextWkf) {
    final Optional<MenuBuilder> formerReportingMenuBuilderOpt =
        getMenuBuilderByWkfFromRepo(contextWkf);
    final Optional<MetaMenu> contextParentMenuOpt = Optional.ofNullable(contextWkf.getParentMenu());

    formerReportingMenuBuilderOpt.ifPresent(
        formerReportingMenuBuilder -> {
          String formerParentMenuName = formerReportingMenuBuilder.getParentMenu().getName();
          if (!contextParentMenuOpt.isPresent()
              || !contextParentMenuOpt.get().getName().equals(formerParentMenuName)) {
            long wkfWithSameParentMenuCount =
                wkfRepo
                    .all()
                    .filter("self.parentMenu = :parentMenu")
                    .bind("parentMenu", formerReportingMenuBuilder.getParentMenu())
                    .count();
            if (wkfWithSameParentMenuCount > 1) {
              // Update former reporting menu
              Wkf wkf = wkfRepo.find(contextWkf.getId());
              MenuBuilder processTrackingMenuBuilder =
                  menuBuilderRepo.findByName(
                      getMenuNameFromMenuTitle(wkf, PROCESS_TRACKING_MENU_NAME));
              setParentMenuNameContextLine(formerParentMenuName, processTrackingMenuBuilder);

              MenuBuilder workflowDashboardMenuBuilder =
                  menuBuilderRepo.findByName(
                      getMenuNameFromMenuTitle(wkf, WORKFLOW_DASHBOARD_MENU_NAME));
              setParentMenuNameContextLine(formerParentMenuName, workflowDashboardMenuBuilder);
            } else {
              // Remove reporting menu and its sub menus
              menuBuilderRepo
                  .all()
                  .filter("self.parentMenu = :parentMenu")
                  .bind("parentMenu", formerReportingMenuBuilder)
                  .remove();
              List<MenuBuilder> reportingSubMenuBuilders =
                  menuBuilderRepo
                      .findByParentMenu(formerReportingMenuBuilder.getMetaMenu())
                      .fetch();
              for (MenuBuilder reportingSubMenuBuilder : reportingSubMenuBuilders) {
                menuBuilderRepo.remove(reportingSubMenuBuilder);
              }
              menuBuilderRepo.remove(formerReportingMenuBuilder);
            }
          }
        });

    contextParentMenuOpt.ifPresent(
        contextParentMenu -> {
          if (!formerReportingMenuBuilderOpt.isPresent()
              || !formerReportingMenuBuilderOpt
                  .get()
                  .getMetaMenu()
                  .getName()
                  .equals(contextParentMenuOpt.get().getName())) {
            Optional<MenuBuilder> newReportingMenuBuilderOpt = getMenuBuilderByWkf(contextWkf);
            MenuBuilder processTrackingMenuBuilder;
            MenuBuilder workflowDashboardMenuBuilder;
            if (newReportingMenuBuilderOpt.isPresent()) {
              // Find existing reporting menus
              processTrackingMenuBuilder =
                  menuBuilderRepo.findByName(
                      getMenuNameFromMenuTitle(contextWkf, PROCESS_TRACKING_MENU_NAME));
              workflowDashboardMenuBuilder =
                  menuBuilderRepo.findByName(
                      getMenuNameFromMenuTitle(contextWkf, WORKFLOW_DASHBOARD_MENU_NAME));
            } else {
              // Create reporting menu
              MenuBuilder newReportingMenuBuilder = createReportingRootMenu(contextWkf);
              processTrackingMenuBuilder =
                  createProcessTrackingMenu(contextWkf, newReportingMenuBuilder);
              workflowDashboardMenuBuilder =
                  createWorkflowDashboardMenu(contextWkf, newReportingMenuBuilder);
            }
            // Update new reporting menu
            String newParentMenuName = contextParentMenu.getName();
            processTrackingMenuBuilder =
                setParentMenuNameContextLine(newParentMenuName, processTrackingMenuBuilder);
            workflowDashboardMenuBuilder =
                setParentMenuNameContextLine(newParentMenuName, workflowDashboardMenuBuilder);
          }
        });
  }

  private Optional<MenuBuilder> getMenuBuilderByWkfFromRepo(Wkf contextWkf) {
    if (contextWkf.getId() == null) {
      return Optional.empty();
    }
    Wkf wkf = wkfRepo.find(contextWkf.getId());
    return getMenuBuilderByWkf(wkf);
  }

  /**
   * Gets the menu builder related to the parent menu of the specified work-flow. If
   * isFromWkfContext is true, the work-flow is the one in the context, otherwise the related
   * work-flow in database is fetched. Returns null if the work-flow doesn't have a parent menu.
   *
   * @param contextWkf
   * @param isFromWkfContext
   * @return
   */
  private Optional<MenuBuilder> getMenuBuilderByWkf(Wkf wkf) {
    if (wkf.getParentMenu() == null) {
      return Optional.empty();
    }
    String reportingMenuName = getMenuNameFromMenuTitle(wkf, REPORTING_MENU_NAME);
    MetaMenu reportingMenu =
        Beans.get(MetaMenuRepository.class)
            .all()
            .filter("self.name = :name")
            .bind("name", reportingMenuName)
            .fetchOne();
    if (reportingMenu == null) {
      return Optional.empty();
    }
    return Optional.of(
        menuBuilderRepo
            .all()
            .filter("self.metaMenu = :reportingMenu")
            .bind("reportingMenu", reportingMenu)
            .fetchOne());
  }

  private MenuBuilder setParentMenuNameContextLine(String parentMenuName, MenuBuilder menuBuilder) {
    ActionBuilderLine contextLine = new ActionBuilderLine();
    contextLine.setName("parentMenuName");
    contextLine.setValue(parentMenuName);
    contextLine.setActionBuilder(menuBuilder.getActionBuilder());
    contextLine = Beans.get(ActionBuilderLineRepository.class).save(contextLine);

    List<ActionBuilderLine> lines = menuBuilder.getActionBuilder().getLines();
    lines.clear();
    lines.add(contextLine);
    return menuBuilder;
  }

  private MenuBuilder createReportingRootMenu(Wkf wkf) {
    MenuBuilder reportingMenuBuilder = new MenuBuilder();
    reportingMenuBuilder.setName(getMenuNameFromMenuTitle(wkf, REPORTING_MENU_NAME));
    reportingMenuBuilder.setTitle(REPORTING_MENU_NAME);
    reportingMenuBuilder.setAppBuilder(wkf.getAppBuilder());
    reportingMenuBuilder.setParentMenu(wkf.getParentMenu());
    reportingMenuBuilder.setShowAction(false);
    return menuBuilderRepo.save(reportingMenuBuilder);
  }

  private MenuBuilder createProcessTrackingMenu(Wkf wkf, MenuBuilder newReportingMenuBuilder) {
    MenuBuilder processTrackingMenuBuilder =
        initializeReportingSubMenu(wkf, PROCESS_TRACKING_MENU_NAME, newReportingMenuBuilder);
    ActionBuilder processTrackingActionBuilder = new ActionBuilder();
    actionBuilderService.setActionBuilderViews(
        processTrackingActionBuilder,
        "com.axelor.studio.db.WkfTracking",
        "wkf-tracking-form",
        "wkf-tracking-grid");
    processTrackingActionBuilder.setDomainCondition("self.wkf.parentMenu.name = :parentMenuName");
    processTrackingActionBuilder.setTypeSelect(ActionBuilderRepository.TYPE_SELECT_VIEW);
    processTrackingMenuBuilder.setActionBuilder(processTrackingActionBuilder);
    return menuBuilderRepo.save(processTrackingMenuBuilder);
  }

  private MenuBuilder createWorkflowDashboardMenu(Wkf wkf, MenuBuilder newReportingMenuBuilder) {
    MenuBuilder workflowDashboardMenuBuilder =
        initializeReportingSubMenu(wkf, WORKFLOW_DASHBOARD_MENU_NAME, newReportingMenuBuilder);
    ActionBuilder workflowDashboardActionBuilder = new ActionBuilder();
    actionBuilderService.setActionBuilderViews(
        workflowDashboardActionBuilder,
        "com.axelor.studio.db.Wkf",
        "wkf-form-dashboard",
        "wkf-grid");
    workflowDashboardActionBuilder.setDomainCondition("self.wkf.parentMenu.name = :parentMenuName");
    workflowDashboardActionBuilder.setTypeSelect(ActionBuilderRepository.TYPE_SELECT_VIEW);
    workflowDashboardMenuBuilder.setActionBuilder(workflowDashboardActionBuilder);
    return menuBuilderRepo.save(workflowDashboardMenuBuilder);
  }

  private MenuBuilder initializeReportingSubMenu(
      Wkf wkf, String menuTitle, MenuBuilder newReportingMenuBuilder) {
    MenuBuilder menuBuilder = new MenuBuilder();
    menuBuilder.setName(getMenuNameFromMenuTitle(wkf, menuTitle));
    menuBuilder.setTitle(menuTitle);
    menuBuilder.setAppBuilder(wkf.getAppBuilder());
    menuBuilder.setParentMenu(newReportingMenuBuilder.getMetaMenu());
    menuBuilder.setShowAction(true);
    return menuBuilder;
  }

  private String getMenuNameFromMenuTitle(Wkf wkf, String menuTitle) {
    return wkf.getParentMenu().getName() + "-" + menuTitle.toLowerCase().replace(' ', '-');
  }
}
