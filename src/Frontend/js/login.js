const API_URL = "http://localhost:8080/api";

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
        .then(res => res.json())
        .then(data => {
            localStorage.setItem("accessToken", data.accessToken);
            window.location.href = "index.html";
        });
});

function showForgot() {
    document.getElementById("forgot").style.display = "block";
}

function forgotPassword() {
    alert("Flow quên mật khẩu – backend sẽ làm sau 😅");
}
