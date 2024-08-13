const fs = require("fs");
var admin = require("firebase-admin");
const {
  getFirestore,
  Timestamp,
  FieldValue,
  Filter,
} = require("firebase-admin/firestore");

var serviceAccount = require("./service_account.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = getFirestore();

const fileNames = ["1.json", "2.json", "3.json", "4.json", "5.json"];
const userName = "antony";

const out = [];
for (const fileName of fileNames) {
  // Step 1: Load the JSON file into memory
  const rawData = fs.readFileSync(`./data/${fileName}`);
  console.log(fileName);

  // Step 2: Parse the JSON data
  const jsonData = JSON.parse(rawData);

  if (jsonData.contents !== undefined) {
    const array =
      jsonData.contents.twoColumnBrowseResultsRenderer.secondaryContents
        .sectionListRenderer.contents[0].musicPlaylistShelfRenderer.contents;
    // Step 3: Extract the values of the `videoId` field
    const videoIds = array.map((item) => {
      return item.musicResponsiveListItemRenderer.playlistItemData.videoId;
    });
    out.push(...videoIds);
  } else {
    const array =
      jsonData.continuationContents.musicPlaylistShelfContinuation.contents;
    const videoIds = array.map((item) => {
      return item.musicResponsiveListItemRenderer.playlistItemData.videoId;
    });
    out.push(...videoIds);
  }
}
// Step 4: Remove duplicate `videoId` values
const uniqueVideoIds = [...new Set(out)];

// Step 5: Reverse the unique `videoId` values
uniqueVideoIds.reverse();

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function setData() {
  const out = [];
  for (const id of uniqueVideoIds) {
    await sleep(2); // Adding a delay of 2 seconds
    out.push({
      videoId: id,
      time: Date.now(),
    });
  }
  await db.collection("songs").doc(userName).set({
    userName: userName,
    ym: out,
  });
}

setData();
