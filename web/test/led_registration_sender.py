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

if len(sys.argv) < 2:
    print('{} <chipid> <ip>'.format(sys.argv[0]))
    sys.exit(1)

message = {
    'type': 'registerled',
    'chipid': int(sys.argv[1]),
    'ip': ip_to_integer(sys.argv[2])
}

udp_broadcast(json.dumps(message))
