# Secure Vault

Secure Vault is a personal Java project developed after completing the Epitech Java Piscine, with the objective of applying Java programming skills to a concrete cybersecurity-oriented problem.

The project was designed to turn the concepts learned during the Piscine into a complete application, while exploring fundamental cybersecurity topics such as authentication, password protection, encryption, access control, and audit logging.

## Project Overview

Secure Vault is a command-line application that allows an authenticated user to manage a private file vault from a terminal interface.

### Main Features

- User account creation and authentication
- Password protection using PBKDF2-HMAC-SHA-256 with a unique salt
- A dedicated vault for each user
- File encryption using AES-256-GCM
- Encrypted file recovery through authenticated decryption
- Protection against path traversal attempts
- Temporary login lockout after repeated authentication failures
- Audit logging of significant application events
- Defensive error handling for invalid input and file operations

The project is intentionally implemented as a lightweight Java CLI application without a database or graphical interface. The objective is to keep the architecture understandable while applying practical security principles.

## Security Model

### Password Protection

Passwords are never stored in plaintext.

During account creation, a random salt is generated and the password is processed with **PBKDF2-HMAC-SHA-256** using a configurable iteration count.

The stored user record contains:

- the username;
- the derived password value;
- the salt.

During authentication, the submitted password is processed again using the stored salt and compared with the stored derived value.

### File Encryption

Files are encrypted before they are stored in the user's vault.

Secure Vault uses **AES-256-GCM** with a newly generated random IV for each encryption operation.

The encrypted file contains the IV together with the authenticated ciphertext. GCM also provides an authentication tag, allowing the application to detect unauthorized modification of encrypted data during decryption.

### Access Control

Each user receives a dedicated directory under:

```text
vault/<username>/
```

File paths are normalized and validated so that file operations remain within the authorized vault directory.

### Brute-Force Resistance

Authentication failures are tracked in memory.

After **three consecutive failed attempts**, the account is temporarily locked for **30 seconds**.

A successful authentication resets the failure state.

### Audit Logging

Security-relevant events such as account creation, successful and failed authentication, file encryption, file recovery, file deletion, and logout are written to:

```text
logs/audit.log
```

Sensitive credentials are intentionally excluded from the audit trail.

## Architecture

The application follows a simple separation-of-responsibilities model:

```text
Main
 |
 +-- AuthService ------ User
 |       |
 |       +-- CryptoService
 |
 +-- Vault
 |     |
 |     +-- FileManager
 |
 +-- AuditLogger
```

### `Main.java`

Provides the command-line interface and coordinates the different application components.

### `User.java`

Represents an authenticated user and stores the username together with the protected password information required for authentication.

### `AuthService.java`

Handles account creation, authentication, login-attempt tracking, and temporary account lockout.

### `Vault.java`

Represents the authenticated user's private storage area and recovery directory.

### `FileManager.java`

Handles persistence of user records and file operations such as encrypted storage, listing, recovery, and deletion.

### `CryptoService.java`

Centralizes password derivation and AES-GCM encryption and decryption operations.

### `AuditLogger.java`

Provides centralized audit logging for important application events.

## Project Structure

```text
SecureVault/
|
+-- src/
|   +-- Main.java
|   +-- User.java
|   +-- AuthService.java
|   +-- Vault.java
|   +-- FileManager.java
|   +-- CryptoService.java
|   +-- AuditLogger.java
|
+-- data/          # Runtime user data, ignored by Git
+-- vault/         # Encrypted user files, ignored by Git
+-- recovered/     # Decrypted recovery files, ignored by Git
+-- logs/          # Runtime audit logs, ignored by Git
+-- README.md
+-- .gitignore
```

Runtime directories are intentionally excluded from version control because they can contain user-specific data and generated files.

## Requirements

- **Java JDK 17 or later** is recommended.
- A terminal or command-line environment.

The project relies on the Java standard library and does not require an external dependency manager.

## Installation

Clone the repository:

```bash
git clone https://github.com/adelred78/securevault-java.git
cd securevault-java
```

Compile the project:

```bash
javac src/*.java
```

Run the application:

```bash
java -cp src Main
```

## Usage

### 1. Create an Account

Select the account creation option and provide a username and password.

The application stores the protected password representation rather than the plaintext password.

### 2. Authenticate

Use the credentials created previously. After successful authentication, the user's personal vault is opened.

### 3. Add a File

Provide the path of a local file. The file is encrypted with AES-GCM before being stored in the user's vault as an `.enc` file.

### 4. List Files

The application displays the encrypted files currently stored in the authenticated user's vault.

### 5. Recover a File

A selected encrypted file can be decrypted and restored in the user's recovery directory.

### 6. Delete a File

Encrypted files can be removed directly from the vault.

## Security Considerations and Limitations

Secure Vault is a **personal educational project**, not a production-grade password manager or enterprise storage system.

Current limitations include:

- user records are stored locally in a text-based file rather than a database;
- login lockout state is held in application memory and is reset when the application stops;
- key derivation is tied to the user's authentication password for this educational implementation;
- recovered files are written in plaintext to a separate local recovery directory and therefore require appropriate operating-system permissions and user handling;
- the application has no multi-process or multi-device synchronization;
- there is no graphical interface.

These limitations are deliberate and provide clear areas for future development.

## Possible Improvements

Future versions could introduce:

- a stronger key-management architecture with independent key storage and rotation;
- persistent and centralized rate limiting;
- database-backed user management;
- secure file metadata handling;
- automated unit and integration tests;
- a graphical or web interface;
- stronger operational logging and monitoring;
- secure deletion and encrypted recovery workflows.

## Learning Objectives

This project was developed to connect Java development with practical cybersecurity concepts, including:

- object-oriented programming;
- encapsulation and separation of responsibilities;
- file-system APIs and persistence;
- authentication and access control;
- password derivation and salting;
- authenticated encryption;
- path traversal prevention;
- brute-force mitigation;
- audit logging;
- defensive error handling;
- Git and GitHub workflow.

## Project Status

**Status: Functional personal project**

The core authentication, encrypted file storage, recovery, access-control checks, brute-force mitigation, and audit logging features are implemented and tested manually.

## Author

**Adel Redjemi**

Computer science student with a strong focus on cybersecurity.

GitHub: https://github.com/adelred78
