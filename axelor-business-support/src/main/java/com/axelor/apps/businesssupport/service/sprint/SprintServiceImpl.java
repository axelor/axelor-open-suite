package com.axelor.apps.businesssupport.service.sprint;

import com.axelor.apps.businesssupport.db.ProjectVersion;
import com.axelor.apps.businesssupport.db.Sprint;
import com.axelor.apps.businesssupport.db.repo.ProjectVersionRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SprintServiceImpl implements SprintService {

  @Inject
  public SprintServiceImpl() {}

  @Override
  public List<Sprint> getCurrentSprintsRelatedToTheProject(Project project) {
    if (project == null || ObjectUtils.isEmpty(project.getRoadmapSet())) {
      return new ArrayList<>();
    }

    return project.getRoadmapSet().stream()
        .filter(
            version ->
                Objects.equals(ProjectVersionRepository.STATUS_ON_HOLD, version.getStatusSelect()))
        .map(ProjectVersion::getSprintList)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
