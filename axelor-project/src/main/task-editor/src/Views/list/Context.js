import React from 'react';

const TaskEditorContext = React.createContext();

export function TaskEditorProvider({ children, ...value }) {
  return <TaskEditorContext.Provider value={value}>{children}</TaskEditorContext.Provider>;
}

export function useTaskEditor() {
  return React.useContext(TaskEditorContext);
}
