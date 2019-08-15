import json
import socket
import struct
import sys

#def ip_to_integer(ip_address):
#    return struct.unpack("!I", socket.inet_aton(ip_address))[0]

def udp_broadcast(ip, message):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, True)
    sock.sendto(message, (ip, 31337))
    sock.close()

if len(sys.argv) < 3:
    print('{} <chipid> <receiverip> <ip>'.format(sys.argv[0]))
    sys.exit(1)

message = {
    'type': 'register32',
    'chipid': int(sys.argv[2]),
    'ip': sys.argv[3]
}

udp_broadcast(sys.argv[1], json.dumps(message))
