/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import moment from 'moment';
import { getFormatedDate, getCompletedStatus } from '../../utils';
import { COLORS } from '../../constants';

const avatarColor = {};
const COLORSCOPY = [...COLORS];

const getColor = () => {
  if (!COLORSCOPY.length) return 'gray';
  const newColor = COLORSCOPY.splice(Math.floor(Math.random() * COLORSCOPY.length), 1)[0];
  return newColor;
};

export const getAvatarColor = id => {
  if (id === undefined) return;
  if (avatarColor[id]) return avatarColor[id];
  let color = getColor();
  if (color) {
    return (avatarColor[id] = color);
  }
};

function menuFilterTasks(tasks, type, { project }) {
  let filteredTasks = [...(tasks || [])];
  let completedStatusId = getCompletedStatus(project).id;
  switch (type) {
    case 'allTasks':
      return filteredTasks;
    case 'incompleteTasks':
      filteredTasks = filteredTasks.filter(task => !task.status || task.status.id !== completedStatusId);
      return filteredTasks;
    case 'completedTasks':
    case 'allCompletedTasks':
      filteredTasks = filteredTasks.filter(task => task.status && task.status.id === completedStatusId);
      return filteredTasks;
    case 'yesterday':
      filteredTasks = filteredTasks.filter(
        task =>
          task.status &&
          task.status.id === completedStatusId &&
          getFormatedDate(task.taskEndDate) === getFormatedDate(moment().subtract(1, 'days')),
      );
      return filteredTasks;
    case 'today':
      filteredTasks = filteredTasks.filter(
        task =>
          task.status &&
          task.status.id === completedStatusId &&
          getFormatedDate(task.taskEndDate) === getFormatedDate(moment()),
      );
      return filteredTasks;
    case 'oneweek':
      filteredTasks = filteredTasks.filter(
        task =>
          task.status && task.status.id === completedStatusId && moment(task.taskEndDate).isSame(new Date(), 'week'),
      );
      return filteredTasks;
    case 'twoWeeks':
      filteredTasks = filteredTasks.filter(
        task =>
          task.status &&
          task.status.id === completedStatusId &&
          moment(task.taskEndDate).isSame(moment().subtract(8, 'days'), 'week'),
      );
      return filteredTasks;
    case 'threeWeeks':
      filteredTasks = filteredTasks.filter(
        task =>
          task.status &&
          task.status.id === completedStatusId &&
          moment(task.taskEndDate).isSame(moment().subtract(15, 'days'), 'week'),
      );
      return filteredTasks;
    default:
      return filteredTasks;
  }
}

export function getFilteredTask(tasks, type, filter, { userId, project }) {
  if (!type) return [];

  const filteredTasks = menuFilterTasks(tasks, type, { project });
  switch (filter) {
    case 'dueNextWeek':
      return filteredTasks.filter(task => moment(task.taskEndDate).isSame(moment().add(8, 'days'), 'week'));
    case 'justMyTasks':
      return filteredTasks.filter(task => (task.assignedTo && task.assignedTo.id) === userId);
    case 'dueThisWeek':
      return filteredTasks.filter(task => moment(task.taskEndDate).isSame(new Date(), 'week'));
    default:
      return filteredTasks;
  }
}
export const getConnectedTaskIds = (id, tasks) => {
  const connectedTaskIds = [id];
  for (let i = 0; i < connectedTaskIds.length; i++) {
    const ID = connectedTaskIds[i];
    tasks.forEach(task => {
      task.parentTask &&
        task.parentTask.id === ID &&
        !connectedTaskIds.includes(task.id) &&
        connectedTaskIds.push(task.id);
    });
  }
  return connectedTaskIds;
};
