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
import React, { useRef, useState, useEffect } from 'react';
import { Divider, IconButton, InputAdornment, Popper, Typography, Paper, List, ListItem } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import { Search, ArrowDropDown, Close } from '@material-ui/icons';

import ChipsList from './ChipsList';
import { FontAwesomeIcon } from '../Icons';
import TextField from '../Form/input/input';
import FilterEditor, { defaultState } from './filterEditor';
import { ButtonLink, BooleanCheckBox } from './common';
import { translate } from '../../utils';

const useStyles = makeStyles(theme => ({
  container: {
    width: 200, //250,
    zIndex: 900,
    '& > div:last-child': {
      zIndex: 1,
    },
  },
  textField: {
    margin: theme.spacing(1),
    flexBasis: 200,
  },
  divider: {
    width: 1,
    height: 28,
    margin: 4,
  },
  typography: {
    padding: theme.spacing(1),
    fontWeight: 600,
    flex: 1,
  },
  searchContainer: {
    display: 'flex',
    flexDirection: 'column',
    '& div': {
      display: 'flex',
      alignItems: 'center',
    },
  },
  formControl: {
    margin: theme.spacing(1),
  },
  group: {
    margin: '0px 8px',
    flexDirection: 'row',
  },
  paper: {
    minWidth: 480,
    boxShadow: '0 4px 12px rgb(0,0,0,0.4)',
  },
  link: {
    margin: theme.spacing(1),
  },
  button: {
    margin: theme.spacing(1),
  },
  list: {
    width: '100%',
    padding: 0,
  },
  listItem: {
    paddingLeft: 0,
    paddingTop: 0,
    paddingBottom: 0,
  },
  searchFilterContainer: {
    display: 'flex',
    alignItems: 'flex-start !important',
  },
  searchFilter: {
    flexDirection: 'column',
    alignItems: 'baseline !important',
    width: '100%',
  },
  filter: {
    flexDirection: 'column',
    marginLeft: 8,
    alignItems: 'baseline !important',
  },
  inputIconBtn: {
    margin: 0,
    padding: 0,
    color: '#bbb',
  },
  label: {
    fontSize: '0.875rem',
  },
  filterLabel: {
    display: 'flex',
    alignItems: 'center',
    margin: '0 8px',
  },
  input: {
    '& input': {
      paddingTop: 8,
      paddingBottom: 8,
      fontSize: 14,
    },
    borderRadius: 16,
  },
}));

function getJSONData(value) {
  try {
    return JSON.parse(value);
  } catch (error) {
    return value;
  }
}
function IconLabel({ icon, title, classes }) {
  return (
    <div className={classes.filterLabel}>
      <FontAwesomeIcon icon={icon} />
      <Typography className={classes.typography}>{title}</Typography>
    </div>
  );
}

export function FilterListItem({ filter, disabled, isChecked, onClick, onChange, classes }) {
  const { title } = filter;
  function onCheck(value) {
    onChange(value, filter);
  }
  return (
    <ListItem className={classes.listItem}>
      <BooleanCheckBox
        name={title.replace(' ', '_').toLowerCase()}
        value={isChecked}
        onChange={onCheck}
        inline
        isDisabled={disabled}
      />
      <ButtonLink
        title={title}
        onClick={() => (!disabled || isChecked) && onClick(filter, !isChecked)}
        style={disabled && !isChecked ? { color: 'rgba(0, 0, 0, 0.54)' } : {}}
      />
    </ListItem>
  );
}

export function FilterList({ active = [], data = [], disabled, onClick, onChange, classes }) {
  return (
    <div className={classes.filter}>
      <List className={classes.list}>
        {data.map(filter => (
          <FilterListItem
            key={filter.id}
            filter={filter}
            isChecked={active.includes(filter.id)}
            disabled={disabled}
            onClick={onClick}
            onChange={onChange}
            classes={classes}
          />
        ))}
      </List>
    </div>
  );
}

export function AdvanceFilterInput({ filters, value = [], onOpen, onDelete, classes, onContentSearch }) {
  const [inputText, setInputText] = useState('');
  const handleContentSearch = React.useCallback(
    e => {
      const { value } = e.target;
      onContentSearch(value || inputText);
    },
    [onContentSearch, inputText],
  );
  return value.length > 0 ? (
    <ChipsList handleClick={onOpen} value={value} onDelete={onDelete} filters={filters} />
  ) : (
    <TextField
      name="search"
      margin="none"
      placeholder={translate('Search')}
      onChange={setInputText}
      onKeyPress={e => e.key === 'Enter' && handleContentSearch(e)}
      value={inputText}
      variant="outlined"
      size="small"
      InputProps={{
        className: classes.input,
        endAdornment: (
          <InputAdornment position="end">
            <IconButton aria-label="open" onClick={onOpen} className={classes.inputIconBtn}>
              <ArrowDropDown />
            </IconButton>
            <IconButton aria-label="refresh" className={classes.inputIconBtn} onClick={handleContentSearch}>
              <Search />
            </IconButton>
          </InputAdornment>
        ),
      }}
    />
  );
}

export default function AdvanceSearchFilterWidget({
  name,
  value,
  fields,
  filters = [],
  domain = [],
  onSave,
  onDelete,
  handleChange: setValue,
  onContentSearch,
}) {
  const classes = useStyles();
  let anchorEl = useRef();
  const [open, setOpen] = React.useState(false);
  const [activeFilters, setActiveFilters] = React.useState([]);
  const [isSingleFilter, setFilterSingle] = React.useState(false);
  const [customCriteria, setCustomCriteria] = useState(defaultState);

  const currentFilter = isSingleFilter ? filters.find(x => x.id === activeFilters[0]) : null;

  function getFilter(values, filterCustom = {}, isCustom) {
    const selectedFilters = filters.filter(x => values.includes(x.id));
    const selectedDomains = domain.filter(x => values.includes(x.id));
    const graphqlFilter = {
      criteria: [],
      operator: filterCustom.operator || 'and',
    };
    if (isCustom && isSingleFilter) {
      graphqlFilter.criteria = filterCustom.criteria;
    } else {
      if (selectedFilters.length) {
        graphqlFilter.criteria.push(
          ...selectedFilters.map(filter => {
            const { criteria = [], operator } = getJSONData(filter.filterCustom);
            return { operator, criteria };
          }),
        );
      }
      graphqlFilter.criteria.push(...(filterCustom.criteria || []));
    }
    if (selectedDomains.length) {
      graphqlFilter._domains = selectedDomains.map(({ title, domain }) => ({
        title,
        domain,
      }));
    }
    return graphqlFilter;
  }

  function applyCustomFilters(filterCustom) {
    setValue({
      selected: [null],
      query: getFilter(activeFilters, filterCustom, true),
    });
  }

  function applyFilters(values, filterCustom) {
    let query = [];
    if (values.length > 0) {
      query = getFilter(values, filterCustom);
    }
    setValue({ selected: [...values], query: query });
  }

  function onFilterClick(filter, checked) {
    const isDomainFilter = !!filter.domain;
    const active = checked && filter ? [filter.id] : [];
    setFilterSingle(isDomainFilter ? false : checked);
    setActiveFilters(active);
    applyFilters(active);
  }

  function onFilterCheck(checked, { id }) {
    const ind = activeFilters.findIndex(x => x === id);
    if (ind > -1) {
      !checked && activeFilters.splice(ind, 1);
    } else {
      checked && activeFilters.push(id);
    }
    setActiveFilters([...activeFilters]);
  }

  function clearFilters() {
    setValue({});
    setActiveFilters([]);
    setFilterSingle(false);
    setCustomCriteria(defaultState);
  }

  function applyFiltersClick(filterCustom) {
    if (activeFilters.length > 0) {
      applyFilters(activeFilters.length ? [...activeFilters] : [null]);
    } else if (filterCustom && filterCustom.criteria.length) {
      applyCustomFilters({
        ...filterCustom,
        criteria: (filterCustom.criteria || []).filter(x => x.fieldName && x.operator),
      });
    }
    setOpen(false);
  }

  function onRemove(filter) {
    onDelete(filter);
    clearFilters();
  }

  async function onModify(filter) {
    const record = await onSave(filter);
    onFilterClick(record);
  }

  function onClose() {
    setOpen(false);
  }

  function onOpen() {
    setOpen(true);
  }

  useEffect(() => {
    if (currentFilter) {
      const { criteria, operator } = getJSONData(currentFilter.filterCustom);
      setCustomCriteria({
        ...currentFilter,
        operator,
        criteria: criteria.map(f => ({ ...f, operator: f.operator })),
      });
    } else {
      setCustomCriteria(defaultState);
    }
  }, [setCustomCriteria, currentFilter]);
  return (
    <div className={classes.container} ref={anchorEl}>
      <AdvanceFilterInput
        value={value && value.selected}
        filters={filters}
        onOpen={onOpen}
        onDelete={clearFilters}
        classes={classes}
        onContentSearch={onContentSearch}
      />
      <Popper open={open} anchorEl={anchorEl.current} placement={'bottom-start'} disablePortal={true}>
        <Paper className={classes.paper}>
          <div className={classes.searchContainer}>
            <div>
              <Typography className={classes.typography}>{translate('Advanced Search')}</Typography>
              <IconButton aria-label="close" onClick={onClose}>
                <Close />
              </IconButton>
            </div>
            <Divider />
            <div className={classes.searchFilterContainer}>
              {domain.length > 0 && (
                <div className={classes.searchFilter}>
                  <IconLabel icon="filter" title="Filters" classes={classes} />
                  <FilterList
                    data={domain}
                    disabled={isSingleFilter}
                    active={activeFilters}
                    onClick={onFilterClick}
                    onChange={onFilterCheck}
                    classes={classes}
                  />
                </div>
              )}
              {filters.length > 0 && (
                <div className={classes.searchFilter}>
                  <IconLabel icon="filter" title="My Filter" classes={classes} />
                  <FilterList
                    data={filters}
                    disabled={isSingleFilter}
                    active={activeFilters}
                    onClick={onFilterClick}
                    onChange={onFilterCheck}
                    classes={classes}
                  />
                </div>
              )}
            </div>
            {filters.length >= 1 || domain.length ? <Divider style={{ marginTop: 8 }} /> : ''}
            <FilterEditor
              fields={fields}
              clearFilters={clearFilters}
              applyFiltersClick={applyFiltersClick}
              filterView={name}
              onSave={onModify}
              onDelete={onRemove}
              customCriteria={customCriteria}
              setCustomCriteria={setCustomCriteria}
            />
          </div>
        </Paper>
      </Popper>
    </div>
  );
}
