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
import {
  Divider,
  IconButton,
  List,
  ListItem,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  makeStyles,
} from '@material-ui/core';
import { Close } from '@material-ui/icons';
import moment from 'moment';
import classnames from 'classnames';

import produce from 'immer';

import { ButtonLink, BooleanRadio, BooleanCheckBox, SimpleButton } from './common';
import Select from './select';
import TextField from '../Form/input/input';
import Selection from '../Form/selection';
import DateTimePicker from '../Form/input/datetime-picker';
import NumberField from '../Form/input/number';
import { operators_by_type, operators } from './constant';
import { searchRecordsByField } from './utils';

const useStyles = makeStyles(theme => ({
  group: {
    margin: '0px 8px',
    flexDirection: 'row',
  },
  button: {
    margin: theme.spacing(1),
  },
  label: {
    fontSize: '0.875rem',
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
  link: {
    margin: theme.spacing(1),
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
  criteriaValue: {
    width: '250px !important',
    alignItems: 'normal',
  },
}));

export const defaultState = {
  title: '',
  operator: 'and',
  isArchived: false,
  isShare: false,
  criteria: [{}],
  selected: false,
};

const addOperatorByType = (keys, value) => {
  keys.map(key => (operators_by_type[key] = value));
};

addOperatorByType(['long', 'decimal', 'date', 'time', 'datetime'], operators_by_type.integer);
addOperatorByType(['one_to_many'], operators_by_type.text);
addOperatorByType(['one_to_one', 'many_to_one', 'many_to_many'], ['like', 'notLike', 'in', 'notIn', 'isNull']);

function renderSimpleWidget(Component, operator, { onChange, value, value2, classes, style, ...rest }) {
  if (['=', '!=', '>', '>=', '<', '<=', 'like', 'notLike'].includes(operator)) {
    return (
      <Component
        name="value"
        className={classes.criteriaValue}
        onChange={value => onChange({ name: 'value', value: value })}
        value={value}
        {...rest}
      />
    );
  } else if (['between', 'notBetween'].includes(operator)) {
    return (
      <React.Fragment>
        <Component
          name="value"
          style={{ marginRight: 8, ...style }}
          className={classes.criteriaValue}
          onChange={value => onChange({ name: 'value', value: `${value}` })}
          value={value}
          {...rest}
        />

        <Component
          name="value2"
          className={classes.criteriaValue}
          onChange={value => onChange({ name: 'value2', value: `${value}` })}
          value={value2}
          {...rest}
        />
      </React.Fragment>
    );
  }
}

function renderRelationalWidget(operator, { onChange, ...rest }) {
  if (['like', 'notLike'].includes(operator)) {
    return (
      <TextField
        name="value"
        onChange={value => onChange({ name: 'value', value: value })}
        margin="none"
        style={{ width: 250 }}
        {...rest}
      />
    );
  } else if (['in', 'notIn'].includes(operator)) {
    const { field, value } = rest;
    const { targetName } = field;

    const fetchData = async ({ search }) => {
      const { data } = await searchRecordsByField(field, search);
      return data;
    };

    return (
      <Selection
        placeholder=""
        fetchAPI={fetchData}
        isSearchable={true}
        optionLabelKey={targetName}
        optionValueKey="id"
        name="value"
        value={value}
        isMulti
        formControlStyle={{ marginBottom: '5px', width: 250 }}
        clearIndicatorStyle={{ paddingLeft: '0 !important' }}
        closeMenuOnScroll={false}
        onChange={value =>
          onChange({
            name: 'value',
            value: Array.isArray(value)
              ? value.map(x => ({ id: x.id, [targetName]: x[targetName] }))
              : value && { id: value.id, [targetName]: value[targetName] },
          })
        }
      />
    );
  }
}

function renderWidget({ type, operator, onChange, value, classes, ...rest }) {
  const props = {
    value: value.value,
    value2: value.value2,
    onChange,
    ...rest,
  };
  switch (type) {
    case 'one_to_one':
    case 'many_to_one':
    case 'many_to_many':
    case 'one_to_many':
      return renderRelationalWidget(operator, { ...props });
    case 'date':
    case 'time':
    case 'dateTime':
      return renderSimpleWidget(DateTimePicker, operator, {
        type: type,
        ...props,
        format: 'DD/MM/YYYY',
        margin: 'none',
        classes,
      });
    case 'integer':
    case 'long':
    case 'decimal':
      return renderSimpleWidget(NumberField, operator, {
        type: type,
        ...props,
        margin: 'none',
        classes,
      });
    case 'enum':
      const options = rest.field.selectionList.map(({ title, value, data }) => ({
        name: (data && data.value) || value,
        title: title,
      }));
      return renderSimpleWidget(Select, operator, {
        options,
        classes,
        ...props,
      });
    default:
      return renderSimpleWidget(TextField, operator, {
        classes,
        ...props,
        margin: 'none',
      });
  }
}

export function AddCriteria({ className, onRemove, onChange, value, fields, classes }) {
  let field = fields.find(item => item.name === value.fieldName);
  let type = ((field && field.type) || '').toLowerCase();
  field && field.selectionList && (type = 'enum');

  let operatorsOptions = operators.filter(item => (operators_by_type[type] || []).includes(item.name));

  function SelectWrapper({ name, options, ...rest }) {
    return (
      <Select
        name={name}
        onChange={value => onChange({ name, value })}
        value={value[name]}
        options={options}
        {...rest}
      />
    );
  }

  return (
    <div>
      <IconButton aria-label="close" className={className} onClick={onRemove}>
        <Close />
      </IconButton>
      <SelectWrapper name="fieldName" options={fields} className="fieldName" />
      <SelectWrapper name="operator" options={operatorsOptions} className="operator" />
      {value.operator &&
        renderWidget({
          type,
          operator: value['operator'],
          value,
          onChange,
          classes,
          ...{ field },
        })}
    </div>
  );
}

export default function FilterEditor({
  customCriteria,
  setCustomCriteria,
  fields,
  clearFilters,
  applyFiltersClick,
  filterView,
  onSave,
  onDelete,
}) {
  const classes = useStyles();
  const [openDialog, setOpenDialog] = React.useState(false);
  const { id, operator, isArchived, criteria, title, isShare } = customCriteria;

  const onAddCriteria = () => {
    setCustomCriteria(
      produce(draft => {
        draft.criteria[draft.criteria.length - 1].operator && draft.criteria.push({});
      }),
    );
  };

  function onChange({ target: { name, value } }) {
    setCustomCriteria({
      ...customCriteria,
      [name]: value,
    });
  }

  function onRemoveCriteria(index) {
    setCustomCriteria(
      produce(draft => {
        if (draft.criteria.length === 1) {
          draft.criteria = [{}];
        } else {
          draft.criteria.splice(index, 1);
        }
      }),
    );
  }

  function onCriteriaChange(name, value, index) {
    if (value instanceof moment) {
      value = moment(value).format('YYYY-MM-DD');
    }
    setCustomCriteria(
      produce(draft => {
        draft.criteria[index][name] = value;
        if (name === 'fieldName') {
          draft.criteria[index].operator = '';
          draft.criteria[index].value = getDefaultValue(value);
        }
      }),
    );
  }

  function getDefaultValue(fieldName) {
    const field = fields.find(f => f.name === fieldName);
    const type = field.type.toLowerCase();
    if (['one_to_one', 'many_to_one', 'many_to_many', 'one_to_many'].includes(type)) {
      return null;
    } else if (['integer', 'long', 'decimal'].includes(type)) {
      return 0;
    } else if (['date', 'time', 'dateTime'].includes(type)) {
      return null;
    } else {
      return '';
    }
  }

  function onClear() {
    clearFilters();
    setCustomCriteria(defaultState);
  }

  function onSaveFilter() {
    const { id, version, title, isShare, operator, criteria } = customCriteria;
    let savedFilter = {
      shared: isShare || false,
      title: title,
      filterCustom: JSON.stringify({ operator, criteria }),
      filterView: filterView,
    };
    if (id) {
      savedFilter.id = id;
      savedFilter.version = version;
    } else {
      savedFilter.name = title.replace(' ', '_').toLowerCase();
    }
    onSave(savedFilter);
  }

  function onDeleteFilter() {
    onDelete(customCriteria);
    setOpenDialog(false);
  }

  return (
    <React.Fragment>
      <div>
        <BooleanRadio
          name="operator"
          onChange={e => onChange(e)}
          value={operator}
          classes={classes}
          className={classes.group}
          data={[
            { label: 'and', value: 'and' },
            { label: 'or', value: 'or' },
          ]}
        />
        <BooleanCheckBox
          name="isArchived"
          title="Show Archived"
          value={isArchived}
          onChange={({ name, checked }) => onChange({ target: { value: checked, name: name } })}
          classes={classes}
          className="archived"
        />
      </div>
      <div>
        <List className={classes.list}>
          {criteria.map((item, index) => (
            <ListItem className={classes.listItem} key={index}>
              <AddCriteria
                className={classes.buttonLink}
                id={index}
                value={item}
                fields={fields}
                onRemove={() => onRemoveCriteria(index)}
                onChange={({ name, value }) => onCriteriaChange(name, value, index)}
                classes={classes}
              />
            </ListItem>
          ))}
        </List>
      </div>
      <div className={classes.buttonLink}>
        <ButtonLink title="Add filter" className={classnames(classes.link, 'add-filter')} onClick={onAddCriteria} />
        <Divider className={classes.divider} />
        <ButtonLink title="Clear" className={classnames(classes.link, 'clear')} onClick={onClear} />
        <Divider className={classes.divider} />
        <ButtonLink title="Export" className={classes.link} />
        <Divider className={classes.divider} />
        <ButtonLink title="Export full" className={classes.link} />
        <Divider className={classes.divider} />
        <ButtonLink
          title="Apply"
          className={classnames(classes.link, 'apply-filter')}
          onClick={() => applyFiltersClick(customCriteria)}
        />
      </div>
      <Divider style={{ marginTop: 8 }} />
      <div>
        <TextField
          name="title"
          value={title}
          onChange={value => onChange({ target: { value: value, name: 'title' } })}
          className={classnames(classes.textField, 'title')}
        />
        <BooleanCheckBox
          title="share"
          name="isShare"
          value={isShare}
          onChange={({ name, checked }) => onChange({ target: { value: checked, name: name } })}
          className="share"
        />
      </div>
      <div>
        <SimpleButton
          classes={classes}
          onClick={onSaveFilter}
          title="Save"
          hide={title === '' || title === null || id !== undefined}
          className="save"
        />
        <SimpleButton
          classes={classes}
          onClick={onSaveFilter}
          title="Update"
          hide={id === '' || id === undefined || id === null}
        />
        <SimpleButton
          classes={classes}
          onClick={() => setOpenDialog(true)}
          title="Delete"
          hide={id === '' || id === undefined || id === null}
          className="delete"
        />
        <Dialog
          open={openDialog}
          onClose={() => setOpenDialog(false)}
          aria-labelledby="alert-dialog-title"
          aria-describedby="alert-dialog-description"
        >
          <DialogTitle id="alert-dialog-title">{'Question'}</DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">Would you like to remove the filter?</DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)} className="cancel">
              Cancel
            </Button>
            <Button onClick={onDeleteFilter} color="primary" autoFocus className="confirm-delete">
              ok
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    </React.Fragment>
  );
}
