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
import React, { useState, useEffect, useRef, useCallback } from 'react';
import classnames from 'classnames';
import NestedMenuItem from 'material-ui-nested-menu-item';
import {
  Typography,
  Button,
  Paper,
  IconButton,
  Fade,
  Menu,
  MenuItem,
  Grow,
  Popper,
  ClickAwayListener,
  MenuList,
} from '@material-ui/core';
import { DragDropContext, Droppable } from 'react-beautiful-dnd';
import { makeStyles } from '@material-ui/core/styles';
import useMediaQuery from '@material-ui/core/useMediaQuery';

import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import AddIcon from '@material-ui/icons/Add';
import DoneIcon from '@material-ui/icons/Done';
import ImportExportIcon from '@material-ui/icons/ImportExport';
import FilterListIcon from '@material-ui/icons/FilterList';
import ClearIcon from '@material-ui/icons/Clear';
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward';
import { Refresh } from '@material-ui/icons';
import Column from './Column';
import { addSection } from '../../Services/api';
import { AdvancedSearch } from '../../Components';
import { sortByKey, translate, getSortOptions } from '../../utils';
import { SORT_COLUMNS, TASK_FILTERS_MENU } from '../../constants';
import { useTaskEditor } from './Context';

const SPACING = 8;
const TOOLBAR_HEIGHT = 50;

const useStyles = makeStyles(theme => ({
  toolbar: {
    padding: '8px 12px',
    textAlign: 'left',
    display: 'flex',
    height: TOOLBAR_HEIGHT,
    boxSizing: 'border-box',
    justifyContent: 'space-between',
    [theme.breakpoints.down('xs')]: {
      height: 100,
      minHeight: `${100}px !important`,
      flexDirection: 'column',
    },
  },
  container: {
    display: 'flex',
    [theme.breakpoints.down('xs')]: {
      justifyContent: 'space-between',
    },
  },
  contentWrapper: {
    height: `calc(100% - ${TOOLBAR_HEIGHT}px)`,
    width: '100%',
    display: 'flex',
    backgroundColor: 'white',
    overflow: 'auto',
    flexDirection: 'column',
    [theme.breakpoints.down('xs')]: {
      height: `calc(100% - ${TOOLBAR_HEIGHT * 2}px)`,
      overflowX: 'hidden',
    },
  },
  menuItem: {
    '&:hover': {
      backgroundColor: '#e8ecee',
    },
  },
  addTaskButton: {
    textTransform: 'none',
    borderRadius: 0,
    borderTopLeftRadius: 5,
    borderBottomLeftRadius: 5,
    whiteSpace: 'nowrap',
  },
  menuButton: {
    padding: 5,
    borderRadius: 0,
    border: '0.5px solid #9EA7DA',
    marginLeft: 2,
    marginRight: 10,
    borderTopRightRadius: 5,
    borderBottomRightRadius: 5,
    '&:hover': {
      border: '0.5px solid #3F51B5',
    },
  },
  addSectionButton: {
    width: 'fit-content',
    textTransform: 'none',
    color: '#b4b6b8',
    '&:hover': {
      color: '#3F51B5 !important',
    },
  },
  toolbarHeader: {
    minWidth: 216,
    borderLeft: '1px solid #E8ECEE',
    borderTop: '1px solid #E8ECEE',
    borderBottom: '1px solid #E8ECEE',
    padding: SPACING * 2,
    background: 'white',
    textAlign: 'left',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
  },
  toolbarSortIcon: {
    height: 16,
    width: 16,
    marginLeft: 10,
    color: '#232933',
  },
  toolbarSortIconOnHover: {
    '& > .toolbarSortIcon': {
      height: 16,
      width: 16,
      marginLeft: 10,
      color: 'transparent',
    },

    '&:hover > .toolbarSortIcon': {
      color: '#232933',
    },
  },
  taskNameHeader: {
    minWidth: 373,
    width: '100%',
    position: 'sticky',
    left: 0,
    background: 'white',
    zIndex: 500,
  },
  parentHeader: {
    display: 'flex',
    padding: '0px 8px',
    position: 'sticky',
    top: 0,
    zIndex: 500,
    width: '100%',
    background: 'white',
    [theme.breakpoints.only('xs')]: {
      padding: 0,
    },
  },
  btnParent: {
    minWidth: 430,
    maxWidth: 430,
    position: 'sticky',
    left: 0,
    padding: '0px 16px',
    textAlign: 'left',
  },
  contextMenuItem: {
    fontSize: 14,
  },
  contextSVGIcon: {
    fill: '#6f7782',
    height: 16,
    width: 16,
    marginRight: 10,
  },
  toolbarButton: {
    textTransform: 'none',
    border: 'none',
    color: '#32325d',
    marginRight: 10,
    '&:hover': {
      background: '#e8ecee',
      borderColor: '#e8ecee',
      color: '#273240',
    },
  },
  iconSelected: {
    fontSize: 20,
  },
  clearIcon: {
    height: 16,
    width: 16,
    marginLeft: 10,
  },
  menuTypography: {
    fontSize: 14,
    marginLeft: 10,
  },
  sortingOptions: {
    display: 'flex',
    alignItems: 'center',
  },
  menuPaper: {
    border: '1px solid #d3d4d5',
  },
  refreshIcon: {
    minWidth: 'min-content',
    borderRadius: 0,
  },
}));

const anchorOrigin = {
  vertical: 'bottom',
  horizontal: 'center',
};

const transformOrigin = {
  vertical: 'top',
  horizontal: 'center',
};

export default function List() {
  const isMobile = useMediaQuery('(max-width:600px)');
  const {
    columns,
    onCardMove,
    onColumnAdd,
    onColumnMove,
    sections = [],
    searchFilter,
    filters,
    handleChangeFilter,
    handleSaveFilter,
    handleDeleteFilter,
    handleContentSearch,
    fields = [],
    addNewTask,
    handleSetMenuFilter,
    menuFilter,
    filter,
    handleSetFilter,
    sortColumnName,
    handleSetSortColumnName,
    selectedProject,
    openTaskInDrawer: propOpen = false,
    forceRefresh,
  } = useTaskEditor();
  const [anchorFilterEl, setAnchorFilterEl] = useState(null);
  const openFilterMenu = Boolean(anchorFilterEl);
  const [anchorTaskFilterEl, setAnchorTaskFilterEl] = useState(null);
  const openFilterTaskMenu = Boolean(anchorTaskFilterEl);
  const [anchorTaskSortEl, setAnchorTaskSortEl] = useState(null);
  const openSortTaskMenu = Boolean(anchorTaskSortEl);
  const [sortOptions, setSortOptions] = useState([]);
  const boardRef = useRef(null);
  const { isShowProgress = false } = selectedProject || {};
  const classes = useStyles();
  const FILTERS_MENU = [
    {
      code: 'justMyTasks',
      value: 'Just my tasks',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M16,18c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8S20.4,18,16,18z M16,4c-3.3,0-6,2.7-6,6s2.7,6,6,6s6-2.7,6-6S19.3,4,16,4z M29,32c-0.6,0-1-0.4-1-1v-4.2c0-2.6-2.2-4.8-4.8-4.8H8.8C6.2,22,4,24.2,4,26.8V31c0,0.6-0.4,1-1,1s-1-0.4-1-1v-4.2C2,23,5,20,8.8,20h14.4c3.7,0,6.8,3,6.8,6.8V31C30,31.6,29.6,32,29,32z"></path>
        </svg>
      ),
    },
    {
      code: 'dueThisWeek',
      value: 'Due this week',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M24,2V1c0-0.6-0.4-1-1-1s-1,0.4-1,1v1H10V1c0-0.6-0.4-1-1-1S8,0.4,8,1v1C4.7,2,2,4.7,2,8v16c0,3.3,2.7,6,6,6h16c3.3,0,6-2.7,6-6V8C30,4.7,27.3,2,24,2z M28,24c0,2.2-1.8,4-4,4H8c-2.2,0-4-1.8-4-4V8c0-2.2,1.8-4,4-4v1c0,0.6,0.4,1,1,1s1-0.4,1-1V4h12v1c0,0.6,0.4,1,1,1s1-0.4,1-1V4c2.2,0,4,1.8,4,4V24z M12,10h8.8v2.1L15.5,22h-2.8l5.2-9.7H12V10z"></path>
        </svg>
      ),
    },
    {
      code: 'dueNextWeek',
      value: 'Due next week',
      icon: (
        <svg className={classes.contextSVGIcon} focusable="false" viewBox="0 0 32 32">
          <path d="M24,2V1c0-0.6-0.4-1-1-1s-1,0.4-1,1v1H10V1c0-0.6-0.4-1-1-1S8,0.4,8,1v1C4.7,2,2,4.7,2,8v16c0,3.3,2.7,6,6,6h16 c3.3,0,6-2.7,6-6V8C30,4.7,27.3,2,24,2z M28,24c0,2.2-1.8,4-4,4H8c-2.2,0-4-1.8-4-4V8c0-2.2,1.8-4,4-4v1c0,0.6,0.4,1,1,1s1-0.4,1-1 V4h12v1c0,0.6,0.4,1,1,1s1-0.4,1-1V4c2.2,0,4,1.8,4,4V24z M23.8,16.4c0.3,0.4,0.3,0.9,0,1.3l-4.5,5.5c-0.2,0.2-0.5,0.3-0.8,0.3 c-0.2,0-0.5-0.1-0.6-0.2c-0.4-0.4-0.5-1-0.1-1.4l3.1-3.9H10c-0.6,0-1-0.4-1-1s0.4-1,1-1h10.9l-3.1-3.9c-0.4-0.4-0.3-1.1,0.1-1.4 c0.4-0.4,1.1-0.3,1.4,0.1L23.8,16.4z"></path>
        </svg>
      ),
    },
  ];

  const handleSortMenuClick = event => {
    setAnchorTaskSortEl(event.currentTarget);
  };

  const handleFilterMenuClick = event => {
    setAnchorTaskFilterEl(event.currentTarget);
  };

  const handleFilterMenuClose = () => {
    setAnchorTaskFilterEl(null);
  };

  const getColumn = useCallback(id => columns.find(column => Number(column.id) === Number(id)), [columns]);

  const onDragEnd = useCallback(
    result => {
      const { source, destination, type } = result;
      if (!destination) {
        return;
      }

      if (source.droppableId === destination.droppableId && source.index === destination.index) {
        return;
      }

      if (type === 'COLUMN') {
        if (destination.index === 0 || source.index === 0) return; // Disallow dragging over dummy section & dragging dummy section itself
        onColumnMove({
          column: columns[source.index],
          index: destination.index,
        });
        return;
      }

      const sourceColumn = getColumn(source.droppableId);
      const destinationColumn = getColumn(destination.droppableId);
      const record = getColumn(source.droppableId).records[source.index];

      onCardMove({
        column: destinationColumn,
        source: sourceColumn,
        record,
        index: destination.index,
      });
    },
    [columns, onCardMove, getColumn, onColumnMove],
  );
  const anchorRef = useRef(null);
  const [open, setOpen] = useState(false);
  const handleToggle = () => {
    setOpen(prevOpen => !prevOpen);
  };
  const handleClose = event => {
    if (anchorRef.current && anchorRef.current.contains(event.target)) {
      return;
    }
    setOpen(false);
  };

  const addNewSection = async (isLastIndex = false) => {
    let res = await addSection({
      name: translate('Untitled section'),
      sequence: sections.length + 1,
    });
    if (res) {
      onColumnAdd(
        {
          column: {
            ...res,
            records: [],
          },
        },
        isLastIndex,
      );
    }
  };

  const dummySectionProps = {
    disableDrag: true,
    disableColumnAdd: true,
    disableColumnCollapse: true,
    disableColumnDelete: true,
    customHeader: true,
    CardListStyle: {
      backgroundColor: 'white',
    },
  };

  useEffect(() => {
    const sortOptions = getSortOptions(selectedProject);
    setSortOptions(sortOptions);
  }, [selectedProject]);

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className={classes.toolbar}>
        <div className={classes.container}>
          <div style={{ display: 'flex' }}>
            <Button
              variant="outlined"
              color="primary"
              onClick={() => addNewTask()}
              className={classes.addTaskButton}
              startIcon={<AddIcon size="small" />}
              size="small"
            >
              {translate('Add Task')}
            </Button>
            <IconButton
              ref={anchorRef}
              aria-controls={open ? 'menu-list-grow' : undefined}
              aria-haspopup="true"
              onClick={handleToggle}
              className={classes.menuButton}
              size="small"
            >
              <KeyboardArrowDownIcon size="small" style={{ color: '#32325d' }} />
            </IconButton>
          </div>
          <AdvancedSearch
            fields={fields}
            domain={[]}
            handleChange={handleChangeFilter}
            onSave={handleSaveFilter}
            onDelete={handleDeleteFilter}
            filters={filters}
            value={searchFilter}
            onContentSearch={handleContentSearch}
          />
        </div>
        <div className={classes.sortingOptions}>
          <Button
            variant="outlined"
            onClick={handleFilterMenuClick}
            startIcon={<CheckCircleOutlineIcon size="small" />}
            size="small"
            className={classes.toolbarButton}
          >
            {(menuFilter && translate(menuFilter.label || menuFilter.value)) || translate('All tasks')}
          </Button>
          <Button
            variant="outlined"
            onClick={e => setAnchorFilterEl(e.currentTarget)}
            startIcon={<FilterListIcon size="small" />}
            size="small"
            className={classes.toolbarButton}
            style={{
              background: filter && (filter.label || filter.value) ? '#CAEEFF' : 'inherit',
              color: filter && (filter.label || filter.value) ? '#2CB3F6' : 'inherit',
            }}
          >
            {translate('TaskEditor.filter')} {filter && translate(filter.label || filter.value)}
            {filter && filter.value && (
              <ClearIcon size="small" onClick={() => handleSetFilter(null)} className={classes.clearIcon} />
            )}
          </Button>
          <Button
            variant="outlined"
            onClick={handleSortMenuClick}
            startIcon={<ImportExportIcon size="small" />}
            size="small"
            className={classes.toolbarButton}
          >
            {translate('TaskEditor.sort')}
            {sortColumnName && (sortColumnName.code === 'sequence' ? '' : ` : ${translate(sortColumnName.name)}`)}
          </Button>
          <Menu
            id="sort-menu"
            anchorEl={anchorTaskSortEl}
            keepMounted
            open={openSortTaskMenu}
            onClose={() => setAnchorTaskSortEl(null)}
            TransitionComponent={Fade}
            classes={{
              paper: classes.menuPaper,
            }}
            elevation={0}
            getContentAnchorEl={null}
            anchorOrigin={anchorOrigin}
            transformOrigin={transformOrigin}
          >
            {sortOptions &&
              sortOptions.map(column => (
                <MenuItem
                  key={column.code}
                  onClick={() => {
                    setAnchorTaskSortEl(null);
                    handleSetSortColumnName(column);
                  }}
                  className={classes.menuItem}
                >
                  <DoneIcon
                    className={classes.iconSelected}
                    style={{
                      visibility: column.code === (sortColumnName && sortColumnName.code) ? 'visible' : 'hidden',
                    }}
                  />
                  <Typography className={classes.menuTypography}>{column.name}</Typography>
                </MenuItem>
              ))}
          </Menu>
          <Menu
            id="filter-menu"
            anchorEl={anchorFilterEl}
            keepMounted
            open={openFilterMenu}
            onClose={() => setAnchorFilterEl(null)}
            TransitionComponent={Fade}
            classes={{
              paper: classes.menuPaper,
            }}
            elevation={0}
            getContentAnchorEl={null}
            anchorOrigin={anchorOrigin}
            transformOrigin={transformOrigin}
          >
            {FILTERS_MENU.map(column => (
              <MenuItem
                key={column.code}
                className={classes.menuItem}
                onClick={() => {
                  setAnchorFilterEl(null);
                  handleSetFilter(column);
                }}
              >
                {column.icon}
                <Typography className={classes.contextMenuItem}>{translate(column.value)}</Typography>
              </MenuItem>
            ))}
          </Menu>

          <Menu
            id="fliter-list-menu"
            anchorEl={anchorTaskFilterEl}
            keepMounted
            open={openFilterTaskMenu}
            onClose={handleFilterMenuClose}
            TransitionComponent={Fade}
            classes={{
              paper: classes.menuPaper,
            }}
            elevation={0}
            getContentAnchorEl={null}
            anchorOrigin={anchorOrigin}
            transformOrigin={transformOrigin}
          >
            {TASK_FILTERS_MENU.map(column =>
              column.items ? (
                <NestedMenuItem
                  parentMenuOpen={openFilterTaskMenu}
                  key={column.code}
                  label={column.value}
                  style={{
                    fontSize: 14,
                    marginLeft: 28,
                  }}
                  onClick={() => {
                    handleFilterMenuClose();
                    handleSetMenuFilter(column);
                  }}
                >
                  {column.items.map(item => (
                    <MenuItem
                      key={item.code}
                      className={classes.menuItem}
                      onClick={() => {
                        handleFilterMenuClose();
                        handleSetMenuFilter(item);
                      }}
                    >
                      <DoneIcon
                        className={classes.iconSelected}
                        style={{
                          visibility: item.value === (menuFilter && menuFilter.value) ? 'visible' : 'hidden',
                        }}
                      />
                      <Typography className={classes.menuTypography}>{item.value}</Typography>
                    </MenuItem>
                  ))}
                </NestedMenuItem>
              ) : (
                <MenuItem
                  key={column.code}
                  onClick={() => {
                    handleFilterMenuClose();
                    handleSetMenuFilter(column);
                  }}
                  className={classes.menuItem}
                >
                  <DoneIcon
                    className={classes.iconSelected}
                    style={{
                      visibility: column.value === (menuFilter && menuFilter.value) ? 'visible' : 'hidden',
                    }}
                  />
                  <Typography className={classes.menuTypography}>{column.value}</Typography>
                </MenuItem>
              ),
            )}
          </Menu>
        </div>
      </div>
      <Droppable droppableId="board" type="COLUMN" direction="vertical">
        {(provided, snapshot) => (
          <div
            ref={el => {
              provided.innerRef(el);
              boardRef.current = el;
            }}
            {...provided.droppableProps}
            className={classes.contentWrapper}
          >
            <div className={classes.parentHeader}>
              <IconButton
                onClick={() => forceRefresh()}
                className={classnames(classes.toolbarHeader, classes.refreshIcon)}
              >
                <Refresh />
              </IconButton>
              <div
                className={classnames(classes.toolbarHeader, classes.taskNameHeader, classes.toolbarSortIconOnHover)}
                onClick={() =>
                  handleSetSortColumnName({
                    code: 'name',
                    name: 'Alphabetical',
                    sortFunction: sortByKey,
                  })
                }
              >
                <Typography className={classes.menuTypography}>{translate('Task Name')}</Typography>
                {sortColumnName && sortColumnName.code === 'name' ? (
                  <ArrowDownwardIcon className={classes.toolbarSortIcon} />
                ) : (
                  <ArrowDownwardIcon className="toolbarSortIcon" />
                )}
              </div>
              {!propOpen && !isMobile && (
                <React.Fragment>
                  <div
                    className={classnames(classes.toolbarHeader, classes.toolbarSortIconOnHover)}
                    onClick={() => handleSetSortColumnName(SORT_COLUMNS.find(v => v.code === 'assignedTo'))}
                  >
                    <Typography className={classes.menuTypography}>{translate('Assignee')}</Typography>
                    {sortColumnName && sortColumnName.code === 'assignedTo' ? (
                      <ArrowDownwardIcon className={classes.toolbarSortIcon} />
                    ) : (
                      <ArrowDownwardIcon className="toolbarSortIcon" />
                    )}
                  </div>
                  <div
                    className={classnames(classes.toolbarHeader, classes.toolbarSortIconOnHover)}
                    onClick={() => handleSetSortColumnName(SORT_COLUMNS.find(v => v.code === 'taskDate'))}
                  >
                    <Typography className={classes.menuTypography}>{translate('TaskEditor.taskDate')}</Typography>
                    {sortColumnName && sortColumnName.code === 'taskDate' ? (
                      <ArrowDownwardIcon className={classes.toolbarSortIcon} />
                    ) : (
                      <ArrowDownwardIcon className="toolbarSortIcon" />
                    )}
                  </div>
                  <div
                    className={classnames(classes.toolbarHeader, classes.toolbarSortIconOnHover)}
                    onClick={() => handleSetSortColumnName(SORT_COLUMNS.find(v => v.code === 'taskEndDate'))}
                  >
                    <Typography className={classes.menuTypography}>{translate('Due Date')}</Typography>
                    {sortColumnName && sortColumnName.code === 'taskEndDate' ? (
                      <ArrowDownwardIcon className={classes.toolbarSortIcon} />
                    ) : (
                      <ArrowDownwardIcon className="toolbarSortIcon" />
                    )}
                  </div>
                  {isShowProgress && (
                    <div
                      className={classnames(classes.toolbarHeader, classes.toolbarSortIconOnHover)}
                      onClick={() => handleSetSortColumnName(SORT_COLUMNS.find(v => v.code === 'progressSelect'))}
                    >
                      <Typography className={classes.menuTypography}>{translate('Progress')}</Typography>
                      {sortColumnName && sortColumnName.code === 'progressSelect' ? (
                        <ArrowDownwardIcon className={classes.toolbarSortIcon} />
                      ) : (
                        <ArrowDownwardIcon className="toolbarSortIcon" />
                      )}
                    </div>
                  )}
                </React.Fragment>
              )}
            </div>
            {columns.map((column, index) => (
              <Column
                key={column.id}
                index={index}
                column={column}
                {...(column.id === -1 ? dummySectionProps : {})} // Section for tasks without section
              />
            ))}
            {!snapshot.isDraggingOver && (
              <div className={classes.btnParent}>
                <Button
                  startIcon={<AddIcon />}
                  className={classes.addSectionButton}
                  onClick={() => addNewSection(true)}
                >
                  {translate('Add Section')}
                </Button>
              </div>
            )}
            {provided.placeholder}
          </div>
        )}
      </Droppable>
      <Popper
        open={open}
        anchorEl={anchorRef.current}
        role={undefined}
        transition
        disablePortal
        style={{ zIndex: 900 }}
      >
        {({ TransitionProps }) => (
          <Grow
            {...TransitionProps}
            style={{
              transformOrigin: 'right bottom',
            }}
          >
            <Paper>
              <ClickAwayListener onClickAway={handleClose}>
                <MenuList autoFocusItem={open} id="menu-list-grow">
                  <MenuItem
                    onClick={e => {
                      addNewSection();
                      handleClose(e);
                    }}
                  >
                    {translate('Add Section')}
                  </MenuItem>
                </MenuList>
              </ClickAwayListener>
            </Paper>
          </Grow>
        )}
      </Popper>
    </DragDropContext>
  );
}

List.defaultProps = {
  columns: [],
};
