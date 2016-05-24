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

'''
    Certificate Operations
'''
def load_cert(certname):
    file_location = '{}/{}'.format(CERTS_FOLDER, certname)
    return crypto.load_certificate(crypto.FILETYPE_PEM, file(file_location).read())

def get_cert_subject(certname):
    return load_cert(certname).get_subject().get_components()

def get_cert_issuer(certname):
    return load_cert(certname).get_issuer().get_components()

def get_cert_map():
    return [
        {
            'certname': certname,
            'subject': get_cert_subject(certname),
            'issuer': get_cert_issuer(certname),
        }
            for certname in os.listdir(CERTS_FOLDER)
    ]

def get_cert_subject_name(cert_subject):
    '''
    Returns the CN field of a cert
    Returns false if the cert does not have a common name field
    '''
    names = [field[1] for field in cert_subject if field[0] == 'CN']
    if not len(names):
        return False
    else:
        assert (len(names) == 1), 'Invalid certificate {}'.format(certname)
        return name[0]
