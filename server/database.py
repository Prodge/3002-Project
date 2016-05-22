from settings import *
from logger import *
import sqlite3

def connect():
    return sqlite3.connect(DB_FILENAME)

def require_db(func):
    '''
    Decorator for encapsulating a function with a db connection
    '''
    def db_wrap(*args, **kwargs):
        connection = connect()
        cursor = connection.cursor()
        output = func(cursor, *args, **kwargs)
        connection.commit()
        connection.close()
        return output
    return db_wrap

@require_db
@log_in_out
def init(cursor):
    '''
    Create DB with table if it doesn't exist
    '''
    try:
        cursor.execute(
            '''
            SELECT COUNT(*)
            FROM {}
            '''.format(DB_TABLENAME_FILES)
        )
    except sqlite3.OperationalError as e: # Database is empty
        log('Database not found/empty, Creating tables')
        cursor.execute(
            '''
            CREATE TABLE {0}
            (
                filename char[{1}],
                certname char[{1}]
            )
            '''.format(DB_TABLENAME_FILES, MAX_FILENAME_LENGTH)
        )

