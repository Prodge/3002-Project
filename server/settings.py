from optparse import OptionParser
import socket

HOST = socket.gethostname()
PORT = 2445
MAX_CONNECTIONS = 5
MAX_BUFFER_SIZE = 1024

CERT_FILE = './cert.pem'
KEY_FILE = CERT_FILE

DB_FILENAME = 'old_trusty.db'
DB_TABLENAME_FILES = 'file_cert_map'

MAX_FILENAME_LENGTH = 200

LOGGING_ENABLED = False
LOG_FILE = 'log'
VERBOSE = False

FILES_FOLDER = 'files'
CERTS_FOLDER = 'certs'
REQUIRED_FOLDERS = [
    FILES_FOLDER,
    CERTS_FOLDER,
]

# Update with cli options
parser = OptionParser(usage="Usage: python server.py [options]")
parser.add_option(
    "-v", "--verbose",
    action = "store_true",
    default = VERBOSE
)
parser.add_option(
    "-l", "--logging-enabled",
    action = "store_true",
    default = LOGGING_ENABLED
)
parser.add_option(
    "-o", "--log-file",
    action = "store",
    default = LOG_FILE
)
parser.add_option(
    "-c", "--certs-folder",
    action = "store",
    default = CERTS_FOLDER
)
parser.add_option(
    "-f", "--files-folder",
    action = "store",
    default = FILES_FOLDER
)
parser.add_option(
    "-a", "--cert-file",
    action = "store",
    default = CERT_FILE
)
parser.add_option(
    "-k", "--key-file",
    action = "store",
    default = KEY_FILE
)
parser.add_option(
    "-p", "--port",
    action = "store",
    default = PORT
)
parser.add_option(
    "-d", "--db-filename",
    action = "store",
    default = DB_FILENAME
)
options, _ = parser.parse_args()

for option, value in vars(options).iteritems():
    vars()[option.upper()] = value
