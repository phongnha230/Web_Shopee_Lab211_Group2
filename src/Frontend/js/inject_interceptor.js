const fs = require('fs');
const path = require('path');

const dir = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend';
const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));

const interceptorScript = `
    <!-- Global Fetch Interceptor for Token Refresh -->
    <script>
        (function() {
            const originalFetch = window.fetch;
            let isRefreshing = false;
            let refreshPromise = null;

            window.fetch = async function(...args) {
                let response;
                try {
                    response = await originalFetch(...args);
                } catch (error) {
                    throw error;
                }
                
                if (response.status === 401 && typeof args[0] === 'string' && !args[0].includes('/auth/login') && !args[0].includes('/auth/refresh-token')) {
                    const refreshToken = localStorage.getItem('refreshToken');
                    if (refreshToken) {
                        if (!isRefreshing) {
                            isRefreshing = true;
                            refreshPromise = (async () => {
                                try {
                                    const refreshResponse = await originalFetch('http://localhost:8088/api/auth/refresh-token', {
                                        method: 'POST',
                                        headers: {
                                            'Content-Type': 'application/json',
                                            'Refresh-Token': refreshToken
                                        }
                                    });
                                    
                                    if (refreshResponse.ok) {
                                        const data = await refreshResponse.json();
                                        localStorage.setItem('accessToken', data.accessToken);
                                        localStorage.setItem('refreshToken', data.refreshToken);
                                        return data.accessToken;
                                    } else {
                                        localStorage.clear();
                                        window.location.href = 'login.html';
                                        return new Promise(() => {});
                                    }
                                } catch (e) {
                                    localStorage.clear();
                                    window.location.href = 'login.html';
                                    return new Promise(() => {});
                                } finally {
                                    isRefreshing = false;
                                }
                            })();
                        }

                        try {
                            const newAccessToken = await refreshPromise;
                            if (args[1]) {
                                args[1].headers = args[1].headers || {};
                                if (args[1].headers instanceof Headers) {
                                    args[1].headers.set('Authorization', 'Bearer ' + newAccessToken);
                                } else {
                                    args[1].headers['Authorization'] = 'Bearer ' + newAccessToken;
                                }
                            }
                            return originalFetch(...args);
                        } catch (e) {
                            return new Promise(() => {});
                        }
                    } else {
                        localStorage.clear();
                        window.location.href = 'login.html';
                        return new Promise(() => {});
                    }
                }
                return response;
            };
        })();
    </script>
`;

let count = 0;
for (const file of files) {
    const filePath = path.join(dir, file);
    let content = fs.readFileSync(filePath, 'utf-8');

    // Remove old interceptor script
    const regex = /<!-- Global Fetch Interceptor for Token Refresh -->[\s\S]*?<\/script>/g;
    content = content.replace(regex, '');

    if (content.includes('<head>')) {
        content = content.replace('<head>', '<head>\n' + interceptorScript);
    } else if (content.match(/<body[^>]*>/)) {
        content = content.replace(/<body[^>]*>/, match => match + '\n' + interceptorScript);
    }

    fs.writeFileSync(filePath, content, 'utf-8');
    count++;
}

console.log('Injected non-stalling interceptor (401 only) into ' + count + ' files.');
