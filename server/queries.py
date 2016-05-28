from os.path import exists
from os import remove, makedirs, listdir

from settings import *
from database import query

'''
    Database Operations
'''
def is_file_in_database(filename):
    res = query(
        '''
        select count(*)
        from {}
        where filename = "{}"
        '''.format(DB_TABLENAME_FILES, filename)
    )
    return res[0][0] != 0

def is_file_key_in_database(filename):
    res = query(
        '''
        select count(*)
        from {}
        where filename = "{}"
        '''.format(DB_TABLENAME_KEYS, filename)
    )
    return res[0][0] != 0

def is_file_cert_mapping_in_database(filename, certname):
    res = query(
        '''
        select count(*)
        from {}
        where filename = "{}" and certname = "{}"
        '''.format(DB_TABLENAME_FILES, filename, certname)
    )
    return res[0][0] != 0

def add_file_cert_mapping(filename, certname):
    query(
        '''
        insert into {}
        values ("{}", "{}")
        '''.format(DB_TABLENAME_FILES, filename, certname)
    )

def add_file_key_mapping(filename, key_hash):
    query(
        '''
        insert into {}
        values ("{}", "{}")
        '''.format(DB_TABLENAME_KEYS, filename, key_hash)
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

def get_linked_certs(filename):
    return [mapping[1] for mapping in get_file_cert_mapping(filename)]

def get_file_key_hash(filename):
    return query('select * from {} where filename="{}"'.format(DB_TABLENAME_KEYS, filename))[0][1]

def get_key_hash_files(key_hash):
    return map(lambda row: row[0], query('select * from {} where key_hash="{}"'.format(DB_TABLENAME_KEYS, key_hash)))

def get_protected_files(key_hash):
    return map(lambda row: row[0], query('select * from {}'.format(DB_TABLENAME_KEYS)))

'''
    File Operations
'''
def file_exists(filename):
    return exists('{}/{}'.format(FILES_FOLDER, filename)) or \
        exists('{}/{}{}'.format(FILES_FOLDER, filename, ENCRYPTED_FILE_POSTFIX))

def cert_exists(certname):
    return exists('{}/{}'.format(CERTS_FOLDER, certname))

def remove_file(filename):
    remove('{}/{}'.format(FILES_FOLDER, filename))

def remove_cert(filename):
    remove('{}/{}'.format(CERTS_FOLDER, certname))
