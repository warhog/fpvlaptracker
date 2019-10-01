# configuration of the host software
you can configure the application using the **application.properties** file.

the following options are available:

|option|description|
|---|---|
|admin.password|the admin password for the race admin|
|shutdown.machine|if a shutdown command using webui is made shutdown the operating system as well|
|server.port|webserver port to use (should be 80)|
|network.server.ip|ip address to listen for udp packets by the node (optional, see extra section)|
|network.server.broadcast|broadcast ip address to listen for udp packets by the node (optional, see extra section)|

## network configuration
you need to configure the ip and broadcast addresses the server should listen to.

this can be either done using the ip addresses or the network interface names for easier configuration.

if you don't provide `network.server.ip` and `network.server.broadcast` in your configuration it defaults to `GET_FROM_wlan0`.

### configuration by ip address
simply give the ip addresses:
```
network.server.ip=192.168.1.5
network.server.broadcast=192.168.1.255
```

### configuration by network interface name
configuration using the network interface names is more generic. instead of an ip address you give a string like `GET_FROM_wlan0`. 
the ip address configuration is then fetched from the interface wlan0 (default on rpi for wifi). 
you can give whatever name is needed, e.g. `GET_FROM_enp0s32e6` or `GET_FROM_wlx74da285ed9a3`.
```
network.server.ip=GET_FROM_wlan0
network.server.broadcast=GET_FROM_wlan0
```


## example
example application.properties file
```
admin.password=admin
audio.playLocal=false
shutdown.machine=false
network.server.ip=GET_FROM_wlan0
network.server.broadcast=GET_FROM_wlan0
```
