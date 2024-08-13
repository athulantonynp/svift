package sviftytd

import (
	"encoding/json"
	"fmt"
	"io"
	"os"
	"strings"
	"sync"

	"github.com/kkdai/youtube/v2"
)
var client youtube.Client = youtube.Client{}


func DownloadAudios(videoIDsStr string,downloadPath string,callback DownloadCallback) string {
	videoIDs := strings.Split(videoIDsStr, ",")
    var wg sync.WaitGroup
    maxGoroutines := 10
    guard := make(chan struct{}, maxGoroutines)
    songs := make([]Song, 0)
    var mu sync.Mutex

    for _, videoID := range videoIDs {
		fmt.Println("Downloading video", videoID)
        wg.Add(1)
        guard <- struct{}{} // Block if there are already maxGoroutines running

        go func(videoID string) {
            defer wg.Done()
            defer func() { <-guard }() // Release the guard

            response := downloadMp3(videoID, downloadPath)
            if response.Error != nil {
				jsonData,_:= json.Marshal(response.Error)
                fmt.Printf("Error downloading video %s: %s\n", videoID, jsonData)
            } else {
                mu.Lock()
                songs = append(songs, *response.Song)
				callback.OnMessage(fmt.Sprintf("Downloaded %d of %d", len(songs), len(videoIDs)))
                mu.Unlock()
            }
        }(videoID)
    }

    wg.Wait()
    data,_:= json.Marshal(songs)
	return string(data)
}

func downloadMp3(videoID string,path string) SongDownloadResponse {
	video, err := client.GetVideo(videoID)

	if err != nil {
		return SongDownloadResponse{nil, &err}
	}

	// Get the format with the highest audio bitrate
	format := getHighestBitrateFormat(video.Formats.WithAudioChannels())
	if format == nil {
		return SongDownloadResponse{nil, &err}
	}

	// Get the stream for the selected format
	stream, _, err := client.GetStream(video, format)
	if err != nil {
		return SongDownloadResponse{nil, &err}
	}
	defer stream.Close()

	fileName := videoID + ".webm"
	// Create a temporary file to save the audio stream
	tempFileName := path+"/"+fileName
	tempFile, err := os.Create(tempFileName)
	if err != nil {
		return SongDownloadResponse{nil, &err}
	}

	_, err = io.Copy(tempFile, stream)
	if err != nil {
		return SongDownloadResponse{nil, &err}
	}

	if err != nil {
		return SongDownloadResponse{nil, &err}
	}

	thumbnailURL := video.Thumbnails[0].URL
	for _, thumb := range video.Thumbnails {
		if thumb.Width > video.Thumbnails[0].Width {
			thumbnailURL = thumb.URL
		}
	}

	song := Song{
		ID: videoID,
		Provider: "youtube",
		ThumbnailURL: thumbnailURL,
		Title: video.Title,
		Author: video.Author,
		FileName: fileName,
		FilePath: tempFileName,
	}
	return SongDownloadResponse{&song, nil}
}

// getHighestBitrateFormat returns the audio format with the highest bitrate
func getHighestBitrateFormat(formats youtube.FormatList) *youtube.Format {
	var bestFormat *youtube.Format
	bestFormat = &formats[0]
	for _, format := range formats {
		if format.Bitrate > bestFormat.Bitrate {
			bestFormat = &format
		}
	}
	return bestFormat
}
