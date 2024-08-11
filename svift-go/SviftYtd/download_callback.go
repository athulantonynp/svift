package sviftytd

type DownloadCallback interface {
	OnMessage(string)
}