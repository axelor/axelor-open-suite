/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ProjectTaskCopyServiceImplTest {

  private ProjectTaskRepository projectTaskRepository;
  private ProjectTaskCopyService projectTaskCopyService;
  private long sequence;

  @BeforeEach
  void prepare() {
    sequence = 0;
    projectTaskRepository = mock(ProjectTaskRepository.class);
    when(projectTaskRepository.copy(any(ProjectTask.class), eq(false)))
        .thenAnswer(
            invocation -> {
              // JPA.copy keeps the many-to-one fields and skips the one-to-many ones
              ProjectTask sourceTask = invocation.getArgument(0);
              ProjectTask copiedTask = new ProjectTask(sourceTask.getName());
              copiedTask.setProject(sourceTask.getProject());
              copiedTask.setParentTask(sourceTask.getParentTask());
              copiedTask.setTicketNumber(sourceTask.getTicketNumber());
              return copiedTask;
            });
    projectTaskCopyService = new ProjectTaskCopyServiceImpl(projectTaskRepository);
  }

  @Test
  void testCopyProjectTaskListWithoutTask() {
    Project targetProject = new Project();

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(new Project(), targetProject);

    Assertions.assertTrue(copiedTaskList.isEmpty());
    Assertions.assertNull(targetProject.getProjectTaskList());
    verifyNoInteractions(projectTaskRepository);
  }

  @Test
  void testCopyProjectTaskListRebuildsHierarchyOnCopiedTasks() {
    Project sourceProject = new Project();
    ProjectTask rootTask = createTask(sourceProject, "Root", null, "SRC1");
    ProjectTask childTask = createTask(sourceProject, "Child", rootTask, "SRC2");
    ProjectTask grandChildTask = createTask(sourceProject, "Grand child", childTask, "SRC3");
    createTask(sourceProject, "Other root", null, "SRC4");
    Project targetProject = new Project();

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(sourceProject, targetProject);

    // every task of the source project is copied on the target project
    Assertions.assertEquals(
        List.of("Root", "Child", "Grand child", "Other root"), names(copiedTaskList));
    Assertions.assertEquals(copiedTaskList, targetProject.getProjectTaskList());
    copiedTaskList.forEach(task -> Assertions.assertSame(targetProject, task.getProject()));

    // the hierarchy links the copies, not the tasks of the source project
    ProjectTask copiedRootTask = copiedTaskList.get(0);
    ProjectTask copiedChildTask = copiedTaskList.get(1);
    ProjectTask copiedGrandChildTask = copiedTaskList.get(2);
    ProjectTask copiedOtherRootTask = copiedTaskList.get(3);
    Assertions.assertNull(copiedRootTask.getParentTask());
    Assertions.assertSame(copiedRootTask, copiedChildTask.getParentTask());
    Assertions.assertSame(copiedChildTask, copiedGrandChildTask.getParentTask());
    Assertions.assertNull(copiedOtherRootTask.getParentTask());
    Assertions.assertEquals(List.of("Child"), names(copiedRootTask.getProjectTaskList()));
    Assertions.assertEquals(List.of("Grand child"), names(copiedChildTask.getProjectTaskList()));
    Assertions.assertNull(copiedOtherRootTask.getProjectTaskList());

    // the source project is left untouched
    Assertions.assertNull(rootTask.getParentTask());
    Assertions.assertSame(rootTask, childTask.getParentTask());
    Assertions.assertSame(childTask, grandChildTask.getParentTask());
    Assertions.assertEquals(4, sourceProject.getProjectTaskList().size());

    // the ticket numbers belong to the source project
    copiedTaskList.forEach(task -> Assertions.assertNull(task.getTicketNumber()));
    Assertions.assertEquals("SRC1", rootTask.getTicketNumber());
  }

  @Test
  void testCopyProjectTaskListDropsParentTaskFromAnotherProject() {
    ProjectTask externalParentTask = createTask(new Project(), "External parent", null, null);
    Project sourceProject = new Project();
    ProjectTask sourceTask = createTask(sourceProject, "Task", externalParentTask, null);
    Project targetProject = new Project();

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(sourceProject, targetProject);

    Assertions.assertEquals(List.of("Task"), names(copiedTaskList));
    Assertions.assertNull(copiedTaskList.get(0).getParentTask());
    // the copy is not linked to the task list of the parent of another project
    Assertions.assertEquals(List.of(sourceTask), externalParentTask.getProjectTaskList());
  }

  @Test
  void testSaveProjectTaskListSavesEveryTaskInOrder() {
    ProjectTask rootTask = new ProjectTask("Root");
    ProjectTask childTask = new ProjectTask("Child");

    projectTaskCopyService.saveProjectTaskList(List.of(rootTask, childTask));

    InOrder inOrder = inOrder(projectTaskRepository);
    inOrder.verify(projectTaskRepository).save(rootTask);
    inOrder.verify(projectTaskRepository).save(childTask);
    inOrder.verifyNoMoreInteractions();
  }

  protected ProjectTask createTask(
      Project project, String name, ProjectTask parentTask, String ticketNumber) {
    ProjectTask task = new ProjectTask(name);
    task.setId(++sequence);
    task.setTicketNumber(ticketNumber);
    project.addProjectTaskListItem(task);
    if (parentTask != null) {
      parentTask.addProjectTaskListItem(task);
    }
    return task;
  }

  protected List<String> names(List<ProjectTask> projectTaskList) {
    return projectTaskList.stream().map(ProjectTask::getName).toList();
  }
}
