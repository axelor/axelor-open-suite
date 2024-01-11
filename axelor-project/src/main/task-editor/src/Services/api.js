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
import Service from './index';

const PROJECT_FIELDS = [
  'toInvoice',
  'isShowTaskCategory',
  'isShowPlanning',
  'isShowStatus',
  'productSet',
  'isShowProgress',
  'taskCustomFields',
  'isShowSection',
  'id',
  'synchronize',
  'parentProject',
  'attrs',
  'fromDate',
  'sequence',
  'projectTaskPrioritySet',
  'clientPartner',
  'name',
  'code',
  'description',
  'projectTaskStatusSet',
  'assignedTo',
  'isShowPriority',
  'projectFolderSet',
  'company',
  'currency',
  'contactPartner',
  'membersUserSet',
  'toDate',
  'isBusinessProject',
  'fullName',
  'projectStatus',
  'projectTaskCategorySet',
];

const TASK_FIELDS = [
  'project',
  'projectTaskSection',
  'status',
  'parentTask',
  'name',
  'metaFile',
  'description',
  'assignedTo',
  'taskEndDate',
  'taskDate',
  'taskDeadline',
  'priority',
  'fullName',
  'status.isCompleted',
  'status.isDefaultCompleted',
  'priority.technicalTypeSelect',
  'progressSelect',
  'attrs',
  'statusBeforeComplete',
  'sequence',
  'projectTaskCategory',
];

function filterObjectByKeys(obj, keys) {
  return Object.keys(obj)
    .filter(key => keys.includes(key))
    .reduce((acc, key) => (obj[key] === undefined ? acc : { ...acc, [key]: obj[key] }), {});
}

export async function fetchTasks(options) {
  let res = await Service.search('com.axelor.apps.project.db.ProjectTask', { fields: TASK_FIELDS, ...options });
  if (res && res.data) {
    return res.data;
  }
}

export async function addProject(data) {
  let res = await Service.add('com.axelor.apps.project.db.Project', {
    ...data,
  });
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}

export async function addTask(data, filteredResponse = true) {
  let res = await Service.add('com.axelor.apps.project.db.ProjectTask', {
    ...data,
  });
  if (res && res.data && res.data[0]) {
    if (filteredResponse) return filterObjectByKeys(res.data[0], [...TASK_FIELDS, 'id', 'version']);
    return res.data[0];
  }
}

export async function updateTask(data, completeResponse = false, filteredResponse = true) {
  let res = await Service.add('com.axelor.apps.project.db.ProjectTask', { ...data });
  if (res && res.data && res.data[0]) {
    if (filteredResponse) {
      res.data[0] = filterObjectByKeys(res.data[0], [...TASK_FIELDS, 'id', 'version']);
    }
    if (!completeResponse) {
      return res.data[0];
    }
  }
  return res;
}

export async function deleteTask(id) {
  let res = await Service.delete('com.axelor.apps.project.db.ProjectTask', id);
  return res;
}

export async function fetchSections(options) {
  let res = await Service.search('com.axelor.apps.project.db.ProjectTaskSection', {
    fields: ['name', 'sequence', 'selected'],
    ...options,
  });
  if (res && res.data) {
    return res.data;
  }
}

export async function addSection(data) {
  let res = await Service.add('com.axelor.apps.project.db.ProjectTaskSection', {
    ...data,
  });
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}

export async function updateSection(data, completeResponse = false) {
  let res = await Service.add('com.axelor.apps.project.db.ProjectTaskSection', {
    ...data,
  });
  if (completeResponse) return res;
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}

export async function deleteSection(id) {
  let res = await Service.delete('com.axelor.apps.project.db.ProjectTaskSection', id);
  return res;
}

export async function getMessages({ id, offset = 0, limit = 4, relatedModel } = {}) {
  let res = await Service.get(
    `ws/rest/com.axelor.mail.db.MailMessage/messages?limit=${limit}&offset=${offset}&relatedId=${id}&relatedModel=${relatedModel}`,
  );
  if (res && res.data) {
    return { comments: res.data, offset: res.offset, total: res.total };
  }
}

export async function removeMessage(data) {
  let res = await Service.post(`ws/rest/com.axelor.mail.db.MailMessage/${data.id}/remove`, {
    data,
  });
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}

export async function addMessage({ id, body = '', files = [], entityModel } = {}) {
  let res = await Service.post(`ws/rest/${entityModel}/${id}/message`, {
    data: {
      body,
      files,
      type: 'comment',
    },
  });
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}

export async function getInfo() {
  let res = await Service.get(`ws/app/info`);
  if (res) {
    return res;
  }
}

export async function getFilters() {
  const model = 'com.axelor.meta.db.MetaFilter';

  const method = 'com.axelor.meta.web.MetaFilterController:findFilters';
  const data = {
    data: {
      model,
      domain: null,
      context: {
        filterView: 'task-filters',
        _model: model,
      },
    },
  };

  let res = await Service.post(`/ws/action/${method}`, data);
  if (res && res.data) {
    return res.data;
  }
}

export async function saveFilter(filter) {
  const method = 'com.axelor.meta.web.MetaFilterController:saveFilter';
  const model = 'com.axelor.meta.db.MetaFilter';
  const data = {
    model,
    data: {
      model,
      domain: null,
      context: {
        ...filter,
        ...(filter.name && { name: filter.name }),
        filterView: 'task-filters',
        filters: '',
      },
    },
  };

  return Service.post(`/ws/action/${method}`, data).then(({ data }) => data);
}

export async function deleteFilter(filter) {
  const method = 'com.axelor.meta.web.MetaFilterController:removeFilter';
  const model = 'com.axelor.meta.db.MetaFilter';
  const data = {
    model,
    data: {
      model,
      domain: null,
      context: {
        name: filter.name,
        filterView: 'task-filters',
      },
    },
  };

  return Service.post(`/ws/action/${method}`, data);
}

export async function fetchFields(model) {
  let res = await Service.get(`/ws/meta/fields/${model}?limit=10`);
  if (res && res.data && res.data.fields) {
    return res.data;
  }
}

export async function addDMSFile(data) {
  let res = await Service.add('com.axelor.dms.db.DMSFile', data);
  if (res && res.data) {
    return res.data;
  }
}

export async function uploadFile(blob, headers) {
  let res = await Service.upload(blob, headers);
  if (res && res.result) {
    return res.result;
  }
}

export async function getDMSFiles(relatedId, relatedModel = 'com.axelor.apps.project.db.ProjectTask') {
  let res = await Service.search('com.axelor.dms.db.DMSFile', {
    fields: ['fileName', 'relatedModel', 'relatedId', 'metaFile'],
    data: {
      _domain: `self.isDirectory = false AND self.relatedId = ${relatedId} AND self.relatedModel = '${relatedModel}'`,
      _domainContext: { rid: relatedId, rmodel: relatedModel },
    },
  });
  if (res && res.data) {
    return res.data;
  }
}

export async function removeDMSFile(options) {
  let res = await Service.post('ws/rest/com.axelor.dms.db.DMSFile/removeAll', {
    records: options,
  });
  if (res && res.data) {
    return res.data;
  }
}

export async function downloadDMSFile(file, getBlob = false) {
  return Service.download(file, getBlob);
}

export async function getProject(id, options) {
  if (!id) return;
  let res = await Service.fetchId('com.axelor.apps.project.db.Project', id, { fields: PROJECT_FIELDS, ...options });
  if (res && res.data && res.data[0]) {
    return res.data[0];
  }
}
