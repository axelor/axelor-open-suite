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
package com.axelor.apps.project.service.comment;

import com.axelor.apps.base.db.Comment;
import com.axelor.apps.base.db.CommentFile;
import com.axelor.apps.base.db.repo.CommentRepository;
import com.axelor.apps.base.service.comment.CommentServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.mail.db.MailMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class CommentProjectServiceImpl extends CommentServiceImpl implements CommentProjectService {

  @Inject
  public CommentProjectServiceImpl(CommentRepository commentRepo) {

    super(commentRepo);
  }

  @Override
  @Transactional
  public void createComment(ProjectTask projectTask, MailMessage message) {

    Comment comment = createComment(projectTask, message, projectTask.getNote());

    List<CommentFile> commentFileList = projectTask.getCommentFileList();

    if (CollectionUtils.isNotEmpty(commentFileList)) {

      for (CommentFile commentFile : commentFileList) {
        comment.addCommentFileListItem(commentFile);
      }
    }

    projectTask.setNote("");
    projectTask.clearCommentFileList();

    commentRepo.save(comment);
  }

  @Override
  public Comment createComment(ProjectTask projectTask, MailMessage message, String note) {

    Comment comment = new Comment();

    comment.setProjectTask(projectTask);
    comment.setMailMessage(message);
    comment.setIsPrivateNote(projectTask.getIsPrivateNote());
    comment.setNote(note);
    comment.setSequence(computeSequence(projectTask));

    return comment;
  }

  @Override
  public int computeSequence(ProjectTask projectTask) {

    return CollectionUtils.isNotEmpty(projectTask.getCommentList())
        ? projectTask.getCommentList().size() + 1
        : 1;
  }

  @Override
  public void createCommentWithOnlyAttachment(ProjectTask projectTask) {

    MailMessage message = new MailMessage();
    message.setRelatedId(projectTask.getId());
    message.setRelatedModel(ProjectTask.class.getName());
    message.setAuthor(projectTask.getUpdatedBy());

    createComment(projectTask, message);
  }
}
