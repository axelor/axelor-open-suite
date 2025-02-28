package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.hr.service.project.ProjectIndicatorsService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectIndicatorsDashboardController {
  public void getTotalAllocatedTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);
    BigDecimal allocatedTime =
        allocationLineComputeService.getAllocatedTime((Project) map.get("project"),(LocalDate) map.get("fromDate"), (LocalDate) map.get("toDate"),  (Employee) map.get("employee"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", allocatedTime);

    response.setData(List.of(dataResponse));
  }

  public void getTotalPlannedTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);
    BigDecimal plannedTime =
        allocationLineComputeService.computePlannedTime((LocalDate) map.get("fromDate"), (LocalDate) map.get("toDate"),  (Employee) map.get("employee") ,(Project) map.get("project"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", plannedTime);

    response.setData(List.of(dataResponse));
  }

  public void getTotalLeaveDays(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    ProjectIndicatorsService projectIndicatorsService = Beans.get(ProjectIndicatorsService.class);
    BigDecimal leaveDays =
        projectIndicatorsService.getProjectOrEmployeeLeaveDays((Project) map.get("project"),   (Employee) map.get("employee") ,(LocalDate) map.get("fromDate"),
                (LocalDate) map.get("toDate"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", leaveDays);

    response.setData(List.of(dataResponse));
  }

  public void getTotalAvailableDays(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    ProjectIndicatorsService projectIndicatorsService = Beans.get(ProjectIndicatorsService.class);
    BigDecimal availableDays =
        projectIndicatorsService.getAvailableDays((Project) map.get("project"),   (Employee) map.get("employee") ,(LocalDate) map.get("fromDate"),
                (LocalDate) map.get("toDate"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", availableDays);

    response.setData(List.of(dataResponse));
  }

  public void getTotalEstimatedTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    ProjectIndicatorsService projectIndicatorsService = Beans.get(ProjectIndicatorsService.class);
    BigDecimal estimatedTime =
        projectIndicatorsService.getEstimatedTime( (Project) map.get("project"),   (Employee) map.get("employee") ,(LocalDate) map.get("fromDate"),
                (LocalDate) map.get("toDate"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", estimatedTime);

    response.setData(List.of(dataResponse));
  }

  public void getTotalSpentTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> map = getRequestMap(request);
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);

    BigDecimal spentTime =
        allocationLineComputeService.computeSpentTime(
            (LocalDate) map.get("fromDate"),
            (LocalDate) map.get("toDate"),
            (Employee) map.get("employee"),
            (Project) map.get("project"));
    Map<String, Object> dataResponse = new HashMap<>();
    dataResponse.put("total", spentTime);

    response.setData(List.of(dataResponse));
  }

  protected Map<String, Object> getRequestMap(ActionRequest request) {
    Map<String, Object> data = request.getData();
    Project project = null;
    if (data.get("project") != null) {
      Long projectId = Long.valueOf(((Map) data.get("project")).get("id").toString());
      project = Beans.get(ProjectRepository.class).find(projectId);
    }
    Employee employee = null;
    if (data.get("employee") != null) {
      Long employeeId = Long.valueOf(((Map) data.get("employee")).get("id").toString());
      employee = Beans.get(EmployeeRepository.class).find(employeeId);
    }
    LocalDate fromDate = LocalDate.parse((CharSequence) request.getData().get("fromDate"));
    LocalDate toDate = LocalDate.parse((CharSequence) request.getData().get("toDate"));
    Map<String, Object> map = new HashMap<>();
    map.put("project", project);
    map.put("employee", employee);
    map.put("fromDate", fromDate);
    map.put("toDate", toDate);
    return map;
  }
}
