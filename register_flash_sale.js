
const BASE_URL = 'http://localhost:8080/api';

async function main() {
    try {
        console.log("🚀 Starting Flash Sale Registration Flow...");

        // 1. Admin Login
        console.log("\n🔑 Logging in as Admin...");
        let adminToken = await login('admin@shopee.com', 'admin123');
        console.log("✅ Admin Logged In");

        // 2. Create Campaign
        console.log("\n⚡ Creating Flash Sale Campaign...");
        const campaign = await createCampaign(adminToken);
        console.log(`✅ Campaign Created: ${campaign.name} (${campaign.id})`);

        // 3. Create Slot
        console.log("\n⏰ Creating Time Slot...");
        const slot = await createSlot(adminToken, campaign.id);
        console.log(`✅ Slot Created: ${slot.id}`);

        // 4. Register Seller User
        const timestamp = Date.now();
        const sellerUser = `seller_${timestamp}`;
        const sellerEmail = `seller_${timestamp}@test.com`;
        console.log(`\n👤 Registering new Seller: ${sellerUser} (${sellerEmail})...`);
        await registerUser(sellerUser, 'password', sellerEmail);
        console.log("✅ Seller Registered");

        // 5. Seller Login
        console.log("\n🔑 Logging in as Seller...");
        let sellerToken = await login(sellerUser, 'password');
        console.log("✅ Seller Logged In");

        // 6. Register Shop
        const shopName = `Shop_${timestamp}`;
        console.log(`\n🏪 Registering Shop: ${shopName}...`);
        const shop = await registerShop(sellerToken, shopName, sellerEmail); // Pass email here
        console.log(`✅ Shop Registered: ${shop.name} (Status: ${shop.status || 'PENDING'})`);

        // 7. Approve Shop (as Admin)
        console.log("\n👑 Approving Shop...");
        // Need to find shop ID first.
        const pendingShops = await getPendingShops(adminToken);
        const myShop = pendingShops.find(s => s.name === shopName);
        if (myShop) {
            await approveShop(adminToken, myShop.id);
            console.log(`✅ Shop Approved: ${myShop.id}`);
        } else {
            console.log("⚠️ Shop auto-approved or not found in pending list. (Check status)");
        }

        // 8. Add Product (as Seller)
        console.log("\n📦 Adding Product...");
        // Re-login to refresh token roles to SELLER (after shop approval)
        sellerToken = await login(sellerUser, 'password');
        const product = await createProduct(sellerToken);
        console.log(`✅ Product Created: ${product.name} (${product.id})`);

        // 9. Register for Flash Sale
        console.log("\n📝 Registering Product for Flash Sale...");
        // Need variant ID - let's fetch variants to be safe
        const variants = await getVariants(sellerToken, product.id);
        const variantId = variants.length > 0 ? variants[0].id : null;

        await registerFlashSale(sellerToken, slot.id, product.id, variantId, 50000, 10);
        console.log("✅ Registration Submitted");

        // 10. Approve Registration (as Admin)
        console.log("\n👑 Approving Registration...");
        const pendingRegs = await getPendingRegistrations(adminToken);
        // Find our registration
        const myReg = pendingRegs.find(r => r.productId === product.id);
        if (myReg) {
            await approveRegistration(adminToken, myReg.id);
            console.log(`✅ Registration Approved: ${myReg.id}`);
        } else {
            console.log("⚠️ Registration not found in pending list (or already approved).");
        }

        console.log("\n🎉 FLOW COMPLETED SUCCESSFULLY!");

    } catch (error) {
        console.error("\n❌ ERROR:", error.message);
        if (error.cause) console.error("Cause:", error.cause);
    }
}

// --- HELPER FUNCTIONS ---

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    const text = await res.text();
    if (!res.ok) {
        throw new Error(`Request to ${url} failed: ${res.status} ${text}`, { cause: text });
    }
    return text ? JSON.parse(text) : {};
}

async function login(username, password) {
    const res = await fetchJson(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: username, password })
    });
    return res.accessToken;
}

async function registerUser(username, password, email) {
    return await fetchJson(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email, fullName: 'Test Seller', phone: '0909090909' })
    });
}

async function createCampaign(token) {
    const now = new Date();
    // Start now, End +1 hour
    const start = new Date(now.getTime());
    const end = new Date(now.getTime() + 3600000);
    const regDeadline = new Date(now.getTime() + 1800000); // +30m

    return await fetchJson(`${BASE_URL}/flash-sales/campaigns`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name: `Auto Campaign ${Date.now()}`,
            description: "Automated Test Campaign",
            startDate: start.toISOString(),
            endDate: end.toISOString(),
            minDiscountPercentage: 10,
            minStockPerProduct: 5,
            registrationDeadline: regDeadline.toISOString(),
            approvalDeadline: regDeadline.toISOString()
        })
    });
}

async function createSlot(token, campaignId) {
    const now = new Date();
    const start = new Date(now.getTime() + 60000); // Start in 1 min
    const end = new Date(now.getTime() + 3500000);
    return await fetchJson(`${BASE_URL}/flash-sales/slots`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            campaignId,
            startTime: start.toISOString(),
            endTime: end.toISOString()
        })
    });
}

async function registerShop(token, name, email) {
    return await fetchJson(`${BASE_URL}/shop/register`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name,
            address: "123 Test St, Ward, District, City",
            description: "Test Shop",
            ownerEmail: email,
            ownerPhone: "0123456789"
        })
    });
}

async function getPendingShops(token) {
    // CORRECTED ENDPOINT
    return await fetchJson(`${BASE_URL}/shop/admin/pending`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
}

async function approveShop(token, id) {
    // CORRECTED ENDPOINT
    return await fetchJson(`${BASE_URL}/shop/admin/approve/${id}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
}

async function createProduct(token) {
    // Need category ID -> fetch categories first
    const catsBuffer = await fetch(`${BASE_URL}/categories`, { headers: { 'Authorization': `Bearer ${token}` } });
    let catId = 1;
    if (catsBuffer.ok) {
        const cats = await catsBuffer.json();
        if (cats.length > 0) catId = cats[0].id;
    }

    return await fetchJson(`${BASE_URL}/products`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name: `Test Product ${Date.now()}`,
            description: "Test Desc",
            price: 100000,
            stock: 100,
            categoryId: catId,
            images: ["https://via.placeholder.com/150"]
        })
    });
}

async function getVariants(token, productId) {
    const res = await fetch(`${BASE_URL}/products/${productId}/variants`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (res.ok) return await res.json();
    return [];
}

async function registerFlashSale(token, flashSaleId, productId, variantId, salePrice, saleStock) {
    // CORRECTED ENDPOINT
    return await fetchJson(`${BASE_URL}/flash-sales/registrations`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            flashSaleId,
            productId,
            variantId, // can be null
            // ShopId is inferred from token in Controller: shopService.getMyShop(auth.getName())
            salePrice,
            saleStock
        })
    });
}

async function getPendingRegistrations(token) {
    return await fetchJson(`${BASE_URL}/flash-sales/registrations/status/PENDING`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
}

async function approveRegistration(token, id) {
    return await fetchJson(`${BASE_URL}/flash-sales/registrations/${id}/approve`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'APPROVED', adminNote: "Auto Approved" })
    });
}

main();
