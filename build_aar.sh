cd svift-android/app/libs && rm -rf ytdlib.aar
cd ../../../
cd svift-go/SviftYtd/ 
go get golang.org/x/mobile/cmd/gomobile@v0.0.0-20240806205939-81131f6468ab
go mod download
go run golang.org/x/mobile/cmd/gomobile init
GO111MODULE=on
go run golang.org/x/mobile/cmd/gomobile bind -target=android -androidapi 24 -ldflags="-s -w" -o ../../svift-android/app/libs/ytdlib.aar
cd ../../
rm -rf ./svift-android/app/libs/ytdlib-sources.jar