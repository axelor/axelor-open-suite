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
import React from 'react';
import PictureAsPdfIcon from '@material-ui/icons/PictureAsPdf';
import ImageIcon from '@material-ui/icons/Image';
import DescriptionIcon from '@material-ui/icons/Description';
import CloseIcon from '@material-ui/icons/Close';
import { SaveAlt as DownloadIcon } from '@material-ui/icons';
import { IconButton } from '@material-ui/core';
import './styles.css';

function AttachmentList({ files, maxFiles, removeAttachment, downloadAttachment }) {
  let defaultIcon = '';
  let fileItems = [];
  if (!files) return;
  fileItems = Array.prototype.slice.call(files).slice(0, maxFiles);

  return (
    <div className="attachment-list-container">
      {fileItems.map((item, index) => {
        switch (item.fileType) {
          case 'image/png':
          case 'image/jpeg':
            defaultIcon = <ImageIcon />;
            break;
          case 'application/pdf':
            defaultIcon = <PictureAsPdfIcon />;
            break;
          default:
            defaultIcon = <DescriptionIcon />;
        }
        return (
          <div className="attachment-list-item" key={index}>
            <span
              style={{
                paddingBottom: 15,
                display: 'flex',
                alignItems: 'center',
              }}
            >
              <span style={{ padding: 5 }}>{defaultIcon}</span>
              <span style={{ paddingLeft: 5 }}>{item.fileName}</span>
              <IconButton onClick={() => removeAttachment(item)}>
                <CloseIcon />
              </IconButton>
              <IconButton onClick={() => downloadAttachment(item)}>
                <DownloadIcon />
              </IconButton>
            </span>
          </div>
        );
      })}
    </div>
  );
}
export default AttachmentList;
