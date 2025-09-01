import {
  SET_ERROR,
  SET_LOADING,
  SET_SAVED,
  UPDATE_COUNTS,
  UPDATE_TSLINES,
} from "./TimesheetActionTypes";

const Reducer = (state, action) => {
  switch (action.type) {
    case UPDATE_TSLINES:
      return { ...state, tslines: action.payload };

    case UPDATE_COUNTS:
      return { ...state, counts: action.payload };

    case SET_LOADING:
      return { ...state, isLoading: action.payload };

    case SET_SAVED:
      return { ...state, isSaved: action.payload };

    case SET_ERROR:
      return { ...state, isError: action.payload };

    default:
      return state;
  }
};

export default Reducer;
