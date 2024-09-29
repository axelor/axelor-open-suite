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
package com.axelor.apps.base.service.comment;

import com.axelor.apps.base.db.Comment;
import com.axelor.apps.base.db.CommentFile;
import com.axelor.apps.base.db.repo.CommentRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.mail.db.MailMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class CommentServiceImpl implements CommentService {

  public CommentRepository commentRepo;

  @Inject
  public CommentServiceImpl(CommentRepository commentRepo) {

    this.commentRepo = commentRepo;
  }

  @Override
  @Transactional
  public void deleteComment(Comment comment) {

    if (StringUtils.isEmpty(comment.getNote())) {
      return;
    }

    List<CommentFile> commentFileList = comment.getCommentFileList();
    MailMessage mailMessage = comment.getMailMessage();

    if (CollectionUtils.isEmpty(commentFileList)
        && mailMessage != null
        && !StringUtils.isBlank(mailMessage.getBody())) {

      try {
        JSONObject jsonObject = new JSONObject(mailMessage.getBody());
        JSONArray jsonArray = jsonObject.optJSONArray("tracks");

        if (jsonArray != null && jsonArray.length() == 1) {
          JSONObject track = jsonArray.getJSONObject(0);

          if ("comment.note".equals(track.getString("title"))) {
            commentRepo.remove(comment);
            return;
          }
        }
      } catch (JSONException e) {
        TraceBackService.trace(e);
      }
    }

    comment.setNote("");
    commentRepo.save(comment);
  }
}
