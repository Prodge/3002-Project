from settings import *
import datetime

def log(event):
    '''
    Defines the default log behaviour for the server
    '''
    if LOGGING_ENABLED:
        timestamp = datetime.datetime.strftime(
            datetime.datetime.now(),
            '%Y-%m-%d %H:%M:%S:%f'
        )
        print '[{}]  {}'.format(timestamp, event)

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

