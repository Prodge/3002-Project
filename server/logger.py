
def log(event):
    '''
    Defines the default log behaviour for the server
    '''
    print event

def log_in_out(func):
    '''
    Decorator for logging when a function is entered and completed
    '''
    def log_wrap(*args, **kwargs):
        log('Starting {}'.format(func.__name__))
        output = func(*args, **kwargs)
        log('Finished {}'.format(func.__name__))
        return output
    return log_wrap

