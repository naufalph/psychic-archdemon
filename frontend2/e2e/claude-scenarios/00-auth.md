# Auth Scenarios

Test users (credentials already seeded in dev DB):
- CLIENT: test.client1@rumantra.com / password123
- ARCHITECT: test.architect1@rumantra.com / password123

App runs at: http://localhost:3001

---

## UI Labels (app is in Indonesian by default)
- Email field label: "Email"
- Password field label: "Kata Sandi"
- Submit button: "Masuk"
- Signup role CLIENT: "Menyewa Arsitek"
- Signup role ARCHITECT: "Menawarkan Layanan"
- First Name: "Nama Depan" / Last Name: "Nama Belakang"
- Create Account button: "Buat Akun"
- Confirm Password: "Konfirmasi Kata Sandi"

---

## Scenario 1: Login as CLIENT

1. Navigate to http://localhost:3001/login
2. Take a screenshot to confirm the login page rendered
3. Fill "Email" field with `test.client1@rumantra.com`
4. Fill "Kata Sandi" field with `password123`
5. Click the "Masuk" button
6. Wait for navigation — expect URL to contain `/client/`
7. Take a screenshot of the resulting page
8. Verify: a dashboard with the client's name or project summary is visible
9. Check localStorage: `auth_token` should be set and non-empty

**Pass criteria:** Lands on `/client/dashboard` with visible content. Token in localStorage.

---

## Scenario 2: Login as ARCHITECT

1. Navigate to http://localhost:3001/login
2. Fill "Email" with `test.architect1@rumantra.com`, "Password" with `password123`
3. Click "Sign In"
4. Wait for URL to contain `/architect/`
5. Take a screenshot
6. Verify: architect dashboard content is visible (opportunities, bid quota, etc.)

**Pass criteria:** Lands on `/architect/dashboard`.

---

## Scenario 3: Wrong credentials show error

1. Navigate to http://localhost:3001/login
2. Fill "Email" with `notreal@example.com`, "Password" with `wrongpassword`
3. Click "Sign In"
4. Wait 3 seconds
5. Take a screenshot
6. Verify: an error message is visible (red alert box)
7. Verify: URL is still `/login`

**Pass criteria:** Error banner visible, no navigation happened.

---

## Scenario 4: Unauthenticated redirect

1. Clear localStorage (evaluate: `localStorage.clear()`)
2. Navigate directly to http://localhost:3001/client/dashboard
3. Verify: redirected to `/login` or `/`
4. Take a screenshot confirming the redirect

**Pass criteria:** Protected page is not shown to unauthenticated user.

---

## Scenario 5: Signup form — CLIENT role

1. Navigate to http://localhost:3001/signup
2. Take a screenshot — should show "Saya ingin" role selection screen
3. Click "Menyewa Arsitek" (Hire an Architect)
4. Verify: form appears with Nama Depan, Nama Belakang, Email, Kata Sandi fields
5. Fill Nama Depan: "Test", Nama Belakang: "User", Email: "newtest@example.com"
6. Fill Kata Sandi: "Secure1@" and Konfirmasi Kata Sandi: "Secure1@"
7. Check the terms checkbox (#agreeTerms)
8. DO NOT submit — instead take a screenshot to verify the form is filled
9. Test the error: change Konfirmasi Kata Sandi to "Different1@"
10. Click "Buat Akun"
11. Verify: password mismatch error appears

**Pass criteria:** Password mismatch error is visible.

---

## Scenario 6: Signup form — password strength validation

1. Navigate to http://localhost:3001/signup
2. Click "Menawarkan Layanan" (Offer Services — architect role)
3. Fill all fields but set Kata Sandi: "weak"
4. Click "Buat Akun"
5. Verify: password strength error appears (at least 8 characters)

**Pass criteria:** Validation error visible without navigating away.
