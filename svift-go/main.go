package main

import (
	"encoding/json"
	"fmt"
	"io/fs"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strings"
	sviftytd "svift-go/SviftYtd"
)

// Define a concrete type that implements the DownloadCallback interface
type MyDownloadCallback struct{}

// Implement the OnMessage method for MyDownloadCallback
func (m MyDownloadCallback) OnMessage(msg string) {
    fmt.Println(msg)
}

// func main() {
// 	dir := "./downloads" // Replace with the directory containing your files
// 	err := RenameMp3FilesInDirectory(dir)
// 	if err != nil {
// 		fmt.Printf("Error: %v\n", err)
// 	} else {
// 		fmt.Println("All MP3 files processed.")
// 	}
// }

func main() {
    fmt.Println("Hello, From Svift-Go!")
	var callback sviftytd.DownloadCallback = MyDownloadCallback{}

	var songsList = readFile()
	
	var songs = sviftytd.DownloadAudios(songsList,"./downloads",callback)
	fmt.Println("Songs:", songs)
	_,err := json.Marshal(songs)
	if err != nil {
		fmt.Println("Error marshalling to JSON:", err)
		return
	}
	fmt.Println("Success")
}

func readFile()string{
	data, err := ioutil.ReadFile("music2.json")
	if err != nil {
		log.Fatalf("Failed to read file: %v", err)
	}

	// Parse the JSON array
	var jsonArray []string
	if err := json.Unmarshal(data, &jsonArray); err != nil {
		log.Fatalf("Failed to parse JSON: %v", err)
	}

	// Convert the array to a comma-separated string
	commaSeparated := strings.Join(jsonArray, ",")
	return commaSeparated
}



func sanitizeFileName(name string) string {
	// Remove everything after the first dot but before `.mp3`
	name = regexp.MustCompile(`\.[^.]+$`).ReplaceAllString(name, "")
	// Replace special characters with underscores
	name = regexp.MustCompile(`[^a-zA-Z0-9]+`).ReplaceAllString(name, "_")
	return name
}

func RenameMp3FilesInDirectory(dir string) error {
	// Walk through the directory
	err := filepath.WalkDir(dir, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return err
		}

		// Only process files
		if !d.IsDir() {
			ext := strings.ToLower(filepath.Ext(d.Name()))
			if ext == ".mp3" {
				dirPath := filepath.Dir(path)
				baseName := strings.TrimSuffix(d.Name(), ext)

				// Sanitize the base name
				sanitizedBaseName := sanitizeFileName(baseName)

				// Construct the new file name
				newFileName := sanitizedBaseName + ext
				newPath := filepath.Join(dirPath, newFileName)

				// Rename the file
				if path != newPath { // Avoid renaming if the name is already correct
					err := os.Rename(path, newPath)
					if err != nil {
						fmt.Printf("Error renaming file %s: %v\n", path, err)
					} else {
						fmt.Printf("Renamed: %s -> %s\n", path, newPath)
					}
				}
			}
		}
		return nil
	})

	if err != nil {
		return fmt.Errorf("error walking directory: %w", err)
	}
	return nil
}
