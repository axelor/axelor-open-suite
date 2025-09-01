import { useEffect, useState, useMemo, createContext, useContext } from "react";
import { fetchEmployees, fetchInfo, getDefaults } from "../services/api";
import { EMPLOYEE_FIELDS } from "../constant";
const StoreContext = createContext();

function StoreProvider({ children }) {
  const [state, setState] = useState({
    info: null,
    timesheet: null,
    employee: null,
    records: null,
    page: null,
    projects: [],
    projectTasks: [],
  });

  const setTimesheet = (timesheet) => {
    setState((prev) => ({ ...prev, timesheet }));
  };

  const setEmployee = async (employee, isDefaultTimesheet = true) => {
    if (!isDefaultTimesheet) {
      setState((prev) => ({
        ...prev,
        employee,
      }));
      return;
    }
    const { records, timesheet, projectTasks } = await getDefaults(employee);
    setState((prev) => ({
      ...prev,
      employee,
      records:
        !records?.data?.find((r) => r.id === timesheet.id) && timesheet?.id
          ? [...(records?.data || []), timesheet]
          : records?.data,
      page: {
        offset: records?.offset,
        total: records?.total,
        limit: 10,
      },
      timesheet,
      projects: employee?.timesheetProjectSet || [],
      projectTasks: projectTasks || [],
    }));
  };

  const value = useMemo(
    () => ({
      state,
      setState,
      setTimesheet,
      setEmployee,
    }),
    [state]
  );

  useEffect(() => {
    async function getInitials() {
      try {
        const info = await fetchInfo();
        const employee = await fetchEmployees({
          fields: EMPLOYEE_FIELDS,
          data: {
            criteria: [
              {
                fieldName: "user.id",
                operator: "=",
                value: info.user.id,
              },
            ],
          },
        });
        const loggedInEmployee = employee?.data?.[0];
        const { records, timesheet, projectTasks, counts } = await getDefaults(
          loggedInEmployee
        );
        setState((state) => ({
          ...state,
          info,
          employee: loggedInEmployee,
          records: !records?.data?.find((r) => r.id === timesheet?.id)
            ? [...(records?.data || []), timesheet]
            : records?.data,
          page: {
            offset: records?.offset,
            total: records?.total,
            limit: 10,
          },
          timesheet,
          projects: employee?.data?.[0]?.timesheetProjectSet || [],
          projectTasks: projectTasks || [],
          counts,
        }));
      } catch (error) {
        console.error("Error fetching initial data:", error);
        setState((state) => ({
          ...state,
          loadingCount: 0,
        }));
      }
    }

    getInitials();
  }, []);

  return (
    <StoreContext.Provider value={value}>{children}</StoreContext.Provider>
  );
}

export function useStore() {
  return useContext(StoreContext);
}

export function useUser() {
  const store = useStore();
  return store.state.info?.user || null;
}

export function useTimesheet() {
  const store = useStore();
  return [store.state.timesheet, store.setTimesheet];
}

export function useEmployee() {
  const store = useStore();
  return [store.state.employee, store.setEmployee];
}

export default StoreProvider;
