## How to run application stack
### Build and start backend API

If you want to build it from scratch (or you can skip and use a prebuilt .jar in `./log-service/prebuilt`)
1. install mvn (maven) on your linux
1. from the root of this repo, navigate to ./log-service directory
2. run `mvn clean package` 
3. the service .jar is in ./log-service/target/

Start the API using the command
```java -jar <path to jar file>```

### Build and start the UI
1. from the root of this repo, navigate to ./log-ui
2. run `npm install`
3. run `npm run dev`
4. in your browser, go to `http://localhost:3000`

### How to use the UI
1. select the private key for authentication in ./auth-key/private-key.pem
2. in log path text box, enter the path of the log file in /var/log directory
3. [Optional] enter filter string
4. Hit `Download` button. Or `Next` for subsequent pages

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
openssl x509 -pubkey -noout -in certificate.pem > public_key.pem
```

## Sample request
```
curl --location 'http://localhost:8080/logs/apache2/error.log?take=100&contains=command%20line' \
--header 'Authorization: eyJhbGciOiJSUzI1NiJ9.eyJ1c2VySWQiOiJoYW9uZ3V5ZSJ9.A7Uy2CAIhLLwzLHQQwGl23fcRr6Ez-UcweuB1CURXbHuFNIrmLxFUn_Kr65jQZ6ccEGBVqUvI_E9vFbHf2VsKA66O0JUoAYezAEiBujlYklKmmZPiKgMFGIAuWtSmPaB-6dqfhxFTwyz7mSHys_MxwYT3tjnMIprV8UyWCnVjJIyLnswZ1lrgCJtYaV2vdJ5eg_kzODs5kllEsh4O62Cl0dQdQTtiybNRPmAym6ASZl4cVQOMWSqWsQ3i3eioN2eBwWzfhBB3y_O8eKhSq-4daStd7OnKhUjF9X6uOXHYzuuOsrKRcY3q9us-nULz_IKFTF6mhQ6QcXfviWeAr6MUA'
```