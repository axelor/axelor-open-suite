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
