const http = require('http');

function fetchData(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => resolve(JSON.parse(data)));
        }).on('error', reject);
    });
}

async function check() {
    try {
        const categories = await fetchData('http://localhost:8080/api/categories');
        const products = await fetchData('http://localhost:8080/api/products');

        const catMap = {};
        categories.forEach(c => catMap[c.id] = c.name);

        const result = {
            categories: {},
            orphaned: []
        };

        categories.forEach(c => {
            result.categories[c.name] = [];
        });

        products.forEach(p => {
            let hasValidCat = false;
            if (p.categories && p.categories.length > 0) {
                p.categories.forEach(cid => {
                    const cName = catMap[cid];
                    if (cName) {
                        result.categories[cName].push(p.name);
                        hasValidCat = true;
                    }
                });
            }
            if (!hasValidCat) {
                result.orphaned.push({ name: p.name, id: p.id });
            }
        });

        console.log(JSON.stringify(result, null, 2));

    } catch (e) {
        console.error('Error:', e.message);
    }
}

check();
