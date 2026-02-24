const fs = require('fs');
const path = require('path');

const dir = 'C:/webshoppe/Web_Shopee_Lab211_Group2/src/Frontend';
const currentFileName = path.basename(__filename);

function getAllFiles(dirPath, arrayOfFiles) {
    const files = fs.readdirSync(dirPath);
    arrayOfFiles = arrayOfFiles || [];

    files.forEach(function (file) {
        if (fs.statSync(dirPath + "/" + file).isDirectory()) {
            arrayOfFiles = getAllFiles(dirPath + "/" + file, arrayOfFiles);
        } else {
            if ((file.endsWith('.html') || file.endsWith('.js')) && file !== currentFileName) {
                arrayOfFiles.push(path.join(dirPath, file));
            }
        }
    });

    return arrayOfFiles;
}

const files = getAllFiles(dir);
let patchedCount = 0;

for (const file of files) {
    let content = fs.readFileSync(file, 'utf-8');
    let original = content;

    // Phase 1: Remove global declarations
    content = content.replace(/const\s+accessToken\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');
    content = content.replace(/let\s+accessToken\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');
    content = content.replace(/var\s+accessToken\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');

    content = content.replace(/const\s+token\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');
    content = content.replace(/let\s+token\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');
    content = content.replace(/var\s+token\s*=\s*localStorage\.getItem\(['"]accessToken['"]\);?/g, '');

    // Phase 2: Replace usages in strings and template literals
    content = content.replace(/\$\{accessToken\}/g, '${localStorage.getItem("accessToken")}');
    content = content.replace(/\$\{token\}/g, '${localStorage.getItem("accessToken")}');

    // Phase 3: Replace usages in Fetch headers
    content = content.replace(/Bearer '\s*\+\s*accessToken/g, "Bearer ' + localStorage.getItem('accessToken')");
    content = content.replace(/Bearer '\s*\+\s*token/g, "Bearer ' + localStorage.getItem('accessToken')");
    content = content.replace(/`Bearer `\s*\+\s*accessToken/g, "`Bearer ` + localStorage.getItem('accessToken')");
    content = content.replace(/`Bearer `\s*\+\s*token/g, "`Bearer ` + localStorage.getItem('accessToken')");
    content = content.replace(/"Bearer "\s*\+\s*accessToken/g, "\"Bearer \" + localStorage.getItem('accessToken')");
    content = content.replace(/"Bearer "\s*\+\s*token/g, "\"Bearer \" + localStorage.getItem('accessToken')");

    // Phase 4: Truthy/Falsy/Conditional checks (More aggressive)
    // Avoid replacing inside strings or when it's a property (e.g., .token)
    // Matches: if (token), !token, token &&, && token, || token, token ||, (token), ? token :, : token
    const replaceToken = (str) => {
        // Replace 'accessToken' and 'token' only as standalone words
        // We use lookbehind trickery with regex if possible, but JS regex lookbehind support varies.
        // Safer way: search for patterns.

        const patterns = [
            [/if\s*\(([^)]*)\btoken\b([^)]*)\)/g, 'if ($1localStorage.getItem("accessToken")$2)'],
            [/if\s*\(([^)]*)\baccessToken\b([^)]*)\)/g, 'if ($1localStorage.getItem("accessToken")$2)'],
            [/\!\s*\btoken\b/g, '!localStorage.getItem("accessToken")'],
            [/\!\s*\baccessToken\b/g, '!localStorage.getItem("accessToken")'],
            [/\btoken\s+&&/g, 'localStorage.getItem("accessToken") &&'],
            [/\baccessToken\s+&&/g, 'localStorage.getItem("accessToken") &&'],
            [/&&\s+token\b/g, '&& localStorage.getItem("accessToken")'],
            [/&&\s+accessToken\b/g, '&& localStorage.getItem("accessToken")'],
            [/\btoken\s+\|\|/g, 'localStorage.getItem("accessToken") ||'],
            [/\baccessToken\s+\|\|/g, 'localStorage.getItem("accessToken") ||'],
            [/\|\|\s+token\b/g, '|| localStorage.getItem("accessToken")'],
            [/\|\|\s+accessToken\b/g, '|| localStorage.getItem("accessToken")'],
            [/\?\s*\btoken\b/g, '? localStorage.getItem("accessToken")'],
            [/\?\s*\baccessToken\b/g, '? localStorage.getItem("accessToken")'],
            [/:\s*\btoken\b/g, ': localStorage.getItem("accessToken")'],
            [/:\s*\baccessToken\b/g, ': localStorage.getItem("accessToken")'],
            [/\(\s*\btoken\b/g, '(localStorage.getItem("accessToken")'],
            [/\(\s*\baccessToken\b/g, '(localStorage.getItem("accessToken")'],
            [/\btoken\s*\)/g, 'localStorage.getItem("accessToken"))'], // Wait, this might double closing bracket if not careful.
            // Actually, let's stick to simpler ones or use a better parser-like approach.
        ];

        let s = str;
        // Basic standalone replacements that are likely safe
        s = s.replace(/\btoken &&/g, 'localStorage.getItem("accessToken") &&');
        s = s.replace(/\baccessToken &&/g, 'localStorage.getItem("accessToken") &&');
        s = s.replace(/&& token\b/g, '&& localStorage.getItem("accessToken")');
        s = s.replace(/&& accessToken\b/g, '&& localStorage.getItem("accessToken")');

        s = s.replace(/\btoken \|\|/g, 'localStorage.getItem("accessToken") ||');
        s = s.replace(/\baccessToken \|\|/g, 'localStorage.getItem("accessToken") ||');
        s = s.replace(/\|\| token\b/g, '|| localStorage.getItem("accessToken")');
        s = s.replace(/\|\| accessToken\b/g, '|| localStorage.getItem("accessToken")');

        s = s.replace(/if\s*\(!token\)/g, 'if (!localStorage.getItem("accessToken"))');
        s = s.replace(/if\s*\(!accessToken\)/g, 'if (!localStorage.getItem("accessToken"))');
        s = s.replace(/if\s*\(token\)/g, 'if (localStorage.getItem("accessToken"))');
        s = s.replace(/if\s*\(accessToken\)/g, 'if (localStorage.getItem("accessToken"))');

        s = s.replace(/\? accessToken :/g, '? localStorage.getItem("accessToken") :');
        s = s.replace(/\? token :/g, '? localStorage.getItem("accessToken") :');
        s = s.replace(/: accessToken/g, ': localStorage.getItem("accessToken")');
        s = s.replace(/: token/g, ': localStorage.getItem("accessToken")');

        return s;
    };

    content = replaceToken(content);

    // Phase 5: Function arguments (Calls, not declarations)
    // We already fixed declarations manually for a few files.
    // Let's be careful not to break declarations here.
    content = content.replace(/\(accessToken\s*,\s*/g, '(localStorage.getItem("accessToken"), ');
    content = content.replace(/\(token\s*,\s*/g, '(localStorage.getItem("accessToken"), ');
    content = content.replace(/,\s*accessToken\s*\)/g, ', localStorage.getItem("accessToken"))');
    content = content.replace(/,\s*token\s*\)/g, ', localStorage.getItem("accessToken"))');

    // Exact match for (token) or (accessToken) as call arg
    content = content.replace(/\((token|accessToken)\)/g, '(localStorage.getItem("accessToken"))');

    if (content !== original) {
        fs.writeFileSync(file, content, 'utf-8');
        patchedCount++;
        console.log(`Patched: ${file}`);
    }
}

console.log(`Done! Patched ${patchedCount} files.`);
