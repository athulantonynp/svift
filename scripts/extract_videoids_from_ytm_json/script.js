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

// Step 1: Load the JSON file into memory
const rawData = fs.readFileSync("data.json");

// Step 2: Parse the JSON data
const jsonData = JSON.parse(rawData);

const array =
  jsonData.contents.twoColumnBrowseResultsRenderer.secondaryContents
    .sectionListRenderer.contents[0].musicPlaylistShelfRenderer.contents;
// Step 3: Extract the values of the `videoId` field
const videoIds = array.map((item) => {
  return item.musicResponsiveListItemRenderer.playlistItemData.videoId;
});

// Step 4: Remove duplicate `videoId` values
const uniqueVideoIds = [...new Set(videoIds)];

// Step 5: Reverse the unique `videoId` values
uniqueVideoIds.reverse();

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function setData() {
  const out = [];
  for (const id of uniqueVideoIds) {
    await sleep(2000); // Adding a delay of 2 seconds
    out.push({
      videoId: id,
      time: Date.now(),
    });
  }
  await db.collection("songs").doc("athulantony").set({
    ym: out,
  });
}

setData();
