// Test script to verify product has shopId
const API_BASE_URL = 'http://localhost:8080/api';

async function testProductShopId() {
    try {
        // Test with a known product ID
        const productId = 1; // Change this to test different products

        console.log(`Testing product ID: ${productId}`);

        const response = await fetch(`${API_BASE_URL}/products/${productId}`);
        if (!response.ok) {
            console.error('Failed to fetch product');
            return;
        }

        const product = await response.json();
        console.log('Product data:', product);
        console.log('shopId:', product.shopId);
        console.log('shopId type:', typeof product.shopId);
        console.log('shopId is valid:', product.shopId && product.shopId !== 'undefined' && product.shopId !== 'null');

        if (product.shopId) {
            // Try to fetch shop details
            const shopResponse = await fetch(`${API_BASE_URL}/shop/${product.shopId}`);
            if (shopResponse.ok) {
                const shop = await shopResponse.json();
                console.log('Shop data:', shop);
                console.log('Shop name:', shop.name);
            } else {
                console.error('Shop not found for shopId:', product.shopId);
            }
        } else {
            console.error('Product does not have shopId!');
        }

    } catch (error) {
        console.error('Error:', error);
    }
}

// Run the test
testProductShopId();
