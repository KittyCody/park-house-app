# Park House

Parking management software built with **Spring Boot**.  
Includes a **Spring Authorization Server** (token issuance) and a **JWT RS256-protected API**.

---

## Quick Start

> Just cloned the project? Follow these 4 steps in order.

**1. Start the database**

```bash
docker compose up -d
```

**2. Generate RSA keys** _(one-time setup)_

```bash
keytool -genkeypair -alias jwt-rs256 \
  -keyalg RSA -keysize 2048 -sigalg SHA256withRSA \
  -dname "CN=parkhouse" \
  -storetype PKCS12 \
  -keystore ./jwt-keys.p12 \
  -storepass no_secrets_among_devs
```

**3. Set up your `.env`** _(copy `.env.example` and adjust if needed)_

```env
DB_URL=jdbc:postgresql://localhost:7432/park_db
DB_USERNAME=admin
DB_PASSWORD=password

ISSUER=http://localhost:5140
KEYSTORE_LOCATION=file:./jwt-keys.p12
KEYSTORE_PASSWORD=no_secrets_among_devs
KEY_ALIAS=jwt-rs256
KEY_PASSWORD=no_secrets_among_devs
```

**4. Start the application**

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080` and the auth server at `http://localhost:5140`.

---

## Prerequisites

- JDK 21+
- Docker & Docker Compose
- Maven
- `keytool` (ships with the JDK)
- (Optional) `jq` for pretty-printing JSON

---

## Clients & roles

Three clients are pre-configured for development, all with password `change_me`:

| Client                        | Role         | Typical usage                                          |
|-------------------------------|--------------|--------------------------------------------------------|
| `parking-external-machine-01` | Read only    | Entry/exit gates — query parking status                |
| `parking-internal-machine-01` | Read + write | Internal machines — create and close tickets           |
| `parking-admin`               | Full admin   | Backoffice — configuration, reports, client management |

> In production, change passwords and generate dedicated RSA keys.

---

## Getting an access token

The auth server uses the **Client Credentials** flow (machine-to-machine, no user involved).

```bash
# Example with the external machine
curl -u parking-external-machine-01:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token
```

<details>
<summary>Other clients</summary>

```bash
# Internal machine
curl -u parking-internal-machine-01:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token

# Admin
curl -u parking-admin:change_me \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  http://localhost:5140/oauth2/token
```

</details>

The response contains an `access_token` JWT to use in subsequent requests.

---

## Calling the API

Add the token to the `Authorization` header of each request:

```bash
TOKEN="eyJ..."  # paste your access_token here

# Parking status
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/status

# List active tickets (internal machine or admin only)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/tickets

# Create a ticket (internal machine or admin only)
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "AB-123-CD"}' \
  http://localhost:8080/api/tickets
```

> If you get a `403 Forbidden`, your client does not have the required permissions. See the roles table above.

---

## Troubleshooting

**`keytool` not found**

```
'keytool' is not recognized...
```

Add the `bin/` directory of your JDK to your `PATH`, or use the full path: `/usr/lib/jvm/java-21/bin/keytool`.

---

**`jwt-keys.p12` not found on startup**

```
java.io.FileNotFoundException: jwt-keys.p12
```

Check that `KEYSTORE_LOCATION` in your `.env` points to the correct absolute path.  
If you use `file:./jwt-keys.p12`, the file must be at the project root.

---

**401 error when calling the API**

```json
{
  "error": "invalid_token"
}
```

The token may be expired (token lifetime: 7 days). Generate a new one.  
Check that `ISSUER` in your `.env` exactly matches the auth server URL.

---

**403 error when calling the API**

```json
{
  "error": "access_denied"
}
```

The client you used does not have permission for this endpoint. Use `parking-admin` for sensitive operations.

---

**Database fails to start**

```
port 7432 already in use
```

Another service is using that port. Change the port in `docker-compose.yml` and update `DB_URL` in your `.env`.