// removed axios
async function test() {
    try {
        console.log("Logging in as user@gmail.com / 123456");
        const loginRes = await fetch('http://localhost:8088/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'user@gmail.com', password: '1' })
        });

        let data = await loginRes.json();
        if (!loginRes.ok) {
            console.log("Login failed", loginRes.status, data);

            // Try another common user
            console.log("Trying admin@example.com / admin123");
            const loginRes2 = await fetch('http://localhost:8088/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: 'admin@webshoppe.com', password: '1' })
            });
            data = await loginRes2.json();
            if (!loginRes2.ok) {
                console.log("Login 2 failed", data);
                return;
            }
        }

        console.log("We have a valid refresh token:", data.refreshToken ? "YES" : "NO");
        if (data.refreshToken) {
            console.log("Requesting refresh...");
            const refreshRes = await fetch('http://localhost:8088/api/auth/refresh-token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Refresh-Token': data.refreshToken
                }
            });
            const text = await refreshRes.text();
            console.log("Refresh Response Code:", refreshRes.status);
            console.log("Refresh Response Body:", text);
        }
    } catch (e) {
        console.error("Error", e);
    }
}
test();
