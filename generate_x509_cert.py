#!/usr/bin/python

from OpenSSL import crypto, SSL
from socket import gethostname
from pprint import pprint
from time import gmtime, mktime
from os.path import exists, join
from sys import argv

'''
USAGE:

    python2 generate_x509_cert.py {subject-name} {issuer-name}

OUTPUT FILENAME:

    {subject-name}-{issuer-name}.pem

'''

def create_self_signed_cert(cert_dir):
    # create a key pair
    k = crypto.PKey()
    k.generate_key(crypto.TYPE_RSA, 1024)

    # create a self-signed cert
    cert = crypto.X509()
    cert.get_subject().C = "AU"
    cert.get_subject().ST = "Western Australia"
    cert.get_subject().L = "Perth"
    cert.get_subject().O = "UWA"
    cert.get_subject().OU = "computer science"
    cert.get_subject().CN = argv[1]
    cert.set_serial_number(1000)
    cert.gmtime_adj_notBefore(0)
    cert.gmtime_adj_notAfter(10*365*24*60*60)
    cert.set_issuer(cert.get_subject())
    cert.get_issuer().CN = argv[2]
    cert.set_pubkey(k)
    cert.sign(k, 'sha1')

    open(join(cert_dir, '{}-{}.pem'.format(argv[1], argv[2])), "wt").write(
        crypto.dump_certificate(crypto.FILETYPE_PEM, cert))

create_self_signed_cert(".")
