const fs = require('fs');
const path = require('path');

const dir = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend';
const file = path.join(dir, 'profile.html');
let content = fs.readFileSync(file, 'utf-8');

// The regex will find the script block and button injected by inject_debug_btn.js
const regex = /<!-- ONE-CLICK DEBUG BUTTON -->[\s\S]*?<\/script>/g;

if (content.match(regex)) {
    content = content.replace(regex, '');
    fs.writeFileSync(file, content, 'utf-8');
    console.log("Successfully removed the malicious TEST REFRESH TOKEN button from profile.html");
} else {
    console.log("TEST REFRESH TOKEN button already removed or not found.");
}
