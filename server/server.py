#!/usr/bin/python

import socket
import ssl

def log(event):
    print event

def log_in_out(func):
    def log_wrap(*args, **kwargs):
        log('Starting {}'.format(func.__name__))
        output = func(*args, **kwargs)
        log('Finished {}'.format(func.__name__))
        return output
    return log_wrap

HOST = socket.gethostname()
PORT = 2445
MAX_CONNECTIONS = 5
MAX_BUFFER_SIZE = 4096

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
