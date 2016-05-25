from os.path import getsize
from StringIO import StringIO
from OpenSSL import crypto
import json

from settings import *
from logger import *
from queries import *
from cert import *

@log_in_out
def write_file_from_socket(file_object, filesize, conn):
    current_bytes_received = 0
    while (int(filesize) != current_bytes_received): # filesize == CBR on final packet
        chunk = conn.recv(MAX_BUFFER_SIZE)
        current_bytes_received += len(chunk)
        file_object.write(chunk)

def get_data(data, *args):
    values = []
    for key in args:
        value = data.get(key, None)
        assert value, 'No {} recieved'.format(key)
        values.append(value)
    return values

def send_struct(conn, dict):
    conn.send(json.dumps(dict) + '\0')

def send_msg(conn, status_code, msg):
    send_struct(conn,
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
    f = open('{}/{}'.format(FILES_FOLDER, filename), 'wb')
    write_file_from_socket(f, filesize, conn)
    f.close()

@log_in_out
def task_list(data, conn):
    send_struct(conn,
        [
            {
                'filename': mapping[0],
                'certname': mapping[1],
                'cot_size': len(get_largest_cot(mapping[0])),
                'filesize': getsize('{}/{}'.format(FILES_FOLDER, mapping[0])),
            }
                for mapping in get_file_cert_mappings()
        ]
    )

@log_in_out
def task_cert(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    send_msg(conn, 200, 'ready to receive')
    if file_exists(filename):
        remove_file(filename)
    # Write socket to string buffer to test validity once complete
    cert_buffer = StringIO()
    write_file_from_socket(cert_buffer, filesize, conn)
    cert_buffer.seek(0)
    try:
        crypto.load_certificate(crypto.FILETYPE_PEM, cert_buffer.read())
        log('Certificate check passed: {}'.format(filename))
    except crypto.Error as e:
        # Force re-raise with custom string
        assert False, 'Invalid certificate upload attempt: {}'.format(filename)
        cert_buffer.close()
        return
    # Finally write file from string buffer
    cert_buffer.seek(0)
    f = open('{}/{}'.format(CERTS_FOLDER, filename), 'wb')
    f.write(cert_buffer.read())
    f.close()
    cert_buffer.close()

@log_in_out
def task_vouch(data, conn):
    filename, certname = get_data(data, *['filename', 'certname'])
    assert file_exists(filename), "File does not exist"
    assert cert_exists(certname), "Certificate does not exist"
    assert not is_file_cert_mapping_in_database(filename, certname), "This certificate already vouches for this file"
    if len(get_file_cert_mapping(filename)) == 1 and get_file_cert_mapping(filename)[0][1] == '' :
        update_file_cert_mapping(filename, certname)
    else:
        add_file_cert_mapping(filename, certname)
    send_msg(conn, 200, 'ok')

@log_in_out
def task_fetch(data, conn):
    filename, = get_data(data, *['filename'])
    cot_size = data.get('cot_size', None)
    cot_name = data.get('cot_name', None)
    assert file_exists(filename), "File does not exist"

    cots = get_all_cots(filename)
    log(cot_size)
    if cot_size:
        cot_size = int(cot_size)
        cots = filter(lambda cot: len(cot) >= cot_size, cots)
        assert len(cots), "Circle of trust did not meet required length"
    if cot_name:
        assert cot_name in [cert['common_name'] for cot in cots for cert in cot], "Circle of trust did not contain the required name"

    filesize = getsize('{}/{}'.format(FILES_FOLDER, filename))
    send_struct(conn,{'status_code': 200, 'file_size': filesize})

    f = open('{}/{}'.format(FILES_FOLDER, filename), 'rb')
    chunk = f.read(MAX_BUFFER_SIZE)
    while len(chunk) != 0:
        conn.send(chunk)
        chunk = f.read(MAX_BUFFER_SIZE)
    f.close()
