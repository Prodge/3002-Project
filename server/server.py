from os.path import exists
from os import makedirs

import socket
import json
import ssl

from logger import *
import database as db
import settings
import tasks

@log_in_out
def init():
    '''
    Initialises the server sockets and database
    '''
    # DB
    db.init()

    # Folders
    for folder in REQUIRED_FOLDERS:
        if exists(folder):
            log('Folder "{}" exists'.format(folder))
        else:
            log('Creating folder "{}"'.format(folder))
            makedirs(folder)

    # Sockets
    soc = socket.socket()
    soc.bind(('', int(PORT)))
    soc.listen(MAX_CONNECTIONS)
    log("Listening on port: {}".format(PORT))
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
        json_data = conn.recv(settings.MAX_BUFFER_SIZE)
        log('Received Instruction: {}'.format(json_data))

        data = json.loads(json_data)
        task_func = getattr(tasks, 'task_{}'.format(data.get('Operation')))

        try:
            task_func(data, conn)
        except Exception as e:
            log('Eception Occured: {}'.format(e))
            tasks.send_msg(conn, 500, '{}'.format(e))

        conn.close()

main()
