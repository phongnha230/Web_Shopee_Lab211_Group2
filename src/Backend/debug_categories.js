async function checkCategories() {
    try {
        const response = await fetch('http://localhost:8080/api/categories');
        const categories = await response.json();

        if (categories.length > 0) {
            console.log('First category keys:', Object.keys(categories[0]));
            // Also log keys of a few others to be sure
            console.log('First category sample:', JSON.stringify(categories[0], null, 2));
        } else {
            console.log('No categories found.');
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
    }
}

checkCategories();
