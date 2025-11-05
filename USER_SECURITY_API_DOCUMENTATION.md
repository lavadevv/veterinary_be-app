# User Security API Documentation

## Overview
Comprehensive security enhancements for User authentication and authorization module including refresh tokens, password management, rate limiting, and session management.

---

## 🔐 Authentication Endpoints

### 1. Login
**POST** `/auth/login`

Enhanced login with refresh token generation, rate limiting, and IP tracking.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "status": "ACTIVE",
    "mustChangePassword": false
  }
}
```

**Security Features:**
- ✅ IP address tracking
- ✅ User-Agent tracking
- ✅ Rate limiting: Max 5 attempts per 15 minutes
- ✅ Account lock after max attempts (30 minutes)
- ✅ IP blocking: Max 10 attempts per 15 minutes
- ✅ User status validation (ACTIVE, LOCKED, INACTIVE)

**Error Responses:**
- `403` - Account locked: "Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần"
- `403` - IP blocked: "IP này đã bị chặn do đăng nhập sai quá nhiều lần"
- `403` - Account inactive: "Tài khoản chưa được kích hoạt"
- `401` - Invalid credentials: "Email hoặc mật khẩu không chính xác"

---

### 2. Refresh Token
**POST** `/user/refresh-token`

Generate new access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

**Security Features:**
- ✅ Token rotation: Old refresh token revoked, new one issued
- ✅ IP address validation
- ✅ User-Agent tracking
- ✅ Automatic expiry check

**Error Responses:**
- `401` - Invalid token: "Refresh token không hợp lệ hoặc đã hết hạn"
- `401` - Revoked token: "Refresh token đã bị thu hồi"

---

### 3. Logout
**POST** `/user/logout`

Logout current session by revoking refresh token.

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

---

### 4. Logout All Devices
**POST** `/user/logout-all`

Revoke all refresh tokens for the authenticated user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Đã đăng xuất tất cả thiết bị"
}
```

---

## 🔑 Password Management Endpoints

### 5. Change Password
**POST** `/user/change-password`

Change password for authenticated user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "oldPassword": "OldPass123!",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công"
}
```

**Password Policy:**
- ✅ Minimum 8 characters
- ✅ At least one uppercase letter
- ✅ At least one lowercase letter
- ✅ At least one digit
- ✅ At least one special character (@$!%*?&)
- ✅ Cannot reuse last 3 passwords

**Error Responses:**
- `400` - Weak password: "Mật khẩu phải có ít nhất 8 ký tự..."
- `400` - Password reuse: "Mật khẩu đã được sử dụng trước đó. Vui lòng chọn mật khẩu khác"
- `401` - Wrong old password: "Mật khẩu cũ không chính xác"

---

### 6. Forgot Password
**PUT** `/auth/forgot-password`

Reset password using OTP verification.

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "password": "NewSecurePass123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": "Mật khẩu đã được thay đổi thành công. Vui lòng đăng nhập lại."
}
```

**Security Features:**
- ✅ OTP validation (10 minutes timeout)
- ✅ Password strength validation
- ✅ Password history check
- ✅ All refresh tokens revoked after reset
- ✅ Clear mustChangePassword flag

---

## 📱 Session Management Endpoints

### 7. Get Active Sessions
**GET** `/user/sessions`

List all active sessions for the authenticated user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "deviceInfo": "Chrome on Windows",
      "ipAddress": "192.168.1.100",
      "lastUsed": "2025-11-02T10:30:00Z",
      "createdAt": "2025-11-01T08:00:00Z",
      "expiresAt": "2025-11-08T08:00:00Z",
      "current": true
    },
    {
      "id": 124,
      "deviceInfo": "Safari on iPhone",
      "ipAddress": "192.168.1.101",
      "lastUsed": "2025-11-01T15:20:00Z",
      "createdAt": "2025-11-01T12:00:00Z",
      "expiresAt": "2025-11-08T12:00:00Z",
      "current": false
    }
  ],
  "total": 2
}
```

---

### 8. Revoke Specific Session
**DELETE** `/user/sessions/{refreshToken}`

Revoke a specific session by refresh token.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Đã xóa phiên đăng nhập"
}
```

---

## 👤 User Account Endpoints

### 9. Verify Account
**PUT** `/auth/verify-account`

Verify user account with OTP after registration.

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "data": "OTP đã xác thực thành công. Tài khoản đã được kích hoạt."
}
```

**Security Features:**
- ✅ Sets user status to ACTIVE
- ✅ Clears block flag
- ✅ Removes OTP after verification

---

## 🗑️ User Management Endpoints (Admin)

### 10. Delete User (Soft Delete)
**DELETE** `/user/{id}`

Soft delete a user (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Xóa người dùng thành công"
}
```

**Security Features:**
- ✅ Soft delete (sets deletedAt, deletedBy)
- ✅ Sets user status to INACTIVE
- ✅ Preserves data for audit trail
- ✅ Admin authorization required

---

### 11. Delete Multiple Users (Soft Delete)
**DELETE** `/user/all`

Soft delete multiple users (Admin only).

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Request Body:**
```json
[1, 2, 3, 4, 5]
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "email": "user1@example.com",
      "fullName": "User One",
      "status": "INACTIVE",
      "deletedAt": "2025-11-02T10:30:00Z"
    }
  ]
}
```

---

## 📊 Configuration

### JWT Configuration (`application.yml`)

```yaml
jwt:
  key: ${JWT_KEY}
  # Access token expiration (15 minutes)
  expiration: 900
  # Refresh token expiration (7 days)
  refresh-token-expiration: 604800

security:
  # Rate limiting
  max-login-attempts: 5
  login-attempt-window: 900  # 15 minutes
  
  # Account locking
  account-lock-duration: 1800  # 30 minutes
  
  # IP blocking
  max-ip-attempts: 10
  ip-attempt-window: 900  # 15 minutes
  
  # Password policy
  password-history-size: 3
  
  # OTP configuration
  otp-verify-ttl: 600  # 10 minutes
  otp-reset-ttl: 600  # 10 minutes
  otp-regen-min-interval: 30  # 30 seconds
```

---

## 🗄️ Database Schema

### New Tables

#### 1. refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    revoked_at DATETIME,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE INDEX idx_token ON refresh_tokens(token);
CREATE INDEX idx_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);
```

#### 2. password_history
```sql
CREATE TABLE password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    changed_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE INDEX idx_user_id ON password_history(user_id);
CREATE INDEX idx_changed_at ON password_history(changed_at);
```

#### 3. login_attempts
```sql
CREATE TABLE login_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    attempted_at DATETIME NOT NULL,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255)
);

CREATE INDEX idx_email_time ON login_attempts(email, attempted_at);
CREATE INDEX idx_ip_time ON login_attempts(ip_address, attempted_at);
```

### Updated User Table
```sql
ALTER TABLE user ADD COLUMN deleted_at DATETIME;
ALTER TABLE user ADD COLUMN deleted_by VARCHAR(255);
ALTER TABLE user ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION';
ALTER TABLE user ADD COLUMN password_changed_at DATETIME;
ALTER TABLE user ADD COLUMN must_change_password BOOLEAN DEFAULT FALSE;
ALTER TABLE user ADD COLUMN locked_until DATETIME;
ALTER TABLE user ADD COLUMN failed_login_attempts INT DEFAULT 0;

CREATE INDEX idx_status ON user(status);
CREATE INDEX idx_deleted_at ON user(deleted_at);
```

---

## 🔄 Scheduled Tasks

### Token Cleanup Job
Runs daily at 2:00 AM to remove expired and revoked tokens.

```java
@Scheduled(cron = "0 0 2 * * *")
public void cleanupExpiredTokens() {
    // Remove expired tokens older than 30 days
    // Remove revoked tokens older than 7 days
}
```

---

## 🛡️ Security Best Practices

### 1. Token Management
- ✅ Short-lived access tokens (15 minutes)
- ✅ Long-lived refresh tokens (7 days)
- ✅ Token rotation on refresh
- ✅ Revoke all tokens on password change
- ✅ Track device and IP per token

### 2. Password Security
- ✅ Strong password policy enforcement
- ✅ Password history tracking (last 3)
- ✅ Bcrypt hashing with salt
- ✅ Force password change on admin reset

### 3. Rate Limiting
- ✅ Per-user rate limiting (5 attempts / 15 min)
- ✅ Per-IP rate limiting (10 attempts / 15 min)
- ✅ Automatic account locking (30 minutes)
- ✅ Exponential backoff on failed attempts

### 4. Audit Trail
- ✅ All login attempts logged (success/failure)
- ✅ Soft delete with audit fields
- ✅ Password change history
- ✅ Session activity tracking

---

## 📱 Frontend Integration Example

### Login Flow
```javascript
// 1. Login
const loginResponse = await fetch('/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { token, refreshToken } = await loginResponse.json();

// Store tokens
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);

// 2. Use access token for API calls
const apiResponse = await fetch('/api/protected-resource', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 3. Refresh token when access token expires
if (apiResponse.status === 401) {
  const refreshResponse = await fetch('/user/refresh-token', {
    method: 'POST',
    body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
  });
  
  const { accessToken, refreshToken: newRefreshToken } = await refreshResponse.json();
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', newRefreshToken);
  
  // Retry original request
}

// 4. Logout
await fetch('/user/logout', {
  method: 'POST',
  body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
});

localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
```

---

## ⚠️ Error Codes Reference

| Code | Message | Description |
|------|---------|-------------|
| 401 | Invalid credentials | Email or password incorrect |
| 403 | Account locked | Too many failed login attempts |
| 403 | IP blocked | Too many attempts from this IP |
| 403 | Account inactive | Account not yet verified or deactivated |
| 400 | Weak password | Password doesn't meet policy requirements |
| 400 | Password reused | Password matches recent history |
| 401 | Invalid refresh token | Token expired or revoked |
| 400 | OTP invalid | OTP incorrect or expired |

---

## 🚀 Testing Recommendations

### 1. Rate Limiting Tests
```bash
# Test user rate limiting (should lock after 5 attempts)
for i in {1..6}; do
  curl -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrongpassword"}'
done
```

### 2. Token Rotation Test
```bash
# Verify old refresh token is revoked after refresh
curl -X POST http://localhost:8080/user/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"OLD_TOKEN"}'
```

### 3. Password Policy Test
```bash
# Test weak password rejection
curl -X POST http://localhost:8080/user/change-password \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"OldPass123!","newPassword":"weak","confirmPassword":"weak"}'
```

---

## 📝 Migration Notes

### Breaking Changes
1. ⚠️ Login endpoint now requires IP and User-Agent (handled automatically by controller)
2. ⚠️ Login response now includes `refreshToken`, `status`, `mustChangePassword`
3. ⚠️ Delete operations are now soft deletes (data preserved)
4. ⚠️ Users must be in ACTIVE status to login

### Backward Compatibility
- ✅ Existing access tokens continue to work
- ✅ Old endpoints remain functional
- ✅ No database migration required (DDL auto-update enabled)

---

## 📞 Support

For issues or questions:
- Check application logs for detailed error messages
- Review security configuration in `application.yml`
- Verify database schema matches expected structure
- Test with provided curl commands

---

**Last Updated:** November 2, 2025  
**Version:** 2.0.0  
**Author:** Backend Development Team
