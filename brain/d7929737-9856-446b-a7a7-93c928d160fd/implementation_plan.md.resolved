# Implementation Plan - Authentication Flow

The goal is to implement a functional authentication flow where clicking the user icon navigates to the Login page, which then links to a "Create Account" (Register) page. The designs provided will be matched.

## User Review Required
> [!IMPORTANT]
> The current User Icon dropdown menu in the Header will be replaced with a direct link to the Login page as requested.

## Proposed Changes

### Components Layer

#### [MODIFY] [Header.tsx](file:///c:/Users/HPPAVILION/Documents/Shop_nextJs/my-app/components/common/Header.tsx)
- Change the User icon button to wrap in a `Link` to `/login` instead of `DropdownMenuTrigger`.
- (Optional) Remove the `DropdownMenu` import if no longer used, or keep it if we plan to use it for logged-in state later (for now, I will simplify to direct link as per request).

### Pages Layer

#### [MODIFY] [login/page.tsx](file:///c:/Users/HPPAVILION/Documents/Shop_nextJs/my-app/app/(auth)/login/page.tsx)
- Implement a centered login form.
- **UI Elements**:
    - Logo (URBANNEST)
    - Heading: "WELCOME BACK"
    - Subtext: "Enter your details to access your account."
    - Inputs: Email Address, Password
    - "Forgot Password?" link
    - Button: "SIGN IN" (Black background)
    - Divider: "OR CONTINUE WITH"
    - Social Buttons: Google, Apple
    - Footer Link: "New here? Create account" -> Links to `/register`

#### [MODIFY] [register/page.tsx](file:///c:/Users/HPPAVILION/Documents/Shop_nextJs/my-app/app/(auth)/register/page.tsx)
- Implement a split-layout registration page (Form Left, Image Right).
- **UI Elements**:
    - Header: Log In link (top right)
    - Heading: "JOIN THE NEST"
    - Subtext: "Secure your fit. Create an account..."
    - Inputs: Full Name, Email Address, Create Password
    - Checkbox: Newsletter signup
    - Button: "CREATE ACCOUNT" (Black background)
    - Footer Link: "Already have an account? Log In" -> Links to `/login`
    - Right side: Image content (Sneaker/Product image).

## Verification Plan

### Manual Verification
1.  **Navigation**:
    - Click User Icon -> Verify navigation to `/login`.
    - Click "Create account" on Login page -> Verify navigation to `/register`.
    - Click "Log In" on Register page -> Verify navigation to `/login`.
2.  **UI Check**:
    - Verify Login page matches the "minimalist centered" design.
    - Verify Register page matches the "split layout" design.
3.  **Responsiveness**:
    - Check mobile view for both pages (likely stacking the split view on register).
