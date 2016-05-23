from settings import *
from logger import *
from queries import *

def write_file_from_socket(folder, filename, filesize, conn):
    f = open('{}/{}'.format(folder, filename), 'wb')
    chunk = conn.recv(MAX_BUFFER_SIZE)
    current_bytes_received = MAX_BUFFER_SIZE
    # Needs refactor
    while (True):
        if filesize > current_bytes_received:
            f.write(chunk)
        else:
            f.write(chunk[0: (filesize - current_bytes_received) % MAX_BUFFER_SIZE])
            break
        chunk = conn.recv(MAX_BUFFER_SIZE)
        current_bytes_received += MAX_BUFFER_SIZE
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
