# configuration of the host software
you can configure the application using the **application.properties** file.

the following options are available:

|option|description|
|---|---|
|admin.password|the admin password for the race admin|
|audio.playLocal|play local audio (in addition to remote websocket audio)|
|shutdown.machine|if a shutdown command using webui is made shutdown the operating system as well|
|audio.language|audio language to use. usa a BCP 47 language tag here, e.g. en-US or en-UK or de-DE|
|server.port|webserver port to use (should be 80)|


## example
example application.properties file
```
admin.password=admin
audio.playLocal=false
shutdown.machine=false
audio.language=de-DE
```