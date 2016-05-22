import socket
import ssl

from settings import *
from logger import *
import database as db

@log_in_out
def init():
    '''
    Initialises the server sockets and database
    '''
    db.init()
    soc = socket.socket()
    soc.bind(('', PORT))
    soc.listen(MAX_CONNECTIONS)
    ssl_soc = ssl.wrap_socket(
        soc,
        ssl_version = ssl.PROTOCOL_TLSv1,
        cert_reqs = ssl.CERT_NONE,  # not sure if this is important
        server_side = True,
        keyfile = KEY_FILE,
        certfile = CERT_FILE)
    return ssl_soc

def main():
    soc = init()
    while True:
        conn, address = soc.accept()
        log('New connection from {}'.format(address))
        data = conn.recv(MAX_BUFFER_SIZE)
        log('Received: {}'.format(data))
        conn.close()

main()
