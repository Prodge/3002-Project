from settings import *
from queries import *

def load_cert(certname):
    file_location = '{}/{}'.format(CERTS_FOLDER, certname)
    return crypto.load_certificate(crypto.FILETYPE_PEM, file(file_location).read())

def get_cert_subject(certname):
    return load_cert(certname).get_subject().get_components()

def get_cert_issuer(certname):
    return load_cert(certname).get_issuer().get_components()

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

def get_cert_map():
    '''
    Returns a unique list of dicts of attributes for every cert
    '''
    cert_subject = get_cert_subject(certname)
    return list(set([
        {
            'certname': certname,
            'subject': cert_subject,
            'issuer': get_cert_issuer(certname),
            'common_name': get_cert_sobject_name(cert_subject),
        }
            for certname in os.listdir(CERTS_FOLDER)
    ]))


def get_issuer_certs(cert_map, cert):
    '''
    Return a list of cert_map dicts that contain
    the subject equal to the issuer of the given cert
    '''
    return filter(lambda c: c['subject'] == cert['issuer'], cert_map)

def get_all_cots(filename):
    '''
    Returns all possible COT's as a list of lists.
    Each list contains a COT starting from the cert associated with 'filename'.
    Each cert in each list is represented by a dict mapping aquired from get_cert_map().
    Returns false if there are no COT's
    '''
    certname = get_linked_cert(filename)
    cert_map = get_cert_map()
    start_cert = filter(lambda c: c['certname'] == certname, cert_map)[0]

    def expand_paths(paths, cots, start_cert):
        '''
        Take the first path in paths
        If any extended nodes exist create a new path with the extended nodes
        if an extended node of the starting node exists, add the node to COTS
        return cots once there are no paths left
        '''
        if len(paths) == 0:
            return cots
        path = paths[0]
        last_cert = path[-1]
        next_certs = get_issuer_certs(last_cert['certname'])
        for next_cert in next_certs:
            if next_cert == start_cert:
                # Found a COT!
                cots.append(path)
            elif next_cert not in path:
                # Create two new paths, marking the old path as finished
                paths.append(path + [next_cert])
        paths.pop(0)
        expand_paths(paths, cots, start_cert)

    cots = expand_paths([[start_cert]], [], start_cert)


def get_largest_cot(filename):
    return max(get_all_cots(filename), key=len)
