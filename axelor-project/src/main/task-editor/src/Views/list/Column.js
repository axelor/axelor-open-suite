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
