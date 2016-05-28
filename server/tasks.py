from StringIO import StringIO
from os.path import getsize
from OpenSSL import crypto
import json

from encryption import *
from settings import *
from queries import *
from logger import *
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
    key = data.get('key', None)
    filepath = '{}/{}'.format(FILES_FOLDER, filename)
    assert '/' not in filename, "Invalid filename"
    send_msg(conn, 200, 'ready to receive')
    if not is_file_in_database(filename):
        add_file_cert_mapping(filename, '')
    if is_file_key_in_database(filename):
        assert key, "This file requires a key to update"
        assert check_key(key, get_file_key_hash(filename)), "Invalid key"
    if file_exists(filename):
        remove_file(filename)
    f = open(filepath, 'wb')
    write_file_from_socket(f, filesize, conn)
    f.close()

    if key:
        encrypt_file(filepath, key)
        remove_file(filename)
        if not is_file_key_in_database(filename):
            add_file_key_mapping(filename, hash_key(key))

@log_in_out
def task_list(data, conn):
    file_list = []
    key = data.get('key', None)
    cert_file_mappings = list(get_file_cert_mappings())
    if key:
        assert check_key(key, get_file_key_hash(filename)), "Invalid key"
        hidden_files = get_protected_files() - get_key_hash_files(hash_key(key))
        cert_file_mappings = filter(lambda f: f[0] not in hidden_files, cert_file_mappings)

    for mapping in cert_file_mappings:
        file_found = False
        for f in file_list:
            if f['filename'] == mapping[0]:
                file_found = True
                f['certname'].append(mapping[1])
        if not file_found:
            file_list.append(
                {
                    'filename': mapping[0],
                    'certname': [mapping[1]],
                    'cot_size': len(get_largest_cot(mapping[0])),
                    'filesize': getsize('{}/{}'.format(FILES_FOLDER, mapping[0])),
                }
            )
    send_struct(conn, file_list)

@log_in_out
def task_cert(data, conn):
    filename, filesize = get_data(data, *['filename', 'file_size'])
    send_msg(conn, 200, 'ready to receive')
    assert not file_exists(filename), "A certificate with this name already exists"

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
    send_msg(conn, 200, '{} now vouches for {}'.format(certname, filename))

@log_in_out
def task_fetch(data, conn):
    filename, = get_data(data, *['filename'])
    cot_size = data.get('cot_size', None)
    cot_name = data.get('cot_name', None)
    key = data.get('key', None)
    filepath = '{}/{}'.format(FILES_FOLDER, filename)
    assert file_exists(filename) and is_file_in_database(filename), "{} does not exist".format(filename)

    cots = get_all_cots(filename)
    log(cot_size)
    if cot_size:
        cot_size = int(cot_size)
        cots = filter(lambda cot: len(cot) >= cot_size, cots)
        assert len(cots), "Circle of trust did not meet required length"
    if cot_name:
        assert cot_name in [cert['common_name'] for cot in cots for cert in cot], "Circle of trust did not contain the required name"

    if key:
        assert is_file_key_mapping_in_database(filename), "This file does not need a key to fetch"
        assert check_key(key, get_file_key_hash(filename)), "Invalid key"
        decrypt_file(filename)

    filesize = getsize('{}/{}'.format(FILES_FOLDER, filename))
    send_struct(conn,{'status_code': 200, 'file_size': filesize})

    f = open(filepath, 'rb')
    chunk = f.read(MAX_BUFFER_SIZE)
    while len(chunk) != 0:
        conn.send(chunk)
        chunk = f.read(MAX_BUFFER_SIZE)
    f.close()

    if key:
        remove_file(filename)

def task_get_key(data, conn):
    send_struct(get_key())
