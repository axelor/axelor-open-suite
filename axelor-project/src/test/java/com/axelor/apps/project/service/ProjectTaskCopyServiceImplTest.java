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
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
              // JPA.copy keeps the many-to-one fields and the many-to-many sets, and skips the
              // one-to-many ones
              ProjectTask sourceTask = invocation.getArgument(0);
              ProjectTask copiedTask = new ProjectTask(sourceTask.getName());
              copiedTask.setProject(sourceTask.getProject());
              copiedTask.setParentTask(sourceTask.getParentTask());
              copiedTask.setNextProjectTask(sourceTask.getNextProjectTask());
              copiedTask.setTicketNumber(sourceTask.getTicketNumber());
              copiedTask.setIsFirst(sourceTask.getIsFirst());
              copiedTask.setActiveSprint(sourceTask.getActiveSprint());
              copiedTask.setOldActiveSprint(sourceTask.getActiveSprint());
              copiedTask.setFinishToStartSet(copySet(sourceTask.getFinishToStartSet()));
              copiedTask.setStartToStartSet(copySet(sourceTask.getStartToStartSet()));
              copiedTask.setFinishToFinishSet(copySet(sourceTask.getFinishToFinishSet()));
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
  void testCopyProjectTaskListRemapsRecurrenceChainOnCopiedTasks() {
    Project sourceProject = new Project();
    ProjectTask firstTask = createTask(sourceProject, "First", null, null);
    ProjectTask secondTask = createTask(sourceProject, "Second", null, null);
    firstTask.setIsFirst(true);
    firstTask.setNextProjectTask(secondTask);
    secondTask.setNextProjectTask(createTask(new Project(), "Task of another project", null, null));

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(sourceProject, new Project());

    ProjectTask copiedFirstTask = copiedTaskList.get(0);
    ProjectTask copiedSecondTask = copiedTaskList.get(1);
    Assertions.assertSame(copiedSecondTask, copiedFirstTask.getNextProjectTask());
    // a chain leaving the project is dropped instead of pointing back to another project
    Assertions.assertNull(copiedSecondTask.getNextProjectTask());
    // the chain is copied as is, the copy must not generate a new one
    Assertions.assertFalse(copiedFirstTask.getIsFirst());
    Assertions.assertTrue(firstTask.getIsFirst());
  }

  @Test
  void testCopyProjectTaskListRemapsDependenciesAndResetsSprint() {
    Project sourceProject = new Project();
    ProjectTask predecessorTask = createTask(sourceProject, "Predecessor", null, null);
    ProjectTask task = createTask(sourceProject, "Task", null, null);
    ProjectTask externalTask = createTask(new Project(), "External", null, null);
    task.setFinishToStartSet(new HashSet<>(Set.of(predecessorTask, externalTask)));
    task.setStartToStartSet(new HashSet<>(Set.of(predecessorTask)));
    task.setActiveSprint(new Sprint());

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(sourceProject, new Project());

    ProjectTask copiedPredecessorTask = copiedTaskList.get(0);
    ProjectTask copiedTask = copiedTaskList.get(1);
    // a dependency inside the project follows the copies, one on another project is a real
    // dependency and is kept
    Assertions.assertEquals(
        Set.of(copiedPredecessorTask, externalTask), copiedTask.getFinishToStartSet());
    Assertions.assertEquals(Set.of(copiedPredecessorTask), copiedTask.getStartToStartSet());
    Assertions.assertTrue(task.getFinishToStartSet().contains(predecessorTask));
    // the sprints belong to the source project and are not copied with it
    Assertions.assertNull(copiedTask.getActiveSprint());
    Assertions.assertNull(copiedTask.getOldActiveSprint());
    Assertions.assertNotNull(task.getActiveSprint());
  }

  @Test
  void testCopyProjectTaskListNeverSharesDependencySetsWithSourceTasks() {
    Project sourceProject = new Project();
    ProjectTask task = createTask(sourceProject, "Task", null, null);
    ProjectTask externalTask = createTask(new Project(), "External", null, null);
    // Hibernate gives an empty collection, not null, to a task without dependency
    task.setFinishToStartSet(new HashSet<>(Set.of(externalTask)));
    task.setStartToStartSet(new HashSet<>());
    task.setFinishToFinishSet(new HashSet<>());

    List<ProjectTask> copiedTaskList =
        projectTaskCopyService.copyProjectTaskList(sourceProject, new Project());

    // a collection shared by two entities makes Hibernate fail on flush
    ProjectTask copiedTask = copiedTaskList.get(0);
    Assertions.assertNotSame(task.getFinishToStartSet(), copiedTask.getFinishToStartSet());
    Assertions.assertNotSame(task.getStartToStartSet(), copiedTask.getStartToStartSet());
    Assertions.assertNotSame(task.getFinishToFinishSet(), copiedTask.getFinishToFinishSet());
    Assertions.assertTrue(copiedTask.getStartToStartSet().isEmpty());
    Assertions.assertTrue(copiedTask.getFinishToFinishSet().isEmpty());
  }

  @Test
  void testSaveCopiedProjectTaskListSavesEveryTaskInOrder() {
    ProjectTask rootTask = new ProjectTask("Root");
    ProjectTask childTask = new ProjectTask("Child");

    projectTaskCopyService.saveCopiedProjectTaskList(List.of(rootTask, childTask));

    InOrder inOrder = inOrder(projectTaskRepository);
    inOrder.verify(projectTaskRepository).save(rootTask);
    inOrder.verify(projectTaskRepository).save(childTask);
    inOrder.verifyNoMoreInteractions();
  }

  private ProjectTask createTask(
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

  private Set<ProjectTask> copySet(Set<ProjectTask> taskSet) {
    return taskSet != null ? new HashSet<>(taskSet) : null;
  }

  private List<String> names(List<ProjectTask> projectTaskList) {
    return projectTaskList.stream().map(ProjectTask::getName).toList();
  }
}
