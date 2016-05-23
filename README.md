# 3002-Project

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
| -a | "add" | "filename", "filesize"
| -f | "fetch" | "filename"
| -l | "list" |                        
| -u | "cert" | "filename", "filesize"
| -v | "vouch" | "filename", "certname"



