from os.path import exists
from os import remove, makedirs

from settings import *
from logger import *
from database import query

def is_file_in_database(filename):
    res = query(
        '''
        select count(*)
        from {}
        where filename = "{}"
        '''.format(DB_TABLENAME_FILES, filename)
    )
    return res[0] != 0

def add_file_cert_mapping(filename, certname):
    query(
        '''
        insert into {}
        values ("{}", "{}")
        '''.format(DB_TABLENAME_FILES, filename, certname)
    )

def update_file_cert_mapping(filename, new_certname):
    query(
        '''
        update {}
        set certname = "{}"
        where filename = "{}"
        '''.format(DB_TABLENAME_FILES, new_certname, filename)
    )

def get_file_cert_mappings():
    return query('select * from {}'.format(DB_TABLENAME_FILES))

def get_file_cert_mapping(filename):
    return query('select * from {} where filename="{}"'.format(DB_TABLENAME_FILES, filename))

def file_exists(filename):
    return exists('{}/{}'.format(FILES_FOLDER, filename))

def cert_exists(certname):
    return exists('{}/{}'.format(CERTS_FOLDER, certname))

def remove_file(filename):
    remove('{}/{}'.format(FILES_FOLDER, filename))

def remove_cert(filename):
    remove('{}/{}'.format(CERTS_FOLDER, certname))

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
