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

import com.axelor.apps.base.db.CommentFile;
import com.axelor.apps.base.db.repo.CommentFileRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CommentFileServiceImpl implements CommentFileService {

  public MetaFileRepository metaFileRepo;
  public CommentFileRepository commentFileRepo;
  public MailMessageRepository mailMessageRepo;

  @Inject
  public CommentFileServiceImpl(
      MetaFileRepository metaFileRepo,
      CommentFileRepository commentFileRepo,
      MailMessageRepository mailMessageRepo) {

    this.metaFileRepo = metaFileRepo;
    this.commentFileRepo = commentFileRepo;
    this.mailMessageRepo = mailMessageRepo;
  }

  @Override
  @Transactional
  public void deleteCommentFile(CommentFile commentFile) {

    if (commentFile != null) {
      ProjectTask projectTask = commentFile.getRelatedComment().getProjectTask();
      String body =
          "<ul><li>"
              + "(<del>"
              + commentFile.getAttachmentFile().getFileName()
              + "</del>)"
              + "</li></ul>";

      MailMessage message = new MailMessage();

      message.setBody(body);
      message.setRelatedId(projectTask.getId());
      message.setRelatedModel(ProjectTask.class.getName());
      message.setAuthor(projectTask.getUpdatedBy());

      mailMessageRepo.save(message);

      metaFileRepo.remove(commentFile.getAttachmentFile());
      commentFileRepo.remove(commentFile);
    }
  }
}
