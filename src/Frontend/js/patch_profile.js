const fs = require('fs');
const path = require('path');

const targetFile = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend/profile.html';
let content = fs.readFileSync(targetFile, 'utf-8');

// Remove the global let declaration
content = content.replace("", "");

// Replace 'if (!localStorage.getItem("accessToken"))' with 'if (!localStorage.getItem("accessToken"))'
content = content.replace(/if \(!accessToken\)/g, 'if (!localStorage.getItem("accessToken"))');

// Replace 'Bearer ${localStorage.getItem("accessToken")}' with 'Bearer ${localStorage.getItem("accessToken")}'
content = content.replace(/Bearer \${localStorage.getItem("accessToken")}/g, 'Bearer ${localStorage.getItem("accessToken")}');

fs.writeFileSync(targetFile, content, 'utf-8');
console.log('Successfully patched profile.html to fetch tokens dynamically instead of using cached variables.');
