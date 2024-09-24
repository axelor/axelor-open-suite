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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.base.db.Comment;
import com.axelor.apps.base.db.repo.CommentBaseRepository;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class CommentProjectRepository extends CommentBaseRepository {

  @Override
  public void remove(Comment entity) {

    List<Comment> commentList =
        all()
            .filter(
                "self.id != ?1 and self.projectTask = ?2", entity.getId(), entity.getProjectTask())
            .order("sequence")
            .fetch();

    if (CollectionUtils.isNotEmpty(commentList)) {
      int seq = 1;

      for (Comment comment : commentList) {
        comment.setSequence(seq++);

        if (entity.equals(comment.getParentComment())) {
          comment.setParentComment(null);
        }

        save(comment);
      }
    }

    super.remove(entity);
  }
}
