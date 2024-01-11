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
import { readCookie } from '../utils';

const joinPath = (baseURL = '', subURL = '') => {
  let sep = `${baseURL}`.lastIndexOf('/') === baseURL.length - 1 ? '' : '/';
  return `${baseURL}${sep}${subURL}`;
};

export class Service {
  constructor() {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'application/json');
    headers.append('X-Requested-With', 'XMLHttpRequest');
    headers.append('X-CSRF-Token', readCookie('CSRF-TOKEN'));
    this.baseURL = process.env.NODE_ENV === 'production' ? '../..' : 'axelor-portal';
    this.headers = headers;
  }

  fetch(url, method, options) {
    return fetch(url, options)
      .then(data => {
        if (['head'].indexOf(method.toLowerCase()) !== -1) return data;
        let isJSON = data.headers.get('content-type').includes('application/json');
        return isJSON ? data.json() : data;
      })
      .catch(err => {});
  }

  request(url, config = {}, data = {}) {
    const options = Object.assign(
      {
        method: 'POST',
        credentials: 'include',
        headers: this.headers,
        mode: 'cors',
        body: JSON.stringify(data),
      },
      config,
    );
    if (config.method === 'GET') {
      delete options.body;
    }
    return this.fetch(`${this.baseURL}${url.indexOf('/') === 0 ? url : `/${url}`}`, config.method, options);
  }

  post(url, data) {
    const config = {
      method: 'POST',
    };
    return this.request(url, config, data);
  }

  get(url) {
    const config = {
      method: 'GET',
    };
    return this.request(url, config);
  }

  add(entity, record) {
    const data = {
      data: record,
    };
    const url = `ws/rest/${entity}`;
    return this.post(url, data);
  }

  fetchId(entity, id, data = {}) {
    const url = `ws/rest/${entity}/${id}/fetch`;
    return this.post(url, data);
  }

  delete(entity, id) {
    const config = {
      method: 'DELETE',
    };
    const url = `ws/rest/${entity}/${id}`;
    return this.request(url, config);
  }

  search(entity, options) {
    const data = {
      offset: 0,
      ...options,
    };
    const url = `ws/rest/${entity}/search`;
    return this.post(url, data);
  }

  upload(data = null, headers = {}, callback = () => true, info = {}) {
    return new Promise((resolve, reject) => {
      const baseURL = this.baseURL;
      const xhr = new XMLHttpRequest(),
        method = 'POST',
        url = joinPath(this.baseURL, 'ws/files/upload');

      const doClean = () =>
        headers['X-File-Id']
          ? this.intercept(() => this.http.delete(joinPath(this.baseURL, 'ws/files/upload/' + headers['X-File-Id'])))
          : Promise.resolve(true);

      const formatSize = (done, total) => {
        const format = size => {
          if (size > 1000000000) return parseFloat(size / 1000000000).toFixed(2) + ' GB';
          if (size > 1000000) return parseFloat(size / 1000000).toFixed(2) + ' MB';
          if (size >= 1000) return parseFloat(size / 1000).toFixed(2) + ' KB';
          return size + ' B';
        };
        return format(done || 0) + '/' + format(total);
      };

      xhr.open(method, url, true);

      Object.keys(headers).forEach(k => {
        xhr.setRequestHeader(k, headers[k]);
      });

      xhr.withCredentials = true;
      xhr.overrideMimeType('application/octet-stream');
      xhr.setRequestHeader('Content-Type', 'application/octet-stream');
      xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

      xhr.onload = () => {
        callback(100);
      };

      info.abort = () => {
        xhr.abort();
        return doClean();
      };

      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          switch (xhr.status) {
            case 401:
              this.callbackInterceptor();
              throw new Error('Unauthorized');
            case 200:
              try {
                const result = JSON.parse(xhr.responseText);
                resolve({
                  result,
                  url: `${baseURL}ws/rest/com.axelor.meta.db.MetaFile/${result.id}/content/download?v=0`,
                });
              } catch (e) {
                resolve(xhr.responseText);
              }
              break;
            default:
              doClean();
              reject({ status: xhr.status });
              break;
          }
        }
      };

      xhr.upload.onprogress = e => {
        const fileSize = headers['X-File-Size'];
        const total = parseFloat(headers['X-File-Offset']) + e.loaded;
        const done = Math.round((total / fileSize) * 100);

        info.progress = done > 95 ? '95%' : done + '%';
        info.transfer = formatSize(total, fileSize);
        info.loaded = total === fileSize;

        if (e.lengthComputable) {
          callback((e.loaded / e.total) * 100, info);
        }
      };

      xhr.send(data);
    });
  }

  download(file, getBlob = false) {
    const url = `${this.baseURL}/ws/dms/download/${file.id}`;
    return new Promise(function (resolve, reject) {
      var req = new XMLHttpRequest();
      req.open('GET', url, true);
      req.responseType = 'blob';
      req.onload = function (event) {
        var blob = req.response;
        if (getBlob) {
          resolve(blob);
        } else {
          var link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = file.isDirectory ? `${file.fileName}.zip` : file.fileName;
          link.click();
          resolve();
        }
      };

      req.send();
    });
  }
}

export default new Service();
