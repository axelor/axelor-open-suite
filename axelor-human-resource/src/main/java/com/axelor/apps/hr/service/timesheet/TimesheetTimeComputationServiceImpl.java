package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.db.JpaSupport;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;

public class TimesheetTimeComputationServiceImpl extends JpaSupport
    implements TimesheetTimeComputationService {

  protected TimesheetLineService timesheetLineService;
  protected TimesheetLineRepository timesheetLineRepository;
  protected ProjectRepository projectRepo;
  private static final int ENTITY_FIND_TIMEOUT = 10000;
  private static final int ENTITY_FIND_INTERVAL = 50;
  private ExecutorService executor = Executors.newCachedThreadPool();

  @Inject
  public TimesheetTimeComputationServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineRepository timesheetLineRepository,
      ProjectRepository projectRepo) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineRepository = timesheetLineRepository;
    this.projectRepo = projectRepo;
  }

  @Override
  @Transactional
  public void computeTimeSpent(Timesheet timesheet) {
    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();

    if (timesheetLineList != null) {
      Map<Project, BigDecimal> projectTimeSpentMap =
          timesheetLineService.getProjectTimeSpentMap(timesheetLineList);

      Iterator<Project> projectIterator = projectTimeSpentMap.keySet().iterator();

      while (projectIterator.hasNext()) {
        Project project = projectIterator.next();
        getEntityManager().flush();
        executor.submit(
            () -> {
              final Long startTime = System.currentTimeMillis();
              boolean done = false;
              PersistenceException persistenceException = null;

              do {
                try {
                  inTransaction(
                      () -> {
                        final Project updateProject = findProject(project.getId());
                        getEntityManager().lock(updateProject, LockModeType.PESSIMISTIC_WRITE);

                        projectRepo.save(updateProject);
                      });
                  done = true;
                } catch (PersistenceException e) {
                  persistenceException = e;
                  sleep();
                }
              } while (!done && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT);

              if (!done) {
                throw persistenceException;
              }
              return true;
            });
      }
    }
  }

  @Override
  public BigDecimal computeSubTimeSpent(Project project) {
    BigDecimal sum = BigDecimal.ZERO;
    List<Project> subProjectList =
        projectRepo.all().filter("self.parentProject = ?1", project).fetch();
    if (subProjectList == null || subProjectList.isEmpty()) {
      return this.computeTimeSpent(project);
    }
    for (Project projectIt : subProjectList) {
      sum = sum.add(this.computeSubTimeSpent(projectIt));
    }
    return sum;
  }

  @Override
  public BigDecimal computeTimeSpent(Project project) {
    BigDecimal sum = BigDecimal.ZERO;
    List<TimesheetLine> timesheetLineList =
        timesheetLineRepository
            .all()
            .filter(
                "self.project = ?1 AND self.timesheet.statusSelect = ?2",
                project,
                TimesheetRepository.STATUS_VALIDATED)
            .fetch();
    for (TimesheetLine timesheetLine : timesheetLineList) {
      sum = sum.add(timesheetLine.getHoursDuration());
    }
    return sum;
  }

  protected Project findProject(Long projectId) {
    Project project;
    final long startTime = System.currentTimeMillis();
    while ((project = projectRepo.find(projectId)) == null
        && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT) {
      sleep();
    }
    if (project == null) {
      throw new EntityNotFoundException(projectId.toString());
    }
    return project;
  }

  protected void sleep() {
    try {
      Thread.sleep(ENTITY_FIND_INTERVAL);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
