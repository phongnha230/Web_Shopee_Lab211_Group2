const fs = require('fs');
const path = require('path');

const dir = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend';
const file = path.join(dir, 'profile.html');
let content = fs.readFileSync(file, 'utf-8');

const testBtn = `
<!-- ONE-CLICK DEBUG BUTTON -->
<button onclick="testRefreshDirectly()" style="position:fixed; top:10px; left:10px; z-index:9999999; padding:15px; background:blue; color:white; font-weight:bold; border-radius:10px;">TEST REFRESH TOKEN</button>
<script>
    async function testRefreshDirectly() {
        try {
            const token = localStorage.getItem('refreshToken');
            alert('Bắt đầu gửi Refresh Token: ' + token.substr(-10));
            const refreshResponse = await window.fetch('http://localhost:8080/api/auth/refresh-token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Refresh-Token': localStorage.getItem("accessToken")
                }
            });
            const text = await refreshResponse.text();
            alert('Kết quả từ Backend:\\nStatus: ' + refreshResponse.status + '\\nBody: ' + text);
        } catch (e) {
            alert('Lỗi khi fetch: ' + e.message);
        }
    }
</script>
`;

if (content.match(/<body[^>]*>/)) {
    content = content.replace(/<body[^>]*>/, match => match + '\n' + testBtn);
    fs.writeFileSync(file, content, 'utf-8');
    console.log("Injected blue TEST REFRESH TOKEN button into profile.html");
}
