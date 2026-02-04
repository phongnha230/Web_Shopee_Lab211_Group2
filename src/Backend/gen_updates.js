const fs = require('fs');

const products = JSON.parse(fs.readFileSync('products.json', 'utf8'));
const categories = JSON.parse(fs.readFileSync('categories.json', 'utf8'));

const catMap = {};
categories.forEach(c => catMap[c.name] = c.id);

const nameToCat = {
    // Electronics
    'Nikon': 'Electronics',
    'Camera': 'Electronics',
    'Watt': 'Electronics',
    'Headphone': 'Electronics', // NEW
    'Speaker': 'Electronics',   // NEW
    'Wireless': 'Electronics',  // NEW
    'Bluetooth': 'Electronics', // NEW

    // Fashion
    'Nike': 'Fashion',
    'Sneaker': 'Fashion',
    'Urban': 'Fashion',
    'Shirt': 'Fashion',
    'Dress': 'Fashion',
    'Jeans': 'Fashion',
    'Cloth': 'Fashion',
    'Pant': 'Fashion',

    // Mobile
    'Phone': 'Mobile & Gadgets',
    'Samsung': 'Mobile & Gadgets',
    'iPhone': 'Mobile & Gadgets',
    'Xiaomi': 'Mobile & Gadgets',

    // Home
    'Chair': 'Home',
    'Table': 'Home',
    'Bed': 'Home',
    'Lamp': 'Home',

    // Beauty
    'Lipstick': 'Beauty',
    'Mascara': 'Beauty',
    'Cream': 'Beauty',
    'Skin': 'Beauty',
    'Face': 'Beauty',
    'Hada': 'Beauty',       // NEW
    'Labo': 'Beauty',       // NEW
    'Cleanser': 'Beauty',   // NEW
    'Moisturizing': 'Beauty', // NEW

    // Sports
    'Bottle': 'Sports',
    'Yoga': 'Sports',
    'Gym': 'Sports',
    'Mat': 'Sports',

    // Baby
    'Toy': 'Baby & Toys',
    'Baby': 'Baby & Toys',
    'Diaper': 'Baby & Toys',
    'Rabbit': 'Baby & Toys', // For JELLYCAT

    // Food
    'Food': 'Food',
    'Snack': 'Food',
    'Drink': 'Food',
    'Tea': 'Food',
    'Orange': 'Food', // "My Sweet Orange Tree" -> Book? or Food? 

    // Books
    'Book': 'Books',
    'Novel': 'Books',
    'Tree': 'Books' // "My Sweet Orange Tree" is a book
};

const commands = [];

products.forEach(p => {
    let matched = false;
    for (const [key, catName] of Object.entries(nameToCat)) {
        if (p.name && p.name.includes(key)) {
            const catId = catMap[catName];
            if (catId) {
                // Add category
                commands.push(`Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/products/${p.id}/categories/${catId}"`);
                matched = true;
            }
        }
    }
});

console.log(commands.join('\n'));
