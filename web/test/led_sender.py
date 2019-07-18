import json
import socket
import struct
import sys

def ip_to_integer(ip_address):
    return struct.unpack("!I", socket.inet_aton(ip_address))[0]

def udp_broadcast(message):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, True)
    sock.sendto(message, ("<broadcast>", 31337))
    sock.close()

if len(sys.argv) < 3:
    print('{} mode color <interval>'.format(sys.argv[0]))
    sys.exit(1)

ignore = True
ignore2 = True
data = 'led'
for arg in sys.argv:
    if ignore:
        ignore = False
        continue
    data = '{} {}'.format(data, arg)
    
udp_broadcast(data)
