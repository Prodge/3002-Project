# 3002-Project

**A Project by Tim Metcalf and Don Wimodya Randula Athukorala**

## Instructions to deploy client and server programs
### Server
1. Install dependencies needed to run the server:
```
sudo apt-get install python2.7 python-dev
sudo pip install -r requirements.txt
```
2. Run the server (use --help to get the usage):
   `python2 server.py [ARGS and PARAMS]`

### Client
1. Compile the java files:
   `make`
2. Run the client (use --help to get the usage):
   `java client [ARG and PARMS]`

## Instructions to create a circle of trust
- In the path "client/test_certs" there are certificates which can be used to create a circle of trust.
- The 4 certificates starting with **tim** creates a circle of trust of length 4.
- The 8 certificates starting with **wimo** cerates a circle of trust of length 8.

## Client command line options which are needed to be implemented
| Option | Description |
| ------ | ----------- |
| -a filename | add or replace a file on the oldtrusty server
| -c number | provide the required circumference (length) of a circle of trust
| -f filename | fetch an existing file from the oldtrusty server (simply sent to stdout)
| -h hostname:port | provide the remote address hosting the oldtrusty server
| -l | list all stored files and how they are protected
| -n name | require a circle of trust to involve the named person (i.e. their certificate)
| -u certificate | upload a certificate to the oldtrusty server
| -v filename certificate | vouch for the authenticity of an existing file in the oldtrusty server using the indicated certificate

## Header layout
Header is a JSON encoded dictionary.

example (vouch):
{
  'operation': 'vouch',
  'filename': 'someFile.txt',
  'certname': 'someCert.xx',
}

example (list):
{
  'operation': 'list',
}

| Option | Operation value | Additional Keys |
| ------ | --------------- | --------------- |
| -a | "add" | "filename", "file_size", _"key"_
| -f | "fetch" | "filename", _"cot_size"_, _"cot_name"_, _"key"_
| -l | "list" | _"key"_
| -u | "cert" | "filename", "file_size"
| -v | "vouch" | "filename", "certname"
| -k | "get_key" |
- _Italics/bold_ indicate the key is optional

- "key" (-k) will return a unique secure key to use for storing and retrieving files
- "encrypt" (-e [KEY]) will use the key to add encrypted files, recieve encrypted files and list your encrypted files on the server and other unecrypted files

## Circle of trust spec
- If a COT size is specified, can't fetch a file unless it is of that COT
- If a COT name is specified, can't fetch a file unless the COT contains a cert of that name

## Server Dependencies
- OpenSSL for python
  - sudo pip install pyOpenSSL

- pycrypto for python
  - sudo pip install pycrypto

- python-dev package
  - sudo apt-get install python-dev

- passlib for python
  - sudo pip install passlib

- Python 2.7
