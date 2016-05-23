from settings import *
from logger import *

@log_in_out
def task_add(data, conn):
    filename = data.get('filename', None)
    if not filename:
        raise ValueError('No filename recieved')

    if not is_file_in_database(filename):
        add_file_cert_mapping(filename, '')

    conn.send('ready to receive')
    print 'sent ready'

    if file_exists(filename):
        remove_file(filename)
    f = open('{}/{}'.format(FILES_FOLDER, filename), 'wb')
    chunk = conn.recv(MAX_BUFFER_SIZE)
    while (chunk):
        f.write(chunk)
        conn.recv(MAX_BUFFER_SIZE)
    f.close()

@log_in_out
def task_list(data, conn):
    mappings_dict = [
        {'filename': mapping[0], 'certname': mapping[1]}
            for mapping in get_file_cert_mappings()
    ]
    conn.send(json.dumps(mappings_dict))
