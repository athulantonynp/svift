package sviftytd

type Song struct {
    ID           string `json:"id"`
    Provider     string `json:"provider"`
    ThumbnailURL string `json:"thumbnailURL"`
    Title        string `json:"title"`
    Author       string `json:"author"`
    FileName     string `json:"fileName"`
    FilePath     string `json:"filePath"`
}

type SongDownloadResponse struct{
	Song *Song
	Error *error
}