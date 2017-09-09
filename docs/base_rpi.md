# running fpvlaptracker network gateway on a raspberry pi

## setup wifi access point

https://frillip.com/using-your-raspberry-pi-3-as-a-wifi-access-point-with-hostapd/

## copy fpvlaptracker host software to the pi
copy the fpvlaptracker.jar file, the application.properties and the audio folder to the raspberry pi (using scp, winscp, ...)

## make fpvlaptracker autostart
i appended the following to my /etc/rc.local file:

`cd /home/pi/`

`java -jar fpvlaptracker-0.1.jar`


## config file
you can configure the application using the application.properties file.

the following options are available:

|option|description|
|---|---|
|admin.password|the admin password for the race admin|
|audio.playLocal|play local audio (in addition to remote websocket audio)|
|shutdown.machine|if a shutdown command using webui is made shutdown the operating system as well|
|audio.language|audio language to use. basically just the folder name of the language folder inside the /audio folder|
|server.port|webserver port to use (should be 80)|
