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
import { Paper, Fade } from '@material-ui/core';
import { Draggable } from 'react-beautiful-dnd';
import { makeStyles } from '@material-ui/core/styles';

import Header from './Header';
import CardList from './CardList';

const SPACING = 8;
const useStyles = makeStyles(theme => ({
  column: {
    backgroundColor: '#fafafa',
    margin: SPACING,
    position: 'relative',
    boxShadow: 'none',
    [theme.breakpoints.only('xs')]: {
      marginLeft: 0,
      marginRight: 0,
    },
  },
}));

export default function Column({ index, column, customHeader = false }) {
  const { collapsed = false, id } = column;
  const classes = useStyles();
  return (
    <Draggable draggableId={`column_${id}`} index={index}>
      {provided => (
        <div {...provided.draggableProps} {...provided.dragHandleProps} ref={provided.innerRef}>
          <Paper className={classes.column}>
            <Header column={column} {...provided.dragHandleProps} index={index} isCustom={customHeader} />
            {!collapsed && (
              <Fade in={true} timeout={500}>
                <div>
                  <CardList column={column} />
                </div>
              </Fade>
            )}
          </Paper>
        </div>
      )}
    </Draggable>
  );
}

Column.defaultProps = {
  index: null,
  column: {},
  isScroll: false,
};
