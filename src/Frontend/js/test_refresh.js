const testLoginAndRefresh = async () => {
    try {
        console.log("1. Logging in...");
        const loginRes = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'admin@webshoppe.com', password: 'admin' }) // Guessing standard admin or we can use another email. Let's see if this works.
        });

        let data = await loginRes.json();
        if (!loginRes.ok) {
            console.log("No default admin user to test. Fallback: looking for users...");
            return;
        }
        console.log("Login success! AccessToken ends with:", data.accessToken.slice(-10));
        console.log("RefreshToken ends with:", data.refreshToken.slice(-10));

        console.log("\n2. Trying to refresh token...");
        const refreshRes = await fetch('http://localhost:8080/api/auth/refresh-token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Refresh-Token': data.refreshToken }
        });

        if (refreshRes.ok) {
            const refreshData = await refreshRes.json();
            console.log("Refresh Success!");
            console.log("NEW AccessToken ends with:", refreshData.accessToken.slice(-10));
            console.log("NEW RefreshToken ends with:", refreshData.refreshToken.slice(-10));
        } else {
            console.log("Refresh Failed!", refreshRes.status, await refreshRes.text());
        }
    } catch (e) {
        console.error("Test failed", e);
    }
};
testLoginAndRefresh();
