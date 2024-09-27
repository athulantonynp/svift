const storage = {
  setLoginCredentials: (username, password) => {
    localStorage.setItem("uname", username);
    localStorage.setItem("pass", password);
  },
  getLoginCredentials: () => {
    let creds = {
      username: localStorage.getItem("uname"),
      password: localStorage.getItem("pass"),
    };
    if (creds.username === null || creds.password === null) {
      return null;
    }
    return creds;
  },
};

export default storage;
