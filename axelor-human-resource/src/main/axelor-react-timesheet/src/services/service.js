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
import request from "superagent";

const headers = {
  Accept: "application/json",
  "Content-Type": "application/json",
  "X-Requested-With": "XMLHttpRequest",
};

let lastCookieString;
let lastCookies = {};

function readCookie(name) {
  let cookieString = document.cookie || "";
  if (cookieString !== lastCookieString) {
    lastCookieString = cookieString;
    lastCookies = cookieString.split("; ").reduce((obj, value) => {
      let parts = value.split("=");
      obj[parts[0]] = parts[1];
      return obj;
    }, {});
  }
  return lastCookies[name];
}

class Services {
  constructor(props) {
    const baseURL =
      process.env.NODE_ENV === "production" ? "../../" : "/axelor-erp/";
    this.state = {
      restURL: `${baseURL}ws/rest/`,
      actionURL: `${baseURL}ws/action/`,
      baseURL,
      actionName:
        "com.axelor.apps.hr.service.timesheet.TimesheetService:getCurrentOrCreateTimesheet()",
    };
  }

  getHeaders() {
    return {
      ...headers,
      "X-CSRF-Token": readCookie("CSRF-TOKEN"),
    };
  }

  doLogin(userName, password) {
    const url = `${this.state.baseURL}login.jsp`;
    const data = {
      username: userName,
      password: password,
    };
    return fetch(url, {
      method: "POST",
      headers,
      credentials: "include",
      body: JSON.stringify({ ...data }),
    }).then((res) => {
      const token = res.headers.get("csrf-token");
      token && (document.cookie = `CSRF-TOKEN=${token}`);
      return res;
    });
  }

  info() {
    return fetch(`${this.state.baseURL}ws/app/info`, {
      method: "GET",
      credentials: "include",
      headers: this.getHeaders(),
    })
      .then((res) => {
        return res && res.json();
      })
      .then((body) => {
        return body;
      });
  }

  updateModel(model, entity) {
    return fetch(`${this.state.restURL}${entity}`, {
      method: "POST",
      credentials: "include",
      headers: this.getHeaders(),
      body: JSON.stringify({
        data: model,
      }),
    })
      .then((response) => {
        return response.json();
      })
      .then((body) => {
        return body;
      });
  }

  updateTimeSheet(data, version, id) {
    const timeSheetLine = [];
    data.forEach((line) => {
      const record = {};
      record["id"] = line.id || null;
      record.version = line.version;
      record.project = { id: line.projectId };
      record.product = { id: line.taskId };
      record.duration = line.duration;
      record.hoursDuration = line.hoursDuration;
      record.date = line.date;
      timeSheetLine.push(record);
    });
    return new Promise((resolve, reject) => {
      request
        .post(`${this.state.restURL}com.axelor.apps.hr.db.Timesheet`)
        .withCredentials()
        .send({
          data: {
            id,
            version,
            timesheetLineList: timeSheetLine,
          },
        })
        .set("Accept", "application/json")
        .set("Content-Type", "application/json")
        .set("X-Requested-With", "XMLHttpRequest")
        .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
        .end(function (err, res) {
          if (res) {
            resolve(res);
          } else {
            reject(err);
          }
        });
    });
  }

  fetch(entity, id) {
    return new Promise((resolve, reject) => {
      if (entity && id) {
        request
          //.post(`${this.state.actionURL}${this.state.actionName}`)
          .post(`${this.state.restURL}${entity}/${id}/fetch`)
          .withCredentials()
          .send({
            data: {},
          })
          .set("Accept", "application/json")
          .set("Content-Type", "application/json")
          .set("X-Requested-With", "XMLHttpRequest")
          .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
          .end(function (err, res) {
            const { body } = res;
            resolve({ body });
          });
      } else {
        reject(false);
      }
    });
  }

  fetchTimesheet(timeSheetId = null, entity, sortBy = []) {
    const context = this;
    let id;
    return new Promise((resolve, reject) => {
      if (timeSheetId && entity) {
        request
          .post(`${this.state.restURL}${entity}/${timeSheetId}/fetch`)
          .withCredentials()
          .send({
            data: {},
          })
          .set("Accept", "application/json")
          .set("Content-Type", "application/json")
          .set("X-Requested-With", "XMLHttpRequest")
          .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
          .end(function (err, res) {
            const { body } = res;
            if (body.status === -1) {
              reject(body);
            } else {
              if (body === null) {
                resolve({ timeline: null });
              } else {
                if (body.data) {
                  let record = body.data[0];
                  id = record.id;
                  const criteria = [];
                  const value = [];
                  if (record.timesheetLineList.length > 0) {
                    record.timesheetLineList.forEach(({ id }) =>
                      value.push(id)
                    );
                  }
                  criteria.push({ fieldName: "id", operator: "in", value });
                  if (criteria.length === 0) {
                    resolve({
                      timeline: [],
                      timesheetVersion: record.version,
                      timesheet: record,
                      timesheetId: id,
                      fromDate: record.fromDate,
                      toDate: record.toDate,
                      user: record.user,
                    });
                  }
                  context
                    .search(
                      "com.axelor.apps.hr.db.TimesheetLine",
                      criteria,
                      0,
                      -1,
                      sortBy,
                      []
                    )
                    .then((body) => {
                      const timeline = [];
                      body &&
                        body.data &&
                        body.data.length &&
                        body.data.forEach((task) => {
                          timeline.push({
                            id: task.id,
                            version: task.version,
                            projectId: task.project ? task.project.id : null,
                            project: task.project ? task.project.fullName : "",
                            date: task.date,
                            duration: Number(task.hoursDuration),
                            taskId: task.product ? task.product.id : null,
                            projectTask: task.projectTask,
                            projectTaskId:
                              task.projectTask && task.projectTask.id,
                            task: task.product ? task.product.fullName : "",
                            enableEditor: task.enableEditor,
                          });
                        });
                      resolve({
                        timeline,
                        timesheetVersion: record.version,
                        timesheet: record,
                        timesheetId: id,
                        fromDate: record.fromDate,
                        toDate: record.toDate,
                        user: record.user,
                      });
                    }, reject);
                } else {
                  resolve({ timeline: null });
                }
              }
            }
          });
      } else {
        resolve({ timeline: null });
      }
    });
  }

  getAction(model, action, data = {}) {
    let body = JSON.stringify({
      action: `${action}`,
      model: `${model}`,
      data: data,
    });
    return new Promise((resolve, reject) => {
      request
        .post(`${this.state.actionURL}`)
        .withCredentials()
        .send(body)
        .set("Accept", "application/json")
        .set("Content-Type", "application/json")
        .set("X-Requested-With", "XMLHttpRequest")
        .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
        .end(function (err, res) {
          if (res && res.body && res.body.data) {
            resolve(res.body.data[0]);
          } else {
            reject(err);
          }
        });
    });
  }

  remove(id, entity) {
    return new Promise((resolve, reject) => {
      request
        .delete(`${this.state.restURL}${entity}/${id}`)
        .withCredentials()
        .set("Accept", "application/json")
        .set("Content-Type", "application/json")
        .set("X-Requested-With", "XMLHttpRequest")
        .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
        .end(function (err, res) {
          if (res) {
            resolve(res);
          } else {
            reject(err);
          }
        });
    });
  }

  removeAll(ids, entity) {
    return new Promise((resolve, reject) => {
      request
        .post(`${this.state.restURL}${entity}/removeAll`)
        .withCredentials()
        .send({
          records: ids,
        })
        .set("Accept", "application/json")
        .set("Content-Type", "application/json")
        .set("X-Requested-With", "XMLHttpRequest")
        .set("X-CSRF-Token", readCookie("CSRF-TOKEN"))
        .end(function (err, res) {
          if (res) {
            resolve(res.body);
          } else {
            reject(err);
          }
        });
    });
  }

  search(
    entity,
    criteria = [],
    offset,
    limit,
    sortBy,
    fields = [],
    domain = null
  ) {
    let body = JSON.stringify({
      data: {
        _domain: domain,
        criteria: criteria,
        operator: "and",
      },
      offset: offset,
      limit: limit,
      sortBy: sortBy,
    });
    if (fields.length > 0) {
      let temp = JSON.parse(body);
      temp.fields = fields;
      body = JSON.stringify(temp);
    }

    return fetch(`${this.state.restURL}${entity}/search`, {
      method: "POST",
      credentials: "include",
      headers: this.getHeaders(),
      body,
    })
      .then((response) => {
        return response && response.json();
      })
      .then((body) => {
        return body;
      });
  }
}

export default Services;
