const Reducer = (state, action) => {
  switch (action.type) {
    case "UPDATE_TSLINES":
      return { ...state, tslines: action.payload };

    case "UPDATE_COUNTS":
      return { ...state, counts: action.payload };

    default:
      return state;
  }
};
export default Reducer;
