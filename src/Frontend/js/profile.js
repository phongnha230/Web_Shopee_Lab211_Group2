const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("accessToken");

// ================= LOAD PROFILE =================
fetch(`${API_URL}/user/profile`, {
    headers: {
        Authorization: `Bearer ${token}`,
    },
})
    .then(res => {
        if (res.status === 401) {
            logout();
        }
        return res.json();
    })
    .then(data => {
        document.getElementById("fullName").value = data.fullName || "";
        document.getElementById("phone").value = data.phone || "";
    });

// ================= UPDATE PROFILE =================
document.getElementById("profileForm").addEventListener("submit", function (e) {
    e.preventDefault();

    fetch(`${API_URL}/user/profile`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
            fullName: document.getElementById("fullName").value,
            phone: document.getElementById("phone").value,
        }),
    })
        .then(res => res.text())
        .then(msg => alert(msg));
});

// ================= CHANGE PASSWORD =================
document.getElementById("passwordForm").addEventListener("submit", function (e) {
    e.preventDefault();

    fetch(`${API_URL}/user/change-password`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
            currentPassword: document.getElementById("currentPassword").value,
            newPassword: document.getElementById("newPassword").value,
            confirmPassword: document.getElementById("confirmPassword").value,
        }),
    })
        .then(res => res.text())
        .then(msg => alert(msg));
});

// ================= LOGOUT =================
function logout() {
    localStorage.removeItem("accessToken");
    window.location.href = "login.html";
}
