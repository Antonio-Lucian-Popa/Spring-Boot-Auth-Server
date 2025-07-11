# 🔐 Auth Server - Spring Boot JWT Authentication

Un server complet de autentificare scris în Java (Spring Boot), care oferă suport pentru:

- ✅ JWT Authentication (Access + Refresh)
- ✅ Email Verification
- ✅ Forgot Password Flow
- ✅ OAuth2 Login (Google)
- ✅ Role-based Access
- ✅ Retry Email Service
- ✅ HTML Email Templates (Thymeleaf)
- ✅ Secure Cookies pentru Web
- ✅ API pentru Mobile (React Native)
- ✅ Audit Logs
- ✅ Auto-delete conturi inactive

---

## 🏗️ Stack Tehnologic

- Java 17
- Spring Boot 3
- Spring Security
- Spring OAuth2 Client
- Spring Retry
- Spring Mail
- Thymeleaf
- Liquibase
- PostgreSQL
- JWT (`jjwt`)
- Maven

---

## ⚙️ Configurare

### 🔑 JWT Config

```yaml
jwt:
  secret: your-secret-key
  access-token-expiration-minutes: 15
  refresh-token-expiration-days: 7
```