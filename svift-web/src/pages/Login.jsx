import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import { useState } from "react";
import { collection, getDocs } from "firebase/firestore";
import firebase from "../firebase_admin";
import storage from "../storage";
import Snackbar from "@mui/material/Snackbar";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [open, setOpen] = useState(false);

  const handleClose = (event, reason) => {
    if (reason === "clickaway") {
      return;
    }

    setOpen(false);
  };
  async function onLoginClicked() {
    if (username && password) {
      let hasFound = false;
      const querySnapshot = await getDocs(collection(firebase.db, "users"));
      querySnapshot.forEach((doc) => {
        let data = doc.data();
        if (data.userName === username && data.password === password) {
          console.log("Logged in!");
          storage.setLoginCredentials(username, password);
          hasFound = true;
        }
      });
      if (!hasFound) {
        setOpen(true);
      } else {
        window.location.reload();
      }
    }
  }

  return (
    <Card sx={{ maxWidth: 275, margin: 6 }}>
      <Snackbar
        open={open}
        autoHideDuration={6000}
        onClose={handleClose}
        message="Invalid credentials.Please try again."
      />
      <CardContent>
        <div>
          <h1>Login</h1>
          <form>
            <TextField
              id="outlined-basic"
              label="Username"
              variant="outlined"
              sx={{ marginBottom: 2, marginTop: 2 }}
              onChange={(e) => {
                setUsername(e.target.value);
                console.log(e.target.value);
              }}
            />
            <TextField
              id="outlined-basic"
              label="Password"
              type="password"
              variant="outlined"
              sx={{ marginBottom: 2 }}
              onChange={(e) => {
                setPassword(e.target.value);
                console.log(e.target.value);
              }}
            />
            <br />
            <Button variant="contained" onClick={onLoginClicked}>
              Login
            </Button>
          </form>
        </div>
      </CardContent>
    </Card>
  );
};

export default Login;
