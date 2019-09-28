# running fpvlaptracker in connected mode using a raspberry pi

the following descriptions explains the setup of a raspberry pi as base unit for multiple trackers running in connected mode.

i'm using a raspberry pi b3+ currently but a raspberry pi b2 also is working well. using the pi3 you can use the onboard wifi chipset.

pi4 was not tested yet but should also be working.

## setup wifi access point
the following guide will setup a wifi network called `flt-base` with password `flt-base`. the tracker nodes can automatically connect to it. you can also choose a different name and password but have to adjust it on the nodes using the mobile app then.

all the information (with only slight changes) regarding wifi setup was taken from [frillip.com](https://frillip.com/using-your-raspberry-pi-3-as-a-wifi-access-point-with-hostapd/). you can find further informations about the procedure there.

install dnsmasq and hostpad to provide access point mode and DHCP and DNS server
`sudo apt-get install dnsmasq hostapd`

setup a fixed ip address for your wifi interface.

open /etc/dhcpd.conf using `sudo nano /etc/dhcpcd.conf` and add at the bottom of the file `denyinterfaces wlan0`

open /etc/network/interfaces by using `sudo nano /etc/network/interfaces`

edit the `wlan0` section that it looks like follows
```allow-hotplug wlan0
iface wlan0 inet static
    address 172.24.1.1
    netmask 255.255.255.0
    network 172.24.1.0
    broadcast 172.24.1.255
#    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
```

restart dhcpd and reload configuration for wlan0
```
sudo service dhcpcd restart
sudo ifdown wlan0; sudo ifup wlan0
```

configure hostapd by opening /etc/hostapd/hostapd.conf using `sudo nano /etc/hostapd/hostapd.conf`

put the following configuration in the config file
```# wifi interface to use
interface=wlan0

# Use the nl80211 driver with the brcmfmac driver
driver=nl80211

# This is the name of the network
ssid=flt-base

# Use the 2.4GHz band
hw_mode=g

# Use channel 6
channel=6

# Enable 802.11n
ieee80211n=1

# Enable WMM
wmm_enabled=1

# Enable 40MHz channels with 20ns guard interval
ht_capab=[HT40][SHORT-GI-20][DSSS_CCK-40]

# Accept all MAC addresses
macaddr_acl=0

# Use WPA authentication
auth_algs=1

# Require clients to know the network name
ignore_broadcast_ssid=0

# Use WPA2
wpa=2

# Use a pre-shared key
wpa_key_mgmt=WPA-PSK

# The network passphrase
wpa_passphrase=flt-base

# Use AES, instead of TKIP
rsn_pairwise=CCMP
```

open the default configuration file for hostapd using `sudo nano /etc/default/hostapd`, find the line `#DAEMON_CONF=""` and replace it with `DAEMON_CONF="/etc/hostapd/hostapd.conf"`.

open /etc/dnsmasq.conf by using `sudo nano /etc/dnsmasq.conf` and replace everything in the file with the following configuration:
```interface=wlan0      # Use interface wlan0
listen-address=172.24.1.1 # Explicitly specify the address to listen on
bind-interfaces      # Bind to the interface to make sure we aren't sending things elsewhere
server=8.8.8.8       # Forward DNS requests to Google DNS
domain-needed        # Don't forward short names
bogus-priv           # Never forward addresses in the non-routed address spaces.
dhcp-range=172.24.1.50,172.24.1.150,72h # Assign IP addresses between 172.24.1.50 and 172.24.1.150 with a 72 hour lease time
```

start the services
```sudo service hostapd start
sudo service dnsmasq start
```

you should now see a wifi network called `flt-base`. try to connect to it with password `flt-base`.

## install fpvlaptracker host software on the pi
using the automatic installation method the tracker software is installed in /home/pi/fpvlaptracker

1. connect the raspi to your local network (easiest by cable).
2. login to the raspi and issue the following wget to download the install shell script:
`https://raw.githubusercontent.com/warhog/fpvlaptracker/master/web/install.sh`
3. login to the pi and execute `install.sh` for automatic installation.
```chmod +x install.sh
./install.sh <version>
```
replace version with the version you like to install (see [github releases](https://github.com/warhog/fpvlaptracker/releases)).
4. after successful installation reboot the pi, connect to the wifi and call [`http://172.24.1.1`](http://172.24.1.1) in the browser of your choice.

## config file
you can set further options (like language for speech output) using the [config file](/docs/configuration.md) located in `/home/pi/fpvlaptracker/application.properties`

