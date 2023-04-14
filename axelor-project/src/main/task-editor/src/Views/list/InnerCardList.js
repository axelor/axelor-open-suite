import React from 'react';
import { Draggable } from 'react-beautiful-dnd';

import CardItem from './CardItem';
import { useTaskEditor } from './Context';

const InnerCardList = React.memo(function InnerCardList({ disabled, column }) {
  const { onCardEdit, onCardDelete, tasksToBeDeleted } = useTaskEditor();
  const { canMove = true, canDelete = true, canEdit = true } = column;
  const filteredRecords = column.records?.filter(record => !tasksToBeDeleted.some(v => v.id === record.id));
  return filteredRecords?.length ? (
    filteredRecords.map((record, index) => (
      <Draggable
        key={`${record.id}`}
        draggableId={`task_${record.id}`}
        index={index}
        isDragDisabled={!canMove || disabled}
      >
        {provided => (
          <CardItem
            key={record.id}
            ref={provided.innerRef}
            {...provided.draggableProps}
            dragHandleProps={provided.dragHandleProps}
            canDelete={canDelete}
            canEdit={canEdit}
            record={record}
            disabled={disabled}
            style={{
              ...provided.draggableProps.style,
              width: '100%',
            }}
            onDelete={() => onCardDelete(record.id)}
            onEdit={record => onCardEdit(record)}
          />
        )}
      </Draggable>
    ))
  ) : (
    <React.Fragment />
  );
});

InnerCardList.defaultProps = {
  onCardDelete: () => {},
  disabled: false,
  onCardEdit: () => {},
  column: {},
};

export default InnerCardList;
