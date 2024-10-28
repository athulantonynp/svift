import Card from "@mui/material/Card";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import CardContent from "@mui/material/CardContent";
import { useState } from "react";
import { updateDoc, arrayUnion, doc } from "firebase/firestore";
import storage from "../storage";
import firebase from "../firebase_admin";

const AddMusic = () => {
  const [musicId, setMusicId] = useState("");
  const [message, setMessage] = useState("");
  const [url, setUrl] = useState("");

  async function onSaveClicked() {
    const userName = storage.getLoginCredentials().username;

    if (musicId) {
      try {
        const docRef = doc(firebase.db, "songs", userName);
        const newVideo = {
          time: Date.now(),
          videoId: url ? url : musicId,
        };
        console.log(newVideo);
        await updateDoc(docRef, {
          ym: arrayUnion(newVideo), // Use arrayUnion to add the object to the array
        });

        console.log("Document written with ID: ", docRef.id);
        setMusicId("");
        setMessage("Music added successfully!");
        setUrl("");
      } catch (e) {
        console.error("Error adding document: ", e);
        setMessage("Error adding music");
      }
    }
  }

  function getQueryParam(url, param) {
    try {
      const urlObj = new URL(url);
      return urlObj.searchParams.get(param);
    } catch (e) {
      console.error(e);
    }
    return url;
  }

  return (
    <Card sx={{ maxWidth: 400, margin: 6 }}>
      <CardContent>
        <div>
          <h1>Add Music</h1>
          <form>
            <TextField
              id="outlined-basic"
              label="Youtube Music id"
              variant="outlined"
              fullWidth
              sx={{ marginBottom: 2, marginTop: 2 }}
              onChange={(e) => {
                setMusicId(e.target.value);
                setUrl(getQueryParam(e.target.value, "v"));
              }}
              onPaste={(e) => {
                setTimeout(() => {
                  setMusicId(e.target.value);
                  setUrl(getQueryParam(e.target.value, "v"));
                }, 1000);
              }}
              onInput={(e) => {
                setMusicId(e.target.value);
                setUrl(getQueryParam(e.target.value, "v"));
              }}
              value={musicId}
            />
            <div>{url}</div>
            <Button variant="contained" onClick={onSaveClicked}>
              Save
            </Button>
            <br />
            <div>{message}</div>
          </form>
        </div>
      </CardContent>
    </Card>
  );
};

export default AddMusic;
