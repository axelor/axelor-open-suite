import React from 'react';
import { Droppable } from 'react-beautiful-dnd';

import InnerCardList from './InnerCardList';

const SPACING = 8;
const SCROLLABLE_COLUMN_HEIGHT = 200;

export default function CardList({ disabled, scrollableColumn, gutterBottom, column }) {
  const { canDrop = true } = column;

  return (
    <Droppable droppableId={`${column.id}`} isDropDisabled={!canDrop || disabled}>
      {provided => (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: column.id === -1 ? 'white' : 'inherit',
          }}
          {...provided.droppableProps}
        >
          <div
            style={{
              marginBottom: gutterBottom ? 60 : SPACING / 2,
              marginTop: SPACING / 2,
              ...(scrollableColumn
                ? {
                    overflowX: 'hidden',
                    overflowY: 'auto',
                    padding: SPACING,
                    maxHeight: `${SCROLLABLE_COLUMN_HEIGHT}px`,
                  }
                : {}),
            }}
            ref={provided.innerRef}
          >
            <InnerCardList disabled={disabled} column={column} />
            {provided.placeholder}
          </div>
        </div>
      )}
    </Droppable>
  );
}

CardList.defaultProps = {
  disabled: false,
  scrollableColumn: false,
  gutterBottom: false,
  column: {},
  CardListStyle: {},
};
