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
import React, { useEffect, useState, useRef } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  IconButton,
  DialogContent,
  DialogTitle,
  CircularProgress,
} from '@material-ui/core';
import { Document, Page, pdfjs } from 'react-pdf';
import { pdf, PDFViewer } from '@react-pdf/renderer';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { makeStyles } from '@material-ui/core/styles';
import { ArrowLeft, ArrowRight, GetApp } from '@material-ui/icons';
import { getMessages } from '../../../Services/api';
import { translate } from '../../../utils';
import PdfDocument from './PdfDocument';

const useStyles = makeStyles(() => ({
  paper: {
    minWidth: '50%',
    width: '100%',
    height: '100%',
  },
  pdfViewer: {
    width: '100%',
    height: '100%',
  },
  dialogContent: {
    overflow: 'hidden',
    padding: 0,
  },
  pdfNavigation: {
    display: 'flex',
    justifyContent: 'space-between',
  },
  button: {
    margin: 10,
    color: '#3f51b5',
    border: '1px solid rgba(63, 81, 181, 0.5)',
    textTransform: 'none',
  },
  pdfView: {
    overflow: 'auto',
    height: '100%',
    width: '100%',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
  },
}));

pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.js`;

export default function PdfDialog({ open, handleClose, task, subTasks }) {
  const classes = useStyles();
  const [comments, setComments] = useState(null);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [documentURL, setDocumentURL] = useState();
  const isMobile = useMediaQuery('(max-width:600px)');

  const downloadPdf = () => {
    let link = document.createElement('a');
    link.href = documentURL;
    link.download = `${(task && task.name) || 'Task'}.pdf`;
    link.dispatchEvent(new MouseEvent('click'));
  };

  const ObjectUrlRef = useRef(null);
  useEffect(() => {
    const taskId = task && task.id;
    if (taskId && open) {
      const generateBlob = async () => {
        setLoading(true);
        let res = await getMessages({
          id: taskId,
          relatedModel: 'com.axelor.apps.project.db.ProjectTask',
          limit: 25,
        });
        let comments = res && res.comments;
        const blob = await pdf(<PdfDocument task={task} comments={comments} subTasks={subTasks} />).toBlob();
        ObjectUrlRef.current = window.URL.createObjectURL(blob);
        setDocumentURL(ObjectUrlRef.current);
        setComments(comments || null);
        setLoading(false);
      };
      generateBlob();
    }
    return () => {
      if (ObjectUrlRef.current) {
        window.URL.revokeObjectURL(ObjectUrlRef.current);
        ObjectUrlRef.current = null;
      }
    };
  }, [task, subTasks, open]);

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      aria-labelledby="form-dialog-title"
      classes={{
        paper: classes.paper,
      }}
    >
      <DialogTitle id="form-dialog-title">{translate('Download')}</DialogTitle>
      <DialogContent className={classes.dialogContent}>
        {open &&
          !loading &&
          documentURL &&
          (isMobile ? (
            <div className={classes.pdfView}>
              <Document
                file={documentURL}
                onLoadSuccess={result => setTotalPages(result.numPages)}
                loading={<CircularProgress />}
              >
                <Page
                  renderMode="svg"
                  pageNumber={currentPage}
                  width={window.innerWidth - 64}
                  height={window.innerHeight - 64}
                />
              </Document>
              <div className={classes.pdfNavigation}>
                <IconButton disabled={currentPage <= 1} onClick={() => setCurrentPage(currentPage => currentPage - 1)}>
                  <ArrowLeft />
                </IconButton>
                <p>
                  {translate('Page')} {currentPage} of {totalPages}
                </p>
                <IconButton
                  disabled={currentPage >= totalPages}
                  onClick={() => setCurrentPage(currentPage => currentPage + 1)}
                >
                  <ArrowRight />
                </IconButton>
              </div>
            </div>
          ) : (
            // PdfViewer component must be unmounted and remounted every time its prop changes,
            // re-render will lead to crash
            <PDFViewer className={classes.pdfViewer}>
              <PdfDocument task={task} comments={comments} subTasks={subTasks} />
            </PDFViewer>
          ))}
      </DialogContent>
      <DialogActions>
        {isMobile && (
          <Button variant="outlined" className={classes.button} startIcon={<GetApp />} onClick={downloadPdf}>
            {translate('Download')}
          </Button>
        )}
        <Button
          variant="outlined"
          color="primary"
          style={{
            textTransform: 'none',
          }}
          onClick={handleClose}
        >
          {translate('Ok')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
