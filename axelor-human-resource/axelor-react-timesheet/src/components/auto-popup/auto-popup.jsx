import Popup from "reactjs-popup";
import "reactjs-popup/dist/index.css";

export const AutoPopup = ({
  trigger,
  children,
  position = "bottom center",
  closeOnDocumentClick = true,
  arrow = false,
  keepTooltipInside = true,
  repositionOnResize = true,
  contentStyle = {},
  onClose,
  ...props
}) => {
  const defaultContentStyle = {
    minWidth: "20rem",
    maxWidth: "25rem",
    zIndex: 1000,
    marginTop: "10px",
    ...contentStyle,
  };

  return (
    <Popup
      trigger={trigger}
      position={position}
      closeOnDocumentClick={closeOnDocumentClick}
      arrow={arrow}
      keepTooltipInside={keepTooltipInside}
      repositionOnResize={repositionOnResize}
      contentStyle={defaultContentStyle}
      onClose={onClose}
      {...props}
    >
      {children}
    </Popup>
  );
};
