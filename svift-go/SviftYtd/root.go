package sviftytd

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"

	"github.com/kkdai/youtube/v2"
)

var client youtube.Client = youtube.Client{}

func DownloadAudios(videoIDsStr string, downloadPath string, callback DownloadCallback) string {
	videoIDs := strings.Split(videoIDsStr, ",")
	var wg sync.WaitGroup
	maxGoroutines := 20
	guard := make(chan struct{}, maxGoroutines)
	songs := make([]Song, 0)
	erroredIDs := make([]string, 0)
	var mu sync.Mutex

	for _, videoID := range videoIDs {
		wg.Add(1)
		guard <- struct{}{} // Block if there are already maxGoroutines running

		go func(videoID string) {
			defer wg.Done()
			defer func() { <-guard }() // Release the guard

			response := downloadMp3(videoID, downloadPath)
			if response.Error != nil {
				mu.Lock()
				erroredIDs = append(erroredIDs, videoID)
				mu.Unlock()
			} else {
				mu.Lock()
				songs = append(songs, *response.Song)
				callback.OnMessage(fmt.Sprintf("Downloaded %d of %d", len(songs), len(videoIDs)))
				mu.Unlock()
			}
		}(videoID)
	}

	wg.Wait()

	// Print errored IDs
	if len(erroredIDs) > 0 {
		fmt.Println("Errored YouTube IDs:")
		for _, id := range erroredIDs {
			fmt.Printf("https://www.youtube.com/watch?v=%s\n", id)
		}
	}

	// write to file errored IDs
	erroredIDsFile, err := os.Create("erroredIDs.txt")
	if err != nil {
		fmt.Println("Error creating erroredIDs file:", err)
	}
	defer erroredIDsFile.Close()
	for _, id := range erroredIDs {
		_, err := erroredIDsFile.WriteString(id + "\n")
		if err != nil {
			fmt.Println("Error writing to erroredIDs file:", err)
		}
	}
	// Return downloaded songs as JSON
	data, _ := json.Marshal(songs)
	return string(data)
}

func downloadMp3(videoID string, path string) SongDownloadResponse {
	video, err := client.GetVideo(videoID)
	if err != nil {
		fmt.Println("Error getting video:", err)
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
		fmt.Println("Error getting stream:", err)
		return SongDownloadResponse{nil, &err}
	}
	defer stream.Close()

	// Temporary webm file
	webmFileName := fmt.Sprintf("%s/%s.webm", path, videoID)
	webmFile, err := os.Create(webmFileName)
	if err != nil {
		fmt.Println("Error creating file:", err)
		return SongDownloadResponse{nil, &err}
	}
	defer webmFile.Close()

	_, err = io.Copy(webmFile, stream)
	if err != nil {
		fmt.Println("Error copying stream to file:", err)
		return SongDownloadResponse{nil, &err}
	}

	// Convert webm to mp3
	mp3FileName := fmt.Sprintf("%s/%s.mp3", path, sanitizeFileName(video.Title))
	err = convertWebmToMp3(webmFileName, mp3FileName, video.Title, video.Author, video.Thumbnails)
	if err != nil {
		fmt.Println("Error converting to mp3:", err)
		return SongDownloadResponse{nil, &err}
	}

	// Remove the temporary webm file
	os.Remove(webmFileName)

	song := Song{
		ID:          videoID,
		Provider:    "youtube",
		ThumbnailURL: video.Thumbnails[0].URL,
		Title:       video.Title,
		Author:      video.Author,
		FileName:    filepath.Base(mp3FileName),
		FilePath:    mp3FileName,
	}
	return SongDownloadResponse{&song, nil}
}

// convertWebmToMp3 converts a webm file to an mp3 file and adds metadata and album art
func convertWebmToMp3(webmFile string, mp3File string, title string, author string, thumbnails []youtube.Thumbnail) error {
	// Get the best thumbnail
	thumbnailURL := thumbnails[0].URL
	for _, thumb := range thumbnails {
		if thumb.Width > thumbnails[0].Width {
			thumbnailURL = thumb.URL
		}
	}

	// Download thumbnail
	thumbnailFile := strings.Replace(mp3File, ".mp3", ".jpg", 1)
	err := downloadThumbnail(thumbnailURL, thumbnailFile)
	if err != nil {
		return fmt.Errorf("failed to download thumbnail: %w", err)
	}

	// Execute FFmpeg command to convert webm to mp3 with metadata and album art
	cmd := exec.Command(
		"ffmpeg", "-i", webmFile,
		"-i", thumbnailFile,
		"-map", "0:a", "-map", "1:v",
		"-metadata", fmt.Sprintf("title=%s", title),
		"-metadata", fmt.Sprintf("artist=%s", author),
		"-metadata", "album=YouTube Download",
		"-codec:a", "libmp3lame", "-qscale:a", "2",
		"-disposition:v", "attached_pic",
		mp3File,
	)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("ffmpeg error: %s\n%s", err, string(output))
	}

	// Remove the thumbnail file after embedding
	os.Remove(thumbnailFile)
	return nil
}

// downloadThumbnail downloads a thumbnail image from a URL
func downloadThumbnail(url string, outputPath string) error {
	resp, err := http.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	file, err := os.Create(outputPath)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = io.Copy(file, resp.Body)
	return err
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

// sanitizeFileName removes invalid characters from file names
func sanitizeFileName(name string) string {
	invalidChars := []string{"/", "\\", ":", "*", "?", "\"", "<", ">", "|"}
	for _, char := range invalidChars {
		name = strings.ReplaceAll(name, char, "")
	}
	return name
}