const email = 'testuser' + Date.now() + '@example.com';
const password = 'Password@123';

async function test() {
    console.log("1. Registering...");
    const regRes = await fetch('http://localhost:8088/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, fullName: 'Test User' })
    });
    console.log("Reg result:", regRes.status, await regRes.text());

    // Attempting login directly. Wait, if they need OTP verification, maybe we can't login directly!
    // But let's try.
    const loginRes = await fetch('http://localhost:8088/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    const data = await loginRes.json();
    console.log("\n2. Login status:", loginRes.status);
    if (!loginRes.ok) {
        console.log("Login failed:", data);
        return;
    }

    console.log("Login Success! Refresh Token ending in:", data.refreshToken.slice(-10));

    console.log("\n3. Testing Refresh Endpoint...");
    const refreshRes = await fetch('http://localhost:8088/api/auth/refresh-token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Refresh-Token': data.refreshToken
        }
    });

    if (refreshRes.ok) {
        const refreshData = await refreshRes.json();
        console.log("Refresh Success! NEW Access Token ending in:", refreshData.accessToken.slice(-10));
    } else {
        console.log("Refresh Failed!", refreshRes.status, await refreshRes.text());
    }
}
test();
