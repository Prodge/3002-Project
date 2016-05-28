from optparse import OptionParser
from os.path import exists
from os import makedirs

import socket
import json
import ssl

from settings import *
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

@log_in_out
def parse_options():
    parser = OptionParser(usage="Usage: python server.py [options]")
    parser.add_option(
        "-l", "--logging-enabled",
        action = "store_true",
        default = LOGGING_ENABLED
    )
    parser.add_option(
        "-o", "--log-file",
        action = "store",
        default = LOG_FILE
    )
    parser.add_option(
        "-c", "--certs-folder",
        action = "store",
        default = CERTS_FOLDER
    )
    parser.add_option(
        "-f", "--files-folder",
        action = "store",
        default = FILES_FOLDER
    )
    parser.add_option(
        "-a", "--cert-file",
        action = "store",
        default = CERT_FILE
    )
    parser.add_option(
        "-k", "--key-file",
        action = "store",
        default = KEY_FILE
    )
    parser.add_option(
        "-p", "--port",
        action = "store",
        default = PORT
    )
    parser.add_option(
        "-d", "--db-filename",
        action = "store",
        default = DB_FILENAME
    )
    options, _ = parser.parse_args()

    for option, value in options.itteritems():
        setattr(settings, option.upper(), value)

def main():
    parse_options()
    soc = init()
    while True:
        conn, address = soc.accept()
        log('New connection from {}'.format(address))
        json_data = conn.recv(MAX_BUFFER_SIZE)
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
