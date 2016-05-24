from os.path import exists
from os import remove, makedirs, listdir
from OpenSSL import crypto

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

'''
    File Operations
'''
def file_exists(filename):
    assert cert_exists(certname), "Certificate does not exist"
    return exists('{}/{}'.format(FILES_FOLDER, filename))

def cert_exists(certname):
    return exists('{}/{}'.format(CERTS_FOLDER, certname))

def remove_file(filename):
    remove('{}/{}'.format(FILES_FOLDER, filename))

def remove_cert(filename):
    remove('{}/{}'.format(CERTS_FOLDER, certname))
