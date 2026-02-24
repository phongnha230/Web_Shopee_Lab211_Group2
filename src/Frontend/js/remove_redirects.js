const fs = require('fs');
const path = require('path');

const dir = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend';

function walk(directory) {
    let results = [];
    const list = fs.readdirSync(directory);
    list.forEach(file => {
        file = path.join(directory, file);
        const stat = fs.statSync(file);
        if (stat && stat.isDirectory() && !file.includes('node_modules') && !file.includes('backups')) {
            results = results.concat(walk(file));
        } else if (file.endsWith('.html') || file.endsWith('.js')) {
            results.push(file);
        }
    });
    return results;
}

const allFiles = walk(dir);

let modifiedCount = 0;

for (const file of allFiles) {
    // Skip the interceptor script itself since it contains the valid redirect
    if (file.includes('inject_interceptor.js')) continue;

    let content = fs.readFileSync(file, 'utf-8');
    let original = content;

    // Remove window.location.href = 'login.html' or similar (but not in navigation links)
    // Be careful with JS strings, we only want to remove it when it's an assignment statement

    // We will target specific patterns commonly used for auth failures:
    // 1. window.location.href = 'login.html'
    // 2. window.location.href = '/login.html'
    // 3. window.location.replace('login.html...')
    // We'll replace them with a console.log so we don't break JS syntax if it's the only statement in an if block without braces.

    content = content.replace(/window\.location\.href\s*=\s*['"`]\/?login\.html.*?['"`]\s*;/g, "console.warn('Redirect prevented by script');");
    content = content.replace(/window\.location\.replace\(.*?[`'"]\/?login\.html.*?[`'"].*?\)\s*;/g, "console.warn('Replace prevented by script');");

    // Sometimes it's without a semicolon
    content = content.replace(/if\s*\(!token\)\s*window\.location\.href\s*=\s*['"`]\/?login\.html['"`]/g, "if (!localStorage.getItem("accessToken")) console.warn('Missing token');");

    if (content !== original) {
        fs.writeFileSync(file, content, 'utf-8');
        console.log('Modified:', file);
        modifiedCount++;
    }
}

console.log('Removed hardcoded redirects from ' + modifiedCount + ' files.');
