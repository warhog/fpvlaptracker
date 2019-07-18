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

if len(sys.argv) < 4:
    print('{} <chipid> <duration> <rssi>'.format(sys.argv[0]))
    sys.exit(1)

message = {
    'type': 'lap',
    'chipid': int(sys.argv[1]),
    'duration': int(sys.argv[2]),
    'rssi': int(sys.argv[3])
}

udp_broadcast(json.dumps(message))
