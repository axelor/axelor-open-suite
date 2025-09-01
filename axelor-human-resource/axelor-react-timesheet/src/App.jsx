import { Box } from "@axelor/ui";
import { Header, Content, Footer } from "./views";
import { TimesheetContextProvider } from "./context/TimesheetProvider";

import { Toaster } from "sonner";
import "./App.css";
import { useStore } from "./hooks/store";

function App() {
  const { state } = useStore();
  const { timesheet } = state;

  return (
    <TimesheetContextProvider>
      <Toaster richColors />
      <Box display="flex" flexDirection="column" gap={10} vh={100}>
        <Header />
        {timesheet && (
          <>
            <Content />
            <Footer />
          </>
        )}
      </Box>
    </TimesheetContextProvider>
  );
}

export default App;
