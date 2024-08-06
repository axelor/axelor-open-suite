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
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import draftToHtml from 'draftjs-to-html';
import htmlToDraft from 'html-to-draftjs';
import { EditorState, convertToRaw, ContentState } from 'draft-js';
import { Editor } from 'react-draft-wysiwyg';
import { makeStyles } from '@material-ui/core/styles';

import './styles.css';
import 'react-draft-wysiwyg/dist/react-draft-wysiwyg.css';

const Viewer = ({ html }) => <div className="ax-base-draft-editor-viewer" dangerouslySetInnerHTML={{ __html: html }} />;

const useStyles = makeStyles(() => ({
  editorClass: {
    minHeight: 100,
    border: '1px solid #eee',
    marginTop: -7,
    borderTop: 'none',
    padding: 10,
  },
}));

function EditorComponent(props) {
  const { onContentChange, content: propContent, onDescriptionChange, rest } = props;
  const [editorState, setEditorState] = useState(EditorState.createEmpty());
  const [content, setStateContent] = useState(propContent);
  const classes = useStyles();
  useEffect(() => {
    setContent(propContent);
    setStateContent(propContent);
  }, [propContent]);

  const setContent = html => {
    const blocksFromHtml = htmlToDraft(html);
    const { contentBlocks, entityMap } = blocksFromHtml;
    const contentState = ContentState.createFromBlockArray(contentBlocks, entityMap);
    const editorState = EditorState.createWithContent(contentState);
    setEditorState(editorState);
  };

  const getContent = e => {
    const html = draftToHtml(convertToRaw(e.getCurrentContent()));
    return html;
  };

  const onEditorStateChange = e => {
    setEditorState(e);
    setStateContent(getContent(e));
    onContentChange(content);
  };

  return (
    <div {...rest}>
      <Editor
        editorState={editorState}
        editorClassName={classes.editorClass}
        onEditorStateChange={onEditorStateChange}
        toolbar={{
          options: ['inline', 'link', 'emoji', 'colorPicker', 'list', 'textAlign'],
          inline: {
            inDropdown: true,
          },
          list: {
            inDropdown: true,
          },
          textAlign: {
            inDropdown: true,
          },
        }}
        onBlur={e => {
          e.stopPropagation();
          if (content === propContent) return;
          onDescriptionChange(content);
        }}
        // mention={{
        //   separator: " ",
        //   trigger: "@",
        //   suggestions: [
        //     { text: "APPLE", value: "apple", url: "apple" },
        //     Add users
        //   ],
        // }}
      />
    </div>
  );
}

EditorComponent.propTypes = {
  content: PropTypes.any.isRequired,
  onContentChange: PropTypes.func.isRequired,
};

EditorComponent.Viewer = Viewer;

export default EditorComponent;
