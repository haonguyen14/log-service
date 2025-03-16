## How to generate X509 public/private keypair (for testing)

```
openssl genrsa -out private_key.pem 2048
```
```
openssl req -new -key private_key.pem -out csr.pem
``` 
```
openssl x509 -req -days 365 -in csr.pem -signkey private_key.pem -out certificate.pem
```
```
openssl x509 -req -days 365 -in csr.pem -signkey private_key.pem -out certificate.pem
```

## Sample request
```
curl --location 'http://localhost:8080/logs/apache2/error.log?take=100&contains=command%20line' \
--header 'Authorization: eyJhbGciOiJSUzI1NiJ9.eyJ1c2VySWQiOiJoYW9uZ3V5ZSJ9.A7Uy2CAIhLLwzLHQQwGl23fcRr6Ez-UcweuB1CURXbHuFNIrmLxFUn_Kr65jQZ6ccEGBVqUvI_E9vFbHf2VsKA66O0JUoAYezAEiBujlYklKmmZPiKgMFGIAuWtSmPaB-6dqfhxFTwyz7mSHys_MxwYT3tjnMIprV8UyWCnVjJIyLnswZ1lrgCJtYaV2vdJ5eg_kzODs5kllEsh4O62Cl0dQdQTtiybNRPmAym6ASZl4cVQOMWSqWsQ3i3eioN2eBwWzfhBB3y_O8eKhSq-4daStd7OnKhUjF9X6uOXHYzuuOsrKRcY3q9us-nULz_IKFTF6mhQ6QcXfviWeAr6MUA'
```