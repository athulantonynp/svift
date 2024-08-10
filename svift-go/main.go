package main

import (
	"encoding/json"
	"fmt"
	"svift-go/sviftytd"
)

func main() {
    fmt.Println("Hello, From Svift-Go!")
	var songs = sviftytd.DownloadAudios("6xqNk5Sf5jo,fNc39L3aDTs",".")
	fmt.Println("Songs:", songs)
	data,err := json.Marshal(songs)
	if err != nil {
		fmt.Println("Error marshalling to JSON:", err)
		return
	}
	fmt.Println(string(data))
}