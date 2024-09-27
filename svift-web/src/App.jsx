import "./App.css";
import Box from "@mui/material/Box";
import MainAppBar from "./components/Toolbar";
import { useEffect, useState } from "react";
import storage from "./storage";
import Login from "./pages/Login";
import AddMusic from "./pages/AddMusic";

function App() {
  const [isLogin, setLogin] = useState(true);
  useEffect(() => {
    const user = storage.getLoginCredentials();
    setLogin(user === null);
  }, []);
  return (
    <Box sx={{ flexGrow: 1 }}>
      <MainAppBar />
      {isLogin ? (
        <Box>
          <Login />
        </Box>
      ) : (
        <Box>
          <AddMusic />
        </Box>
      )}
    </Box>
  );
}

export default App;
