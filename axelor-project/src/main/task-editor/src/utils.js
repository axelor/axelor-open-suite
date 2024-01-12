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

import { SORT_COLUMNS } from './constants';

let lastCookieString;
let lastCookies = {};

export function readCookie(name) {
  let cookieString = document.cookie || '';
  if (cookieString !== lastCookieString) {
    lastCookieString = cookieString;
    lastCookies = cookieString.split('; ').reduce((obj, value) => {
      let parts = value.split('=');
      obj[parts[0]] = parts[1];
      return obj;
    }, {});
  }
  return lastCookies[name];
}

export function sortByAlphabetic(array, key) {
  return array.sort(function (a, b) {
    let x = a[key];
    let y = b[key];
    return x < y ? -1 : x > y ? 1 : 0;
  });
}

export function sortByKey(array, key) {
  const arr = sortByAlphabetic(array, 'name');
  return arr.sort(function (a, b) {
    let x = a[key];
    let y = b[key];
    return x < y ? -1 : x > y ? 1 : 0;
  });
}

export function sortByDate(array, key) {
  return array.sort(function (a, b) {
    let x = new Date(a[key]);
    let y = new Date(b[key]);
    return x < y ? -1 : x > y ? 1 : 0;
  });
}

export function sortObject(array, key, fieldName) {
  return array.sort(function (a, b) {
    let x = a[key] && a[key][fieldName];
    let y = b[key] && b[key][fieldName];
    if (x === '' || !y) return -1;
    if (y === '' || !x) return 1;
    return x < y ? -1 : x > y ? 1 : 0;
  });
}

export function sortPriority(array, key, fieldName) {
  return array.sort(function (a, b) {
    let x = a[fieldName];
    let y = b[fieldName];
    return x > y ? -1 : x < y ? 1 : 0;
  });
}

export function filesToItems(files, maxFiles) {
  const CHUNK_SIZE = 512 * 1024;
  return Array.prototype.slice
    .call(files)
    .slice(0, maxFiles)
    .map((f, i) => ({
      file: f,
      index: i,
      progress: 0,
      cancelled: false,
      completed: false,
      chunkProgress: new Array(Math.floor(f.size / CHUNK_SIZE) + 1).fill(0),
      error: false,
      totalUploaded: 0,
    }));
}

export function getTasks(tasks, section) {
  if (tasks && !tasks.length) return;
  return sortByKey(
    tasks && (tasks.filter(task => task.projectTaskSection && task.projectTaskSection.id === section.id) || []),
    'sequence',
  );
}

export function getColumns(tasks = [], sections = []) {
  const columns = [
    {
      id: -1,
      sequence: 0,
      records:
        tasks &&
        tasks.length &&
        sortByKey(
          (tasks || []).filter(t => !t.projectTaskSection),
          'sequence',
        ),
    },
  ];
  sections &&
    sections.forEach(section => {
      columns.push({
        ...section,
        records: getTasks(tasks, section),
      });
    });
  return sortByKey(columns, 'sequence');
}

export function getTitle(field) {
  return field.title || field.autoTitle || field.name;
}

export function getJsonField(mainField, field) {
  const path = field.modelField || mainField.name;
  const type = field.type || field.jsonType;
  let _name = `::${field.jsonType || 'text'}`;

  if (field.target && field.targetName) {
    _name = ``;
  }
  return {
    ...field,
    title: `${getTitle(field)} ${getTitle(mainField) && `(${getTitle(mainField)})`}`,
    name: `${path}.${field.name}${_name}`,
    type: type.replace(/-/g, '_').toUpperCase(),
  };
}

export function reorder(list, startIndex, endIndex) {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);
  return result;
}

export function transformCriteria(criteria = [], fields = []) {
  function getCriteria(obj) {
    const { criteria, fieldName, operator, value } = obj;
    if (criteria && Array.isArray(criteria)) {
      return { ...obj, criteria: criteria.map(getCriteria) };
    }
    if (fieldName && operator) {
      const fieldInfo = fields.find(x => x.name === fieldName);
      // relational field
      if (fieldInfo && fieldInfo.target) {
        const { targetName } = fieldInfo;
        const getValue = obj => obj && (typeof obj === 'object' ? obj.id : obj);
        let name = `${fieldName}${targetName ? `.${targetName}` : ''}`;
        if (['in', 'notin'].includes(operator.toLowerCase())) {
          name = `${fieldName}.id`;
        }
        return {
          ...obj,
          fieldName: name,
          value: Array.isArray(value) ? value.map(getValue) : getValue(value),
        };
      }
      if (['isTrue', 'isFalse'].includes(operator)) {
        return {
          ...obj,
          operator: '=',
          value: operator === 'isTrue' ? 'true' : 'false',
        };
      }
    }
    return obj;
  }
  return getCriteria({ criteria });
}

export function getFormatedDate(date) {
  if (!date) return;
  return moment(date).format('YYYY-MM-DD');
}

export function getAttachmentBlob(file) {
  return file.file;
}

export function getHeaders(file, offset) {
  const attachment = file.file;
  if (!attachment) {
    return;
  }
  const headers = {
    'X-File-Name': attachment.name,
    'X-File-Offset': offset,
    'X-File-Size': attachment.size,
    'X-File-Type': attachment.type,
    'X-CSRF-Token': readCookie('CSRF-TOKEN'),
  };
  if (file.id) {
    headers['X-File-Id'] = file.id;
  }
  return headers;
}

export function getCompletedStatus(project = {}) {
  const { projectTaskStatusSet = [] } = project || {};
  if (projectTaskStatusSet && projectTaskStatusSet.length <= 0) return {};
  const status = projectTaskStatusSet && projectTaskStatusSet.filter(status => status.isDefaultCompleted === true);
  if (!status) return {};
  return status[0] || {};
}

export const getStatus = (task = {}, project) => {
  const { status } = task;
  if (!status) return false;
  let completedStatus = getCompletedStatus(project);
  if (!completedStatus) return false;
  return completedStatus.id === status.id;
};

export function translate(str) {
  if (window.top && window.top._t && typeof str === "string") {
    return window.top._t(str);
  }
  return str;
}

export function getSortOptions(project) {
  const { isShowPriority = false, isShowStatus = false, isShowProgress = false, isShowTaskCategory = false } =
    project || {};
  let options = [...SORT_COLUMNS];
  if (!isShowPriority) {
    options = options.filter(option => option.code !== 'priority');
  }
  if (!isShowStatus) {
    options = options.filter(option => option.code !== 'status');
  }
  if (!isShowProgress) {
    options = options.filter(option => option.code !== 'progressSelect');
  }
  if (!isShowTaskCategory) {
    options = options.filter(option => option.code !== 'projectTaskCategory');
  }
  return options;
}
