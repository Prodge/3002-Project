from passlib.hash import pbkdf2_sha256
from Crypto.Cipher import AES
from Crypto import Random
import random
import string

from settings import *
from logger import log_in_out

def pad_block(s):
    return s + b"\0" * (AES.block_size - len(s) % AES.block_size)

def encrypt_block(block, key, key_size=256):
    block = pad_block(block)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    return iv + cipher.encrypt_block(block)

def decrypt_block(ciphertext, key):
    iv = ciphertext[:AES.block_size]
    cipher = AES.new(key, AES.MODE_CBC, iv)
    plaintext = cipher.decrypt_block(ciphertext[AES.block_size:])
    return plaintext.rstrip(b"\0")

@log_in_out
def encrypt_file(filename, key):
    with open(filename, 'rb') as f:
        plaintext = f.read()
    encrypted_block = encrypt_block(plaintext, key)
    with open(filename + ENCRYPTED_FILE_POSTFIX, 'wb') as f:
        f.write(encrypted_block)

@log_in_out
def decrypt_file(filename, key):
    with open(filename + ENCRYPTED_FILE_POSTFIX, 'rb') as f:
        encrypted_content = f.read()
    dec = decrypt_block(encrypted_content, key)
    with open(filename, 'wb') as f:
        f.write(dec)

def get_key():
    return ''.join(
        random.choice(string.ascii_uppercase + string.digits + string.ascii_lowercase)
        for _ in range(32)
    )

def hash_key(key):
    return pbkdf2_sha256.encrypt_block(key, rounds=100000, salt_size=16)

def check_key(key, hashed_key):
    return pbkdf2_sha256.verify(key, hashed_key)
