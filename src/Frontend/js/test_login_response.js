async function test() {
    try {
        const email = 'test' + Date.now() + '@example.com';
        const password = 'Password@123';
        console.log("Registering newly created user:", email);
        let res = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, fullName: 'Test Name' })
        });
        console.log("Register response:", res.status, await res.text());

        console.log("Logging in via API...");
        res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'lenguyenanhmai05@gmail.com', password: '1' })
        });

        const data = await res.json();
        console.log("Login HTTP Status:", res.status);
        console.log("Access Token exists?", !!data.accessToken);
        console.log("Refresh Token exists?", !!data.refreshToken);
        if (data.refreshToken) {
            console.log("Refresh Token value:", data.refreshToken.substring(0, 20) + "...");
        } else {
            console.log("ENTIRE LOGIN RESPONSE:", data);
        }
    } catch (e) {
        console.error("Test failed", e);
    }
}
test();
