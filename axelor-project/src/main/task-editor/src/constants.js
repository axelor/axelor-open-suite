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
import { sortByKey, sortByDate, sortObject, sortPriority } from './utils';
import { red, pink, purple, blue, teal, green, orange, brown, cyan } from '@material-ui/core/colors';
import { translate } from './utils';

export const DUPLICATE_TASK_OPTIONS = [
  {
    name: 'description',
    label: translate('Task Description'),
  },
  {
    name: 'parentTask',
    label: translate('Parent Task'),
  },
  {
    name: 'attachments',
    label: translate('Attachements'),
  },
  {
    name: 'projectTaskSection',
    label: translate('Section'),
  },
  {
    name: 'taskDate',
    label: translate('TaskEditor.taskDate'),
  },
  {
    name: 'taskEndDate',
    label: translate('Task end'),
  },
  {
    name: 'priority',
    label: translate('Priority'),
  },
  {
    name: 'status',
    label: translate('Status'),
  },
  {
    name: 'progressSelect',
    label: translate('Progress'),
  },
  {
    name: 'projectTaskCategory',
    label: translate('Category'),
  },
];

export const PRIORITY = {
  1: 'lightblue',
  2: '#428bca',
  3: 'orange',
  4: '#d9534f',
};

export const TASK_FILTERS_MENU = [
  { code: 'allTasks', value: translate('All tasks') },
  { code: 'incompleteTasks', value: translate('Incomplete tasks') },
  {
    code: 'completedTasks',
    value: translate('Completed tasks'),
    items: [
      { code: 'allCompletedTasks', value: translate('All completed tasks') },
      { code: 'today', value: translate('Today'), label: translate('All completed tasks') },
      { code: 'yesterday', value: translate('Yesterday'), label: translate('All completed tasks') },
      { code: 'oneweek', value: translate('1 week'), label: translate('All completed tasks') },
      { code: 'twoWeeks', value: translate('2 weeks'), label: translate('All completed tasks') },
      { code: 'threeWeeks', value: translate('3 weeks'), label: translate('All completed tasks') },
    ],
  },
];

export const SORT_COLUMNS = [
  { code: 'sequence', name: translate('TaskEditor.none'), sortFunction: sortByKey },
  { code: 'name', name: translate('Alphabetical'), sortFunction: sortByKey },
  {
    code: 'assignedTo',
    name: translate('Assignee'),
    sortFunction: sortObject,
    fieldName: 'fullName',
  },
  { code: 'taskEndDate', name: translate('Due Date'), sortFunction: sortByDate },
  { code: 'taskDate', name: translate('TaskEditor.taskDate'), sortFunction: sortByDate },
  {
    code: 'priority',
    name: translate('Priority'),
    sortFunction: sortPriority,
    fieldName: 'priority.technicalTypeSelect',
  },
  { code: 'status', name: translate('Status'), sortFunction: sortObject, fieldName: 'name' },
  { code: 'progressSelect', name: translate('Progress'), sortFunction: sortByKey },
  { code: 'projectTaskCategory', name: translate('Category'), sortFunction: sortObject, fieldName: 'name' },
];

export const COLORS = [
  blue[600],
  orange[600],
  green[600],
  teal[600],
  purple[600],
  red[600],
  pink[600],
  cyan[600],
  brown[600],
  blue[700],
  orange[700],
  green[700],
  teal[700],
  purple[700],
  red[700],
  pink[700],
  cyan[700],
  brown[700],
  blue[800],
  orange[800],
  green[800],
  teal[800],
  red[800],
  purple[800],
  pink[800],
  cyan[800],
  brown[800],
  blue[900],
  orange[900],
  green[900],
  teal[900],
  purple[900],
  red[900],
  pink[900],
  cyan[900],
  brown[900],
  blue[500],
  orange[500],
  green[500],
  teal[500],
  purple[500],
  red[500],
  pink[500],
  cyan[500],
  brown[500],
];
export const drawerWidth = '50%';
export const TOOLBAR_HEIGHT = 50;
export const mobileDrawerWidth = '100%';
