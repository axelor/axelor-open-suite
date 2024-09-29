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
package com.axelor.apps.project.web;

import com.axelor.apps.base.db.CommentFile;
import com.axelor.apps.base.db.repo.CommentFileRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.service.comment.CommentFileService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CommentFileController {

  public void deleteCommentFile(ActionRequest request, ActionResponse response) {

    try {
      CommentFile commentFile = request.getContext().asType(CommentFile.class);
      commentFile = Beans.get(CommentFileRepository.class).find(commentFile.getId());
      Beans.get(CommentFileService.class).deleteCommentFile(commentFile);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
