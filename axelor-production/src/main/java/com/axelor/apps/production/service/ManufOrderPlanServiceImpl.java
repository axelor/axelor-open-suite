package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.AppProductionRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.Job;
import org.optaplanner.examples.projectjobscheduling.domain.JobType;
import org.optaplanner.examples.projectjobscheduling.domain.Project;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.GlobalResource;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class ManufOrderPlanServiceImpl implements ManufOrderPlanService {

  private Schedule unsolvedJobScheduling;
  private Map<String, Resource> machineCodeToResourceMap;
  private Map<Long, OperationOrder> allocationIdToOperationOrderMap;
  private Map<Long, ManufOrder> projectIdToManufOrderMap;
  private Integer granularity;

  private static final String QUICK_SOLVER_CONFIG =
      "org/optaplanner/examples/projectjobscheduling/solver/projectJobSchedulingQuickSolverConfig.xml";
  private static final String SLOW_SOLVER_CONFIG =
      "org/optaplanner/examples/projectjobscheduling/solver/projectJobSchedulingSlowSolverConfig.xml";

  private void initializePlanner() throws AxelorException {
    // Custom Unsolved Job Scheduling
    this.unsolvedJobScheduling = new Schedule();

    // Create Resources
    this.createResources();

    this.allocationIdToOperationOrderMap = new HashMap<>();

    this.projectIdToManufOrderMap = new HashMap<>();

    // Get optaplanner granularity
    switch (Beans.get(AppProductionRepository.class).all().fetchOne().getSchedulingGranularity()) {
      case (1):
        this.granularity = 60;
        break;
      case (2):
        this.granularity = 3600;
        break;
      case (3):
        this.granularity = 86400;
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(ManufOrder manufOrderToPlan) throws AxelorException {
    this.optaPlan(Lists.newArrayList(manufOrderToPlan));
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(List<ManufOrder> manufOrderListToPlan) throws AxelorException {
    optaPlan(manufOrderListToPlan, true);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(List<ManufOrder> manufOrderListToPlan, boolean quickSolve)
      throws AxelorException {
    this.initializePlanner();

    // Build the Solver
    SolverFactory<Schedule> solverFactory;
    if (quickSolve) solverFactory = SolverFactory.createFromXmlResource(QUICK_SOLVER_CONFIG);
    else solverFactory = SolverFactory.createFromXmlResource(SLOW_SOLVER_CONFIG);
    Solver<Schedule> solver = solverFactory.buildSolver();

    // Round now LocalDateTime
    LocalDateTime now = Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime();
    now = this.roundDateTime(now);

    List<ManufOrder> pinnedManufOrderList =
        Beans.get(ManufOrderRepository.class)
            .all()
            .filter(
                "self.plannedEndDateT > :now AND self.statusSelect != 2 AND self.id NOT IN :manufOrderIds")
            .bind("now", now)
            .bind(
                "manufOrderIds",
                manufOrderListToPlan.stream().map(it -> it.getId()).collect(Collectors.toList()))
            .fetch();

    LocalDateTime startDate = now;

    System.out.println("initial startDate : " + startDate);

    for (ManufOrder manufOrder : pinnedManufOrderList)
      if (manufOrder.getPlannedStartDateT().isBefore(startDate))
        startDate = manufOrder.getPlannedStartDateT();

    startDate = this.roundDateTime(startDate);

    System.out.println("    new startDate : " + startDate);

    for (ManufOrder manufOrder : manufOrderListToPlan) {
      LocalDateTime releaseDate;
      if (manufOrder.getPlannedStartDateT() != null
          && manufOrder.getPlannedStartDateT().isAfter(now)) {
        releaseDate = manufOrder.getPlannedStartDateT();
        releaseDate = this.roundDateTime(releaseDate);
      } else {
        releaseDate = now;
      }

      // Create project
      Project project =
          this.createProject(
              manufOrder, startDate, granularity, this.durationBetween(startDate, releaseDate));
      this.projectIdToManufOrderMap.put(project.getId(), manufOrder);
    }

    System.out.println("pinnedManufOrderList size : " + pinnedManufOrderList.size());
    System.out.println("pinnedManufOrderList :");

    for (ManufOrder manufOrder : pinnedManufOrderList) {
      System.out.println(
          "manufOrder " + manufOrder.getId() + " " + manufOrder.getPlannedEndDateT());
      // Create project
      Project project = this.createProject(manufOrder, startDate, granularity, true);
      this.projectIdToManufOrderMap.put(project.getId(), manufOrder);
    }

    // Solve the problem
    Schedule solvedJobScheduling = solver.solve(this.unsolvedJobScheduling);

    for (Allocation allocation : solvedJobScheduling.getAllocationList()) {
      OperationOrder operationOrder = this.allocationIdToOperationOrderMap.get(allocation.getId());

      if (operationOrder != null) {
        this.planOperationOrder(operationOrder, allocation, granularity, startDate);
      }
    }

    System.out.println("PINNED");
    displayManufOrder(pinnedManufOrderList, solvedJobScheduling);
    System.out.println("NOT PINNED");
    displayManufOrder(manufOrderListToPlan, solvedJobScheduling);
    
    for (Allocation allocation : solvedJobScheduling.getAllocationList()) {
      System.out.println(allocation.getId() + " : " + allocation.getExecutionMode() + " " + allocation.getDelay());
    }
  }

  private LocalDateTime roundDateTime(LocalDateTime dateTime) throws AxelorException {
    switch (Beans.get(AppProductionRepository.class).all().fetchOne().getSchedulingGranularity()) {
      case (1):
        return this.ceilDateTime(dateTime, ChronoUnit.MINUTES);
      case (2):
        return this.ceilDateTime(dateTime, ChronoUnit.HOURS);
      case (3):
        return this.ceilDateTime(dateTime, ChronoUnit.DAYS);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
    }
  }

  private void displayManufOrder(List<ManufOrder> manufOrderList, Schedule solvedJobScheduling) {
    for (ManufOrder manufOrder : manufOrderList) {
      System.out.println(manufOrder.getManufOrderSeq());
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        Allocation allocation = null;
        for (Allocation curAllocation : solvedJobScheduling.getAllocationList()) {
          OperationOrder curOperationOrder =
              this.allocationIdToOperationOrderMap.get(curAllocation.getId());
          if (curOperationOrder != null && curOperationOrder.getId() == operationOrder.getId()) {
            allocation = curAllocation;
          }
        }
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < allocation.getStartDate(); i++) {
          spaces.append(" ");
        }
        StringBuilder dashes = new StringBuilder();
        for (int i = allocation.getStartDate(); i < allocation.getEndDate(); i++) {
          dashes.append("#");
        }
        System.out.println(spaces.toString() + dashes.toString());
      }
    }
  }

  private LocalDateTime ceilDateTime(LocalDateTime dateTime, ChronoUnit chronoUnit) {
    LocalDateTime tmpDateTime = dateTime.truncatedTo(chronoUnit);
    if (tmpDateTime.equals(dateTime)) return tmpDateTime;
    return tmpDateTime.plus(chronoUnit.getDuration());
  }

  private void createResources() {
    this.machineCodeToResourceMap = new HashMap<>();

    List<WorkCenter> workCenterList = Beans.get(WorkCenterRepository.class).all().fetch();
    for (WorkCenter workCenter : workCenterList) {
      Resource resource = new GlobalResource(1);

      this.machineCodeToResourceMap.put(workCenter.getCode(), resource);

      this.unsolvedJobScheduling.addResource(resource);
    }
  }

  private void planOperationOrder(
      OperationOrder operationOrder,
      Allocation allocation,
      Integer granularity,
      LocalDateTime startDate)
      throws AxelorException {
    if (CollectionUtils.isEmpty(operationOrder.getToConsumeProdProductList())) {
      Beans.get(OperationOrderService.class).createToConsumeProdProductList(operationOrder);
    }

    LocalDateTime operationOrderPlannedStartDate =
        startDate.plusSeconds(allocation.getStartDate() * granularity);
    operationOrder.setPlannedStartDateT(operationOrderPlannedStartDate);

    LocalDateTime operationOrderPlannedEndDate =
        startDate.plusSeconds(allocation.getEndDate() * granularity);
    operationOrder.setPlannedEndDateT(operationOrderPlannedEndDate);

    operationOrder.setPlannedDuration(
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));

    ManufOrder manufOrder = this.projectIdToManufOrderMap.get(allocation.getProject().getId());
    if (manufOrder == null || manufOrder.getIsConsProOnOperation()) {
      Beans.get(OperationOrderStockMoveService.class).createToConsumeStockMove(operationOrder);
    }

    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);
  }

  private int getCriticalPathDuration(Project project) {
    Job sourceJob = null;
    for (Job job : project.getJobList()) {
      if (job.getJobType() == JobType.SOURCE) {
        sourceJob = job;
        break;
      }
    }
    if (sourceJob != null) {
      return this.getCriticalPathDuration(sourceJob);
    }
    return 0;
  }

  private int getCriticalPathDuration(Job job) {
    if (job.getJobType() == JobType.SINK) {
      return 0;
    } else {
      int maximumCriticalPathDuration = 0;
      for (Job successorJob : job.getSuccessorJobList()) {
        int criticalPathDuration = this.getCriticalPathDuration(successorJob);
        if (criticalPathDuration > maximumCriticalPathDuration) {
          maximumCriticalPathDuration = criticalPathDuration;
        }
      }
      return maximumCriticalPathDuration + this.maximumExecutionModeDuration(job);
    }
  }

  private int maximumExecutionModeDuration(Job job) {
    int maximumExecutionModeDuration = 0;
    for (ExecutionMode executionMode : job.getExecutionModeList()) {
      if (maximumExecutionModeDuration < executionMode.getDuration()) {
        maximumExecutionModeDuration = executionMode.getDuration();
      }
    }
    return maximumExecutionModeDuration;
  }

  private Project createProject(
      ManufOrder manufOrder,
      LocalDateTime startDate,
      Integer granularity,
      Integer projectReleaseDate) {
    return createProject(manufOrder, startDate, granularity, false, projectReleaseDate);
  }

  private Project createProject(
      ManufOrder manufOrder, LocalDateTime startDate, Integer granularity, boolean pinnedProject) {
    return createProject(manufOrder, startDate, granularity, pinnedProject, null);
  }

  private Project createProject(
      ManufOrder manufOrder,
      LocalDateTime startDate,
      Integer granularity,
      boolean pinnedProject,
      Integer projectReleaseDate) {
    Map<Integer, List<OperationOrder>> priorityToOperationOrderListMap =
        this.getPriorityToOperationOrderListMap(manufOrder);

    List<Integer> sortedPriorityList =
        new ArrayList<Integer>(new TreeSet<Integer>(priorityToOperationOrderListMap.keySet()));

    Map<Integer, ArrayList<Job>> priorityToJobListMap =
        this.initializePriorityToJobListMap(sortedPriorityList);
    Map<Integer, ArrayList<Allocation>> priorityToAllocationListMap =
        this.initializePriorityToAllocationListMap(sortedPriorityList);

    Project project;
    if (projectReleaseDate != null) project = new Project(projectReleaseDate);
    else project = new Project();
    this.unsolvedJobScheduling.addProject(project);

    // Source Job
    List<Job> sourceSuccessorJobList = priorityToJobListMap.get(sortedPriorityList.get(1));
    Job sourceJob = this.getSourceJob(project, sourceSuccessorJobList);
    project.addJob(sourceJob);
    priorityToJobListMap.get(Integer.MIN_VALUE).add(sourceJob);
    this.unsolvedJobScheduling.addJob(sourceJob);
    
    // Source execution mode
    ExecutionMode sourceExecutionMode = new ExecutionMode(sourceJob, 0);
    this.unsolvedJobScheduling.addExecutionMode(sourceExecutionMode);
    sourceJob.addExecutionMode(sourceExecutionMode);

    // Source Allocation
    Long sourceAllocationId = (long) (project.getId() * 100);
    List<Allocation> sourceSucccessorAllocationList =
        priorityToAllocationListMap.get(sortedPriorityList.get(1));
    Allocation sourceAllocation =
        this.getSourceAllocation(
            sourceAllocationId, sourceJob, sourceSucccessorAllocationList, projectReleaseDate);
    priorityToAllocationListMap.get(Integer.MIN_VALUE).add(sourceAllocation);
    this.unsolvedJobScheduling.addAllocation(sourceAllocation);
    sourceAllocation.setExecutionMode(sourceExecutionMode);
    sourceAllocation.setDelay(0);

    // Sink Job
    Job sinkJob = this.getSinkJob(project);
    project.addJob(sinkJob);
    priorityToJobListMap.get(Integer.MAX_VALUE).add(sinkJob);
    this.unsolvedJobScheduling.addJob(sinkJob);
    
    // Sink execution mode
    ExecutionMode sinkExecutionMode = new ExecutionMode(sinkJob, 0);
    this.unsolvedJobScheduling.addExecutionMode(sinkExecutionMode);
    sinkJob.addExecutionMode(sinkExecutionMode);

    // Sink Allocation
    Long sinkAllocationId =
        (long) (project.getId() * 100 + manufOrder.getOperationOrderList().size() + 1);
    List<Allocation> sinkPredecessorAllocationList =
        priorityToAllocationListMap.get(sortedPriorityList.get(sortedPriorityList.size() - 2));
    Allocation sinkAllocation =
        this.getSinkAllocation(
            sinkAllocationId, sinkJob, sinkPredecessorAllocationList, projectReleaseDate);
    priorityToAllocationListMap.get(Integer.MAX_VALUE).add(sinkAllocation);
    this.unsolvedJobScheduling.addAllocation(sinkAllocation);
    sinkAllocation.setExecutionMode(sinkExecutionMode);
    sinkAllocation.setDelay(0);

    Integer allocationIdx = 0;
    for (Integer priorityIdx = 0; priorityIdx < sortedPriorityList.size(); priorityIdx++) {
      Integer priority = sortedPriorityList.get(priorityIdx);
      for (OperationOrder operationOrder : priorityToOperationOrderListMap.get(priority)) {
        // Job
        Job job =
            this.getStandardJob(
                project, priorityToJobListMap.get(sortedPriorityList.get(priorityIdx + 1)));
        project.addJob(job);
        this.unsolvedJobScheduling.addJob(job);
        priorityToJobListMap.get(priority).add(job);

        // Execution Mode
        Integer executionModeDuration =
            this.getExecutionModeDuration(operationOrder, manufOrder, granularity);
        ExecutionMode executionMode = new ExecutionMode(job, executionModeDuration);
        this.unsolvedJobScheduling.addExecutionMode(executionMode);
        job.addExecutionMode(executionMode);

        // Resource Requirement
        Resource resource =
            this.machineCodeToResourceMap.get(operationOrder.getWorkCenter().getCode());
        ResourceRequirement resourceRequirement =
            new ResourceRequirement(executionMode, resource, 1);
        this.unsolvedJobScheduling.addResourceRequirement(resourceRequirement);
        executionMode.addResourceRequirement(resourceRequirement);

        // Allocation
        Long allocationId = (long) (project.getId() * 100 + (allocationIdx + 1));
        List<Allocation> predecessorAllocationList =
            priorityToAllocationListMap.get(sortedPriorityList.get(priorityIdx - 1));
        List<Allocation> successorAllocationList =
            priorityToAllocationListMap.get(sortedPriorityList.get(priorityIdx + 1));
        Allocation allocation =
            this.getAllocation(
                allocationId,
                job,
                predecessorAllocationList,
                successorAllocationList,
                sourceAllocation,
                sinkAllocation,
                projectReleaseDate);
        this.allocationIdToOperationOrderMap.put(allocation.getId(), operationOrder);
        allocationIdx++;
        this.unsolvedJobScheduling.addAllocation(allocation);
        priorityToAllocationListMap.get(priority).add(allocation);

        // Pinned job
        boolean isOperationOrderStarted =
            operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
                || operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY
                || operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED;
        if ((operationOrder.getIsPinned() || isOperationOrderStarted || pinnedProject)
            && operationOrder.getPlannedStartDateT() != null) {
          Integer pinnedDate =
              this.durationBetween(startDate, operationOrder.getPlannedStartDateT());
          job.setPinned(true);
          job.setPinnedDate(pinnedDate);
          job.setPinnedExecutionMode(executionMode);
          allocation.setDelay(pinnedDate);
          allocation.setExecutionMode(executionMode);
          // System.out.println("pinned operation order " + operationOrder.getId() + " pinned date "
          // + job.getPinnedDate());
        }
      }
    }

    project.setCriticalPathDuration(getCriticalPathDuration(project));

    return project;
  }

  private Integer durationBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return (int) ChronoUnit.SECONDS.between(startDate, endDate) / this.granularity;
  }

  private Map<Integer, List<OperationOrder>> getPriorityToOperationOrderListMap(
      ManufOrder manufOrder) {
    Map<Integer, List<OperationOrder>> priorityToOperationOrderListMap = new HashMap<>();
    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      int priority = operationOrder.getPriority();
      if (!priorityToOperationOrderListMap.containsKey(priority)) {
        priorityToOperationOrderListMap.put(priority, new ArrayList<OperationOrder>());
      }
      priorityToOperationOrderListMap.get(priority).add(operationOrder);
    }
    priorityToOperationOrderListMap.put(Integer.MIN_VALUE, new ArrayList<OperationOrder>());
    priorityToOperationOrderListMap.put(Integer.MAX_VALUE, new ArrayList<OperationOrder>());

    return priorityToOperationOrderListMap;
  }

  private Map<Integer, ArrayList<Job>> initializePriorityToJobListMap(
      List<Integer> sortedPriorityList) {
    Map<Integer, ArrayList<Job>> priorityToJobListMap = new HashMap<>();

    for (Integer priority : sortedPriorityList) {
      priorityToJobListMap.put(priority, new ArrayList<Job>());
    }

    return priorityToJobListMap;
  }

  private Map<Integer, ArrayList<Allocation>> initializePriorityToAllocationListMap(
      List<Integer> sortedPriorityList) {
    Map<Integer, ArrayList<Allocation>> priorityToAllocationListMap = new HashMap<>();

    for (Integer priority : sortedPriorityList) {
      priorityToAllocationListMap.put(priority, new ArrayList<Allocation>());
    }

    return priorityToAllocationListMap;
  }

  private Job getSourceJob(Project project, List<Job> successorJobList) {
    return this.getJob(project, successorJobList, JobType.SOURCE);
  }

  private Job getSinkJob(Project project) {
    return this.getJob(project, null, JobType.SINK);
  }

  private Job getStandardJob(Project project, List<Job> successorJobList) {
    return this.getJob(project, successorJobList, JobType.STANDARD);
  }

  private Job getJob(Project project, List<Job> successorJobList, JobType jobType) {
    return new Job(project, successorJobList, jobType);
  }

  private Integer getExecutionModeDuration(
      OperationOrder operationOrder, ManufOrder manufOrder, Integer granularity) {
    long duration = 0;

    if (operationOrder.getWorkCenter().getWorkCenterTypeSelect() != 1) {
      duration =
          (long)
              (operationOrder.getWorkCenter().getDurationPerCycle()
                  * Math.ceil(
                      (float) manufOrder.getQty().intValue()
                          / operationOrder.getWorkCenter().getMaxCapacityPerCycle().intValue()));
    } else if (operationOrder.getWorkCenter().getWorkCenterTypeSelect() == 1) {
      duration =
          operationOrder.getWorkCenter().getProdHumanResourceList().get(0).getDuration()
              * manufOrder.getQty().intValue();
    }
    return (int) Math.ceil(((double) duration) / granularity);
  }

  private Allocation getSourceAllocation(
      Long allocationId,
      Job sourceJob,
      List<Allocation> succcessorAllocationList,
      Integer projectReleaseDate) {
    return this.getAllocation(
        allocationId, sourceJob, null, succcessorAllocationList, null, null, projectReleaseDate);
  }

  private Allocation getSinkAllocation(
      Long allocationId,
      Job sinkJob,
      List<Allocation> predecessorAllocationList,
      Integer projectReleaseDate) {
    return this.getAllocation(
        allocationId,
        sinkJob,
        predecessorAllocationList,
        new ArrayList<Allocation>(),
        null,
        null,
        projectReleaseDate);
  }

  private Allocation getAllocation(
      Long allocationId,
      Job job,
      List<Allocation> predecessorAllocationList,
      List<Allocation> successorAllocationList,
      Allocation sourceAllocation,
      Allocation sinkAllocation,
      Integer projectReleaseDate) {
    Allocation allocation =
        new Allocation(
            job,
            predecessorAllocationList,
            successorAllocationList,
            sourceAllocation,
            sinkAllocation,
            projectReleaseDate != null ? projectReleaseDate : 0);

    allocation.setId(allocationId);

    return allocation;
  }
}
