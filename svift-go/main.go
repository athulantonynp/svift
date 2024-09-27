package main

import (
	"encoding/json"
	"fmt"
	sviftytd "svift-go/SviftYtd"
)

// Define a concrete type that implements the DownloadCallback interface
type MyDownloadCallback struct{}

// Implement the OnMessage method for MyDownloadCallback
func (m MyDownloadCallback) OnMessage(msg string) {
    fmt.Println(msg)
}

func main() {
    fmt.Println("Hello, From Svift-Go!")
	var callback sviftytd.DownloadCallback = MyDownloadCallback{}

	var songs = sviftytd.DownloadAudios("Az-mGR-CehY,QhBv0s-1VYQ,MgSpUurG8LM,YNfDpZPYLrc,lxqYJpAbmjE,vl9npDZkQtI,aaj7h0SzXoA,wxrgHG9Tvy4,mz1Bey6KjsQ,wya5utKP_Po,GFA9C2h1lJU,t7wSjy9Lv-o,MQs-6Oeobnw,BlZjTxPAmKc,rhrD7as3KJg","./downloads",callback)
	fmt.Println("Songs:", songs)
	data,err := json.Marshal(songs)
	if err != nil {
		fmt.Println("Error marshalling to JSON:", err)
		return
	}
	fmt.Println(string(data))
}