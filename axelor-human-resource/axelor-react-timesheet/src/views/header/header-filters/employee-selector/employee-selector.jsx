import { useCallback } from "react";
import { Select } from "../../../../components/selection";
import { fetchEmployees } from "../../../../services/api";
import { useUser, useEmployee } from "../../../../hooks/store";
import { MaterialIcon } from "@axelor/ui/icons/material-icon";
import { toast } from "sonner";

import { openTabView } from "../../../../services/utils";
import styles from "./employee-selector.module.css";

export function UserSelector() {
  const user = useUser();
  const [employee, setEmployee] = useEmployee();

  const fetchTimesheetEmployees = useCallback(async () => {
    const res = await fetchEmployees({
      fields: [
        "name",
        "user",
        "publicHolidayEventsPlanning",
        "timesheetProjectSet",
        "timesheetProjectTaskSet",
        "managerUser",
      ],
      data: {
        criteria: [
          {
            fieldName: "user.id",
            operator: "=",
            value: user?.id,
          },
          {
            fieldName: "managerUser.id",
            operator: "=",
            value: user?.id,
          },
        ],
        operator: "OR",
      },
    });
    return res.data;
  }, [user?.id]);

  const openEmployeeView = useCallback(() => {
    if (!employee?.id) {
      return;
    }
    openTabView({
      name: "employee-form",
      title: "Employee",
      model: "com.axelor.apps.hr.db.Employee",
      viewType: "form",
      views: [
        {
          type: "form",
          name: "employee-form",
        },
      ],
      params: {
        popup: "reload",
        "_popup-record": { id: employee.id },
        __onPopupReload: async () => {
          const res = await fetchEmployees({
            fields: [
              "name",
              "user",
              "publicHolidayEventsPlanning",
              "timesheetProjectSet",
              "timesheetProjectTaskSet",
            ],
            data: {
              criteria: [
                {
                  fieldName: "user.id",
                  operator: "=",
                  value: user?.id,
                },
              ],
              operator: "OR",
            },
          });
          if (res?.data?.[0]) {
            setEmployee(res.data[0], true);
            toast.success("Employee view reloaded successfully.");
          }
        },
      },
    });
  }, [employee?.id, user?.id, setEmployee]);

  return (
    <div className={styles.selectionWrapper}>
      <Select
        name="user"
        placeholder="Select User"
        fetchOptions={fetchTimesheetEmployees}
        optionLabel={(item) => item.name}
        optionValue={(item) => item.id}
        optionKey={(item) => item.id}
        value={employee}
        onChange={setEmployee}
        clearIcon={false}
        inputEndAdornment={
          <MaterialIcon
            onClick={openEmployeeView}
            icon="open_in_new"
            className={styles.icon}
          />
        }
      />
    </div>
  );
}
