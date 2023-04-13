import React from 'react';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
  typography: {
    fontSize: 8,
    fontWeight: 'bold',
  },
  circularProgress: {
    padding: 3,
    color: '#0275D8',
  },
}));

export default function ProgressCircular({ progressNumber = 0 }) {
  const classes = useStyles();

  return (
    <Box position="relative" display="inline-flex">
      <CircularProgress
        className={classes.circularProgress}
        size={30}
        variant="determinate"
        value={progressNumber}
      />
      <Box
        top={0}
        left={0}
        bottom={0}
        right={0}
        position="absolute"
        display="flex"
        alignItems="center"
        justifyContent="center"
      >
        <Typography
          className={classes.typography}
          variant="caption"
          component="div"
          color="textSecondary"
        >{`${Math.round(progressNumber)}%`}</Typography>
      </Box>
    </Box>
  );
}
