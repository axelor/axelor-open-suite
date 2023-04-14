import React, { useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import classnames from 'classnames';
import Select from './Select';

const useStyles = makeStyles(theme => ({
  select: {
    marginBottom: 0,
    minWidth: 200,
    margin: 0,
    [theme.breakpoints.only('xs')]: {
      minWidth: 150,
    },
  },
  paper: {
    width: 200,
    [theme.breakpoints.only('xs')]: {
      width: 150,
    },
  },
}));

function ColumnSelect({ value, onChange, model, title, name, optionLabel, options, criteria, label, ...selectProps }) {
  const classes = useStyles();
  const [entity, setEntity] = React.useState({ ...value });

  const onSelectUpdate = (value, e) => {
    onChange(value[name], e);
    setEntity(value[name]);
  };

  useEffect(() => {
    setEntity({ ...value });
  }, [value]);

  return (
    <Select
      model={model}
      title={title}
      value={entity}
      optionLabel={optionLabel}
      name={name}
      data={entity}
      update={onSelectUpdate}
      size="small"
      className={classnames(classes.select, classes.root)}
      options={options}
      criteria={criteria}
      label={label}
      classes={{
        paper: classes.paper,
      }}
      TextFieldProps={{
        margin: 'dense',
        variant: 'outlined',
      }}
      {...selectProps}
    />
  );
}

ColumnSelect.defaultProps = {
  value: {},
  onChange: () => {},
};

export default ColumnSelect;
