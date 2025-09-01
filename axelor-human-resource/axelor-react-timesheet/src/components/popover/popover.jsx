import { Box, ClickAwayListener, Popper, usePopperTrigger } from "@axelor/ui";
import styles from "./popover.module.css";

export const Popover = ({ title, children, placement = "top" }) => {
  const { open, targetEl, setTargetEl, setContentEl, onClickAway } =
    usePopperTrigger({
      trigger: "hover",
      interactive: true,
      delay: {
        open: 0,
        close: 0,
      },
    });

  if (!title?.trim()) {
    return <>{children}</>;
  }

  return (
    <>
      <span ref={setTargetEl} style={{ display: "inline-block" }}>
        {children}
      </span>
      <Popper
        className={styles.tooltip}
        open={open}
        target={targetEl}
        offset={[0, 15]}
        placement={placement}
        arrow
        shadow
        rounded
      >
        <ClickAwayListener onClickAway={onClickAway}>
          <Box ref={setContentEl} className={styles.container}>
            {title}
          </Box>
        </ClickAwayListener>
      </Popper>
    </>
  );
};

export default Popover;
