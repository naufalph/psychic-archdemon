# Client Flow Scenarios

Prerequisites:
- Both frontend (localhost:3001) and backend (localhost:8080) must be running
- Test client account: test.client1@rumantra.com / password123

---

## Scenario 1: Client dashboard renders

1. Log in as CLIENT via API (POST http://localhost:8080/rmtr/users/login with `{"email":"test.client1@rumantra.com","password":"password123"}`)
2. Set `auth_token` in localStorage from the response `data.token`
3. Navigate to http://localhost:3001/client/dashboard
4. Take a screenshot
5. Verify: dashboard content is visible (not a blank page or error screen)
6. Verify: navigation bar is present with links

**Pass criteria:** Dashboard loads with content.

---

## Scenario 2: Client projects list

1. Log in as CLIENT (use API helper or UI)
2. Navigate to http://localhost:3001/client/projects
3. Take a screenshot
4. Verify: either project cards are shown, OR an empty-state message is shown
5. Verify: a "Create Project" button or link is visible

**Pass criteria:** Page loads, create button exists.

---

## Scenario 3: Create project form opens

1. Log in as CLIENT
2. Navigate to http://localhost:3001/client/projects/create
3. Take a screenshot
4. Verify: a form is visible with input fields for project details

**Pass criteria:** Form renders with inputs.

---

## Scenario 4: Create project — validation

1. Log in as CLIENT
2. Navigate to http://localhost:3001/client/projects/create
3. Click the submit/next button WITHOUT filling anything
4. Take a screenshot
5. Verify: validation errors appear and the form does not navigate away

**Pass criteria:** Errors shown, URL still contains "create".

---

## Scenario 5: Client messages page

1. Log in as CLIENT
2. Navigate to http://localhost:3001/client/messages
3. Take a screenshot
4. Verify: page loads (conversation list or empty state visible)

**Pass criteria:** Messages page renders without crash.

---

## Scenario 6: Client profile page

1. Log in as CLIENT
2. Navigate to http://localhost:3001/client/profile
3. Take a screenshot
4. Verify: profile form or profile info is displayed

**Pass criteria:** Profile page renders.
