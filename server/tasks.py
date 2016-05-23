from sys import getsizeof

from settings import *
from logger import *
from queries import *

@log_in_out
def write_file_from_socket(folder, filename, filesize, conn):
    f = open('{}/{}'.format(folder, filename), 'wb')
    # Needs refactor
    current_bytes_received = 0
    last_bytes_received = 0
    while (True):
        chunk = conn.recv(MAX_BUFFER_SIZE)
        current_bytes_received += len(chunk)
        if filesize > current_bytes_received:
            f.write(chunk)
        else:
            f.write(chunk)
            # extra_bytes = filesize - last_bytes_received
            # if extra_bytes == 0:
                # f.write(chunk)
            # else:
                # # This line is wrong as the slice is based on characters not bytes HOWEVER it appears the final packet will always contain the exact number of bytes anyway so it doesn't need to be sliced
                # f.write(chunk[0: extra_bytes])
            break
        last_bytes_received = current_bytes_received # this also appears unnessesary
    f.close()
    conn.send('200\0')

def get_data(data, *args):
    values = []
    for key in args:
        value = data.get(key, None)
        assert value, 'No {} recieved'.format(key)
        values.append(value)
    return values

@log_in_out
def task_add(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    conn.send('ready to receive\0')
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
    conn.send(json.dumps(mappings_dict))

@log_in_out
def task_cert(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    conn.send('ready to receive\0')
    if file_exists(filename):
        remove_file(filename)
    write_file_from_socket(CERTS_FOLDER, filename, filesize, conn)

@log_in_out
def task_vouch(data, conn):
    filename, certname = get_data(data, *['filename', 'certname'])
    add_file_cert_mapping(filename, certname)
    conn.send('200\0')
