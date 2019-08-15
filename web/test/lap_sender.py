import json
import socket
import struct
import sys

def udp_unicast(host, message):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    sock.sendto(message, (host, 31337))
#    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, True)
 #   sock.sendto(message, ("<broadcast>", 31337))
    sock.close()

if len(sys.argv) < 5:
    print('{} <host> <chipid> <duration> <rssi>'.format(sys.argv[0]))
    sys.exit(1)

message = {
    'type': 'lap',
    'chipid': int(sys.argv[2]),
    'duration': int(sys.argv[3]),
    'rssi': int(sys.argv[4])
}

udp_unicast(sys.argv[1], json.dumps(message))
