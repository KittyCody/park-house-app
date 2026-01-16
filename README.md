# Park House

Parking management software built with **Spring Boot**.  
Includes **Spring Authorization Server** (token issuance) and a **JWT RS256–protected API**.

---

## Prerequisites

- JDK 21+
- Docker & Docker Compose
- Maven
- `keytool` (ships with the JDK)
- (Optional) `jq` for pretty-printing JSON

---

## Generate RSA Keys (PKCS#12)

Create a **PKCS#12** keystore that contains the **private key** and a **certificate** (public key).

> In PKCS#12, the **keystore password equals the key password**.

```bash   
keytool -genkeypair -alias jwt-rs256 \
  -keyalg RSA -keysize 2048 -sigalg SHA256withRSA \
  -dname "CN=parkhouse" \
  -storetype PKCS12 \
  -keystore ./jwt-keys.p12 \
  -storepass your_pass

## Issue an Access Token
A development client is seeded: parking-external-machine-01, parking-internal-machine-01, parking-admin / change_me
```

```bash
curl -u parking-external-machine-01:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token
```

```bash
curl -u parking-internal-machine-01:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token
```

```bash
curl -u parking-admin:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token
```