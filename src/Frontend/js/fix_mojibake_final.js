const fs = require('fs');
const path = 'src/Frontend/index.html';

try {
    let content = fs.readFileSync(path, 'utf8');

    // Exact strings from file view
    const replacements = {
        'Háº¾T HĂ€NG': 'HẾT HÀNG',
        'Ä Ăƒ BĂ N': 'ĐÃ BÁN',
        'Sáº®P CHĂ Y HĂ€NG': 'SẮP CHÁY HÀNG',
        'Ä Ă£ bĂ¡n': 'Đã bán'
    };

    let newContent = content;
    let changed = false;

    for (const [bad, good] of Object.entries(replacements)) {
        if (newContent.includes(bad)) {
            // Using split/join for global replacement without regex issues
            newContent = newContent.split(bad).join(good);
            changed = true;
            console.log(`Replaced: ${bad} -> ${good}`);
        }
    }

    if (changed) {
        fs.writeFileSync(path, newContent, 'utf8');
        console.log('Successfully fixed Mojibake in index.html');
    } else {
        console.log('No Mojibake found in index.html');
    }
} catch (err) {
    console.error('Error:', err);
}
