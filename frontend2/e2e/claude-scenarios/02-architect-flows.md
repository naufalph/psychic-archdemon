# Architect Flow Scenarios

Prerequisites:
- Both frontend (localhost:3001) and backend (localhost:8080) must be running
- Test architect account: test.architect1@rumantra.com / password123

---

## Scenario 1: Architect dashboard renders

1. Log in as ARCHITECT via API (POST http://localhost:8080/rmtr/users/login)
2. Set auth_token in localStorage
3. Navigate to http://localhost:3001/architect/dashboard
4. Take a screenshot
5. Verify: dashboard content is visible — may show bid quota, active projects, or a profile completion nudge
6. Verify: navigation links to Opportunities, Portfolios, My Bids are present

**Pass criteria:** Dashboard loads, nav links present.

---

## Scenario 2: Opportunity list

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/opportunities
3. Take a screenshot
4. Verify: page loads — either project cards are shown (open projects to bid on) or empty state
5. If cards are visible: click on one and verify the project detail page opens

**Pass criteria:** Page renders. If projects exist, clicking a card opens detail.

---

## Scenario 3: Portfolio page

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/portfolios
3. Take a screenshot
4. Verify: page loads with portfolio grid or empty state
5. Verify: "Add Portfolio" or "Create" button is visible
6. Click the add button
7. Take a screenshot — should open a form or modal to create a new portfolio

**Pass criteria:** Portfolio page loads, add button opens creation UI.

---

## Scenario 4: My Bids page

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/bids
3. Take a screenshot
4. Verify: page renders — shows bid list (active, pending, accepted) or empty state

**Pass criteria:** Bids page renders without crash.

---

## Scenario 5: Architect profile page

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/profile
3. Take a screenshot
4. Verify: profile form is visible with fields like bio, specialization, experience

**Pass criteria:** Profile form renders.

---

## Scenario 6: Bid quota / token display

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/dashboard
3. Look for bid token / quota display
4. Take a screenshot highlighting the token area
5. Verify: token count is a number (0 or more), not "null" or "undefined"

**Pass criteria:** Token count renders correctly.

---

## Scenario 7: Token purchase modal

1. Log in as ARCHITECT
2. Navigate to http://localhost:3001/architect/dashboard
3. Find and click the "Buy Tokens" or "Purchase" button
4. Take a screenshot
5. Verify: a modal or drawer opens with token purchase options and pricing

**Pass criteria:** Token purchase UI opens without error.
