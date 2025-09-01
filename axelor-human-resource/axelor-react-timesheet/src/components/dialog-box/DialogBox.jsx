import {
  Button,
  Dialog,
  DialogHeader,
  DialogContent,
  DialogFooter,
  DialogTitle,
  clsx,
} from "@axelor/ui";

import styles from "./dialog-box.module.css";

export function DialogBox({
  open,
  onClose,
  message,
  title,
  onSave,
  children,
  className,
  fullscreen = true,
  addFooter = false,
}) {
  return (
    <Dialog
      open={open}
      fullscreen={fullscreen && children ? true : false}
      centered
      backdrop
      className={clsx(styles.dialogPaper, className)}
    >
      {title && (
        <DialogHeader onCloseClick={onClose}>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
      )}
      <DialogContent className={styles.content}>
        {message}
        {children && <>{children}</>}
      </DialogContent>
      {addFooter && (
        <DialogFooter>
          <Button onClick={onClose} className={styles.save} variant="secondary">
            {"Cancel"}
          </Button>
          <Button onClick={onSave} className={styles.save} variant="primary">
            {"OK"}
          </Button>
        </DialogFooter>
      )}
    </Dialog>
  );
}
