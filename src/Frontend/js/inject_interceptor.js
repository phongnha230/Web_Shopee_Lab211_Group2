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
                
                // If unauthorized or forbidden (Spring defaults to 403 for expired tokens)
                if ((response.status === 401 || response.status === 403) && typeof args[0] === 'string' && !args[0].includes('/auth/login') && !args[0].includes('/auth/refresh-token')) {
                    const refreshToken = localStorage.getItem('refreshToken');
                    if (refreshToken) {
                        if (!isRefreshing) {
                            isRefreshing = true;
                            refreshPromise = (async () => {
                                try {
                                    const refreshResponse = await originalFetch('http://localhost:8080/api/auth/refresh-token', {
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
                                        const errText = await refreshResponse.text();
                                        
                                        // HIỆN BẢNG LỖI TO ĐÙNG, KHÔNG CHO REDIRECT
                                        const div = document.createElement('div');
                                        div.style.cssText = 'position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.95); color:white; z-index:9999999; display:flex; flex-direction:column; justify-content:center; align-items:center; padding: 20px; text-align:center; font-family: monospace; overflow-y:auto;';
                                        div.innerHTML = '<h1 style="color:#ff4444; font-size:30px; margin-bottom:20px;">LỖI TỪ BACKEND KHI GIA HẠN TOKEN</h1><p style="font-size:20px; margin-bottom:10px;">HTTP Status Code: <b style="color:yellow">' + refreshResponse.status + '</b></p><p style="margin-bottom:10px;">Vui lòng chụp màn hình cái bảng này đưa cho AI xem nha!</p><textarea readonly style="width:80%; max-width:800px; height:400px; background:#222; color:#00ff00; padding:15px; border:2px solid red; border-radius:10px; font-size:16px;">' + errText + '</textarea>';
                                        document.body.appendChild(div);

                                        localStorage.removeItem('accessToken');
                                        localStorage.removeItem('refreshToken');
                                        localStorage.removeItem('user');
                                        
                                        // NEVER RESOLVE SO OTHER SCRIPTS HANG FOREVER
                                        return new Promise(() => {});
                                    }
                                } catch (e) {
                                    console.error('Error during token refresh', e);
                                    return new Promise(() => {});
                                } finally {
                                    isRefreshing = false;
                                }
                            })();
                        }

                        try {
                            const newAccessToken = await refreshPromise;
                            // If it hangs, we never reach here
                            
                            // Retry the original request with the new token
                            if (args[1]) {
                                args[1].headers = args[1].headers || {};
                                if (args[1].headers instanceof Headers) {
                                    args[1].headers.set('Authorization', 'Bearer ' + newAccessToken);
                                } else {
                                    args[1].headers['Authorization'] = 'Bearer ' + newAccessToken;
                                }
                            } else {
                                args[1] = {
                                    headers: {
                                        'Authorization': 'Bearer ' + newAccessToken
                                    }
                                };
                            }
                            
                            // Return the retried request
                            return originalFetch(...args);
                        } catch (e) {
                            // If promise rejects? We made it never resolve/reject on error, so it hangs.
                            return new Promise(() => {});
                        }
                    } else {
                        // No refresh token available
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('user');
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

console.log('Injected stalling interceptor into ' + count + ' files.');
