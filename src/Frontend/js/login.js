const API_URL = "http://localhost:8088/api";

document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: document.getElementById("email").value,
            password: document.getElementById("password").value,
        }),
    })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.message || "Login failed");
            }
            return data;
        })
        .then(data => {
            if (data.accessToken) localStorage.setItem("accessToken", data.accessToken);
            if (data.refreshToken) localStorage.setItem("refreshToken", data.refreshToken);
            if (data.user) localStorage.setItem("user", JSON.stringify(data.user));
            window.location.href = "index.html";
        })
        .catch(err => {
            console.error(err);
            alert("Login error: " + err.message);
        });
});

function showForgot() {
    document.getElementById("forgot").style.display = "block";
}

function forgotPassword() {
    window.location.href = "forgot-password.html";
}
