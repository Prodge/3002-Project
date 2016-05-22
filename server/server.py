import socket
import ssl

from settings import *
from logger import log_in_out, log

@log_in_out
def init_socks():
    '''
    Initialises the server sockets
    '''
    soc = socket.socket()
    soc.bind(('', PORT))
    soc.listen(MAX_CONNECTIONS)
    ssl_soc = ssl.wrap_socket(
        soc,
        ssl_version=ssl.PROTOCOL_TLSv1,
        cert_reqs=ssl.CERT_NONE,  # not sure if this is important
        server_side=True,
        keyfile='./cert.pem',
        certfile='./cert.pem')
    return ssl_soc

def main():
    soc = init_socks()
    while True:
        conn, address = soc.accept()
        print 'got connection from {}'.format(address)
        conn.recv(MAX_BUFFER_SIZE)
        conn.close()

main()
