// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Helper function to get auth headers
function getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
    };
}

// Helper function to handle API errors
async function handleResponse(response) {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'Network error' }));
        throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }
    // 204 No Content hoặc body rỗng - không parse JSON
    if (response.status === 204) return null;
    const text = await response.text();
    if (!text || text.trim() === '') return null;
    try {
        return JSON.parse(text);
    } catch {
        return null;
    }
}

// ======================
// CART API SERVICE
// ======================
const CartAPI = {
    // Get user's cart
    getCart: async () => {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Add item to cart
    addItem: async (variantId, quantity = 1) => {
        const response = await fetch(`${API_BASE_URL}/cart/add`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ variantId, quantity })
        });
        return handleResponse(response);
    },

    // Update cart item quantity
    updateQuantity: async (variantId, quantity) => {
        const response = await fetch(`${API_BASE_URL}/cart/update?variantId=${variantId}&quantity=${quantity}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Remove item from cart
    removeItem: async (variantId) => {
        const response = await fetch(`${API_BASE_URL}/cart/remove?variantId=${variantId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Clear entire cart
    clearCart: async () => {
        const response = await fetch(`${API_BASE_URL}/cart/clear`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// ORDER API SERVICE
// ======================
const OrderAPI = {
    // Create order from cart
    createOrder: async (orderData) => {
        const response = await fetch(`${API_BASE_URL}/orders`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(orderData)
        });
        return handleResponse(response);
    },

    // Get all user orders (with review info)
    getOrders: async () => {
        const response = await fetch(`${API_BASE_URL}/orders`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get specific order by ID (with review info)
    getOrderById: async (orderId) => {
        const response = await fetch(`${API_BASE_URL}/orders/${orderId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Update order status
    updateStatus: async (orderId, status) => {
        const response = await fetch(`${API_BASE_URL}/orders/${orderId}/status?status=${status}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Cancel order
    cancelOrder: async (orderId) => {
        const response = await fetch(`${API_BASE_URL}/orders/${orderId}/cancel`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Delete all user orders (clear virtual/test shop data)
    deleteAllOrders: async () => {
        const response = await fetch(`${API_BASE_URL}/orders`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// PAYMENT API SERVICE
// ======================
const PaymentAPI = {
    // Create payment (placeholder - adjust based on actual payment implementation)
    createPayment: async (orderId, method) => {
        const response = await fetch(`${API_BASE_URL}/payments`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ orderId, paymentMethod: method })
        });
        return handleResponse(response);
    },

    // Verify payment
    verifyPayment: async (paymentId) => {
        const response = await fetch(`${API_BASE_URL}/payments/${paymentId}/verify`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// REVIEW API SERVICE
// ======================
const ReviewAPI = {
    // Upload review image (returns { imageUrl })
    uploadReviewImage: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const token = localStorage.getItem('accessToken');
        const headers = { ...(token && { 'Authorization': `Bearer ${token}` }) };
        const response = await fetch(`${API_BASE_URL}/reviews/upload-image`, {
            method: 'POST',
            headers,
            body: formData
        });
        const data = await handleResponse(response);
        return data.imageUrl;
    },

    // Create review for a product
    createReview: async (productId, orderId, rating, comment, imageUrls = []) => {
        const body = { productId, orderId, rating, comment };
        if (imageUrls && imageUrls.length > 0) body.imageUrls = imageUrls;
        const response = await fetch(`${API_BASE_URL}/reviews`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(body)
        });
        return handleResponse(response);
    },

    // Get reviews for a product
    getProductReviews: async (productId) => {
        const response = await fetch(`${API_BASE_URL}/reviews/product/${productId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get user's reviews
    getUserReviews: async (userId) => {
        const response = await fetch(`${API_BASE_URL}/reviews/user/${userId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Update review
    updateReview: async (reviewId, rating, comment, imageUrls) => {
        const body = { rating, comment };
        if (imageUrls && imageUrls.length > 0) body.imageUrls = imageUrls;
        const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(body)
        });
        return handleResponse(response);
    },

    // Delete review
    deleteReview: async (reviewId) => {
        const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Check if user can review a product from an order
    canReview: async (orderId, productId) => {
        const response = await fetch(
            `${API_BASE_URL}/reviews/can-review?orderId=${orderId}&productId=${productId}`,
            { headers: getAuthHeaders() }
        );
        return handleResponse(response);
    },

    // Get all reviewable orders for current user
    getReviewableOrders: async () => {
        const response = await fetch(`${API_BASE_URL}/reviews/reviewable-orders`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get average rating for a product
    getAverageRating: async (productId) => {
        const response = await fetch(`${API_BASE_URL}/reviews/product/${productId}/average-rating`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// PRODUCT API SERVICE
// ======================
const ProductAPI = {
    // Get all products
    getAllProducts: async () => {
        const response = await fetch(`${API_BASE_URL}/products`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get product by ID
    getProductById: async (productId) => {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get products by shop ID
    getProductsByShop: async (shopId) => {
        const response = await fetch(`${API_BASE_URL}/products/shop/${shopId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Create product (for shop owners)
    createProduct: async (productData) => {
        const response = await fetch(`${API_BASE_URL}/products`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(productData)
        });
        return handleResponse(response);
    },

    // Update product
    updateProduct: async (productId, productData) => {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(productData)
        });
        return handleResponse(response);
    },

    // Delete product
    deleteProduct: async (productId) => {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// REFUND API SERVICE
// ======================
const RefundAPI = {
    requestRefund: async (orderId, buyerId, amount, reason) => {
        const response = await fetch(`${API_BASE_URL}/refunds/${orderId}/request`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ buyerId, amount, reason })
        });
        return handleResponse(response);
    },

    getRefundByOrder: async (orderId) => {
        const response = await fetch(`${API_BASE_URL}/refunds/${orderId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// DISPUTE API SERVICE
// ======================
const DisputeAPI = {
    createDispute: async (orderId, buyerId, reason, description) => {
        const response = await fetch(`${API_BASE_URL}/disputes`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ orderId, buyerId, reason, description })
        });
        return handleResponse(response);
    },

    getDisputeByOrder: async (orderId) => {
        const response = await fetch(`${API_BASE_URL}/disputes/order/${orderId}`, {
            headers: getAuthHeaders()
        });
        if (response.status === 404) return null;
        return handleResponse(response);
    },

    getDispute: async (disputeId) => {
        const response = await fetch(`${API_BASE_URL}/disputes/${disputeId}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    addImage: async (disputeId, imageUrl, uploadedBy) => {
        const response = await fetch(`${API_BASE_URL}/disputes/${disputeId}/images`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ imageUrl, uploadedBy })
        });
        return handleResponse(response);
    },

    getDisputeImages: async (disputeId) => {
        const response = await fetch(`${API_BASE_URL}/disputes/${disputeId}/images`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// ======================
// VOUCHER API SERVICE
// ======================
const VoucherAPI = {
    // Get voucher by code
    getByCode: async (code) => {
        const response = await fetch(`${API_BASE_URL}/vouchers/code/${code}`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get all vouchers
    getAllVouchers: async () => {
        const response = await fetch(`${API_BASE_URL}/vouchers`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    },

    // Get voucher codes the current user has already used (from their orders)
    getUsedVoucherCodes: async () => {
        const response = await fetch(`${API_BASE_URL}/vouchers/used-codes`, {
            headers: getAuthHeaders()
        });
        return handleResponse(response);
    }
};

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { CartAPI, OrderAPI, PaymentAPI, ReviewAPI, ProductAPI, VoucherAPI, RefundAPI, DisputeAPI };
}
