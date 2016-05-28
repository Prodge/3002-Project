from settings import *
import datetime

def log(event):
    '''
    Defines the default log behaviour for the server
    '''
    timestamp = datetime.datetime.strftime(
        datetime.datetime.now(),
        '%Y-%m-%d %H:%M:%S:%f'
    )
    this_log = '[{}]  {}'.format(timestamp, event)
    if LOGGING_ENABLED:
        fo = open(LOG_FILE, "a")
        fo.write("{}\n".format(this_log))
        fo.close()
    if VERBOSE:
        print this_log

def log_in_out(func):
    '''
    Decorator for logging when a function is entered and completed
    '''
    def log_wrap(*args, **kwargs):
        function_identifier = '{}.{}'.format(func.__module__, func.__name__)
        log('Starting: {}'.format(function_identifier))
        output = func(*args, **kwargs)
        log('Finished: {}'.format(function_identifier))
        return output
    return log_wrap

