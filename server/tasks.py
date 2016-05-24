from os.path import getsize
import json

from settings import *
from logger import *
from queries import *

@log_in_out
def write_file_from_socket(folder, filename, filesize, conn):
    f = open('{}/{}'.format(folder, filename), 'wb')
    current_bytes_received = 0
    while (int(filesize) != current_bytes_received): # filesize == CBR on final packet
        chunk = conn.recv(MAX_BUFFER_SIZE)
        current_bytes_received += len(chunk)
        f.write(chunk)
    f.close()
    send_msg(conn, 200, 'ok')

def get_data(data, *args):
    values = []
    for key in args:
        value = data.get(key, None)
        assert value, 'No {} recieved'.format(key)
        values.append(value)
    return values

def send_dict(conn, dict):
    conn.send(json.dumps(dict) + '\0')

def send_msg(conn, status_code, msg):
    send_dict(
        {
            'status_code': status_code,
            'msg': msg,
        }
    )

@log_in_out
def task_add(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    send_msg(conn, 200, 'ready to receive')
    if not is_file_in_database(filename):
        add_file_cert_mapping(filename, '')
    if file_exists(filename):
        remove_file(filename)
    write_file_from_socket(FILES_FOLDER, filename, filesize, conn)

@log_in_out
def task_list(data, conn):
    mappings_dict = [
        {'filename': mapping[0], 'certname': mapping[1]}
            for mapping in get_file_cert_mappings()
    ]
    conn.send(json.dumps(mappings_dict) + '\0')

@log_in_out
def task_cert(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    send_msg(conn, 200, 'ready to receive')
    if file_exists(filename):
        remove_file(filename)
    write_file_from_socket(CERTS_FOLDER, filename, filesize, conn)

@log_in_out
def task_vouch(data, conn):
    filename, certname = get_data(data, *['filename', 'certname'])
    add_file_cert_mapping(filename, certname)
    send_msg(conn, 200, 'ok')

@log_in_out
def task_fetch(data, conn):
    filename, = get_data(data, *['filename'])
    assert file_exists(filename), "File does not exist"

    filesize = getsize('{}/{}'.format(FILES_FOLDER, filename))
    send_dict({'status_code': 200, 'file_size': filesize})

    chunk = True
    f = open('{}/{}'.format(FILES_FOLDER, filename), 'rb')
    while chunk:
        chunk = f.read(MAX_BUFFER_SIZE)
        conn.send(chunk)
    f.close()
