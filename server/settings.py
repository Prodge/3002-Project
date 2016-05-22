import socket

HOST = socket.gethostname()
PORT = 2445
MAX_CONNECTIONS = 5
MAX_BUFFER_SIZE = 4096

CERT_FILE = './cert.pem'
KEY_FILE = CERT_FILE

DB_FILENAME = 'old_trusty.db'
DB_TABLENAME_FILES = 'file_cert_map'

MAX_FILENAME_LENGTH = 200

LOGGING_ENABLED = True
