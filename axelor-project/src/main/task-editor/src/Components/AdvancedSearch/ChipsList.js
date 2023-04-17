import React from 'react';
import { List, Chip, IconButton } from '@material-ui/core';
import { Search, ArrowDropDown } from '@material-ui/icons';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
  root: {
    display: 'flex',
    borderBottom: 'solid 1px rgba(0, 0, 0, 0.54)',
    marginBottom: '7px',
  },
  chip: {
    margin: '4px',
    height: 30,
  },
  list: {
    display: 'flex',
    padding: '0 !important',
  },
  listItem: {
    padding: '0 !important',
  },
  divider: {
    width: 1,
    height: 28,
    margin: 4,
  },
  buttonContainer: {
    display: 'flex',
    width: '100%',
    justifyContent: 'flex-end',
    alignItems: 'center',
    position: 'relative',
    bottom: -3,
  },
  inputIconBtn: {
    margin: 0,
    padding: 0,
    color: '#bbb',
  },
}));

export default function Input({ value, onDelete, filters, handleClick, ...rest }) {
  const classes = useStyles();
  let text = '';
  if (value.length > 1) {
    text = `Filters ${value.length}`;
  } else if (value.length) {
    const findFilter = filters.find(x => x.id === value[0]);
    if (findFilter) {
      text = findFilter.title;
    } else {
      text = 'Custom';
    }
  }
  return (
    <div className={classes.root}>
      <List className={classes.list}>
        <Chip label={text} onDelete={() => onDelete(value)} className={classes.chip} />
      </List>
      <div className={classes.buttonContainer}>
        <IconButton aria-label="open" onClick={handleClick} className={classes.inputIconBtn}>
          <ArrowDropDown />
        </IconButton>
        <IconButton aria-label="refresh" className={classes.inputIconBtn}>
          <Search />
        </IconButton>
      </div>
    </div>
  );
}
