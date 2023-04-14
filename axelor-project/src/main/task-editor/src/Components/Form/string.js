import React from 'react';
import Typography from '@material-ui/core/Typography';
import { Chip, Link } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
  container: {
    display: 'flex',
    justifyContent: 'center',
    flexDirection: 'column',
  },
  subtitle1: {
    fontSize: 12,
  },
  chip: {
    margin: theme.spacing(0.5, 0.25),
  },
  link: {
    cursor: 'pointer',
  },
}));

function RenderStringWidget({ title, value }) {
  const classes = useStyles();
  return (
    <div className={classes.container}>
      <Typography variant="subtitle1" className={classes.subtitle1}>
        {title}
      </Typography>
      <Typography>{value}</Typography>
    </div>
  );
}

function RenderRelationalWidget({ title, value, optionLabelKey, viewRecord }) {
  const classes = useStyles();
  function renderLink(value) {
    const link = value && value[optionLabelKey];
    return (
      link && (
        <Link className={classes.link} onClick={() => viewRecord && viewRecord(value)}>
          {link}
        </Link>
      )
    );
  }
  if (Array.isArray(value)) {
    return (
      <div>
        <Typography variant="subtitle1" className={classes.subtitle1}>
          {title}
        </Typography>
        {value.map((v, index) => (
          <Chip key={index} label={renderLink(v)} className={classes.chip} />
        ))}
      </div>
    );
  }
  return (
    <div>
      <Typography variant="subtitle1" className={classes.subtitle1}>
        {title}
      </Typography>
      {value && renderLink(value)}
    </div>
  );
}

export default function StringWidget(item) {
  const { title, value, customRender } = item;
  if (typeof value === 'object' && !customRender) {
    return <RenderRelationalWidget {...item} />;
  }
  return <RenderStringWidget title={title} value={value} />;
}
