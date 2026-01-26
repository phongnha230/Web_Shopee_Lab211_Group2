const API_BASE_URL = 'http://localhost:8080/api/admin/dashboard';

async function fetchDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) throw new Error('Failed to fetch stats');
        const data = await response.json();
        updateDashboardUI(data);
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

function updateDashboardUI(data) {
    // Update Stat Cards
    document.querySelector('h3:nth-of-type(1)').innerText = `$${(data.totalGmv / 1000000).toFixed(2)}M`;
    document.querySelector('.lg\\:grid-cols-5 > div:nth-child(2) h3').innerText = data.totalOrders.toLocaleString();
    document.querySelector('.lg\\:grid-cols-5 > div:nth-child(3) h3').innerText = data.totalUsers.toLocaleString();
    document.querySelector('.lg\\:grid-cols-5 > div:nth-child(4) h3').innerText = data.totalShops.toLocaleString();
    document.querySelector('.lg\\:grid-cols-5 > div:nth-child(5) h3').innerText = `${data.conversionRate.toFixed(1)}%`;

    // Update Recent Users Table (Reusing the Top Selling Products section or adding a new one)
    if (data.recentUsers && data.recentUsers.length > 0) {
        // Here we could update a "Recent Users" section if we added one
        console.log('Recent Users:', data.recentUsers);
    }

    // Update Charts if Chart.js is available
    if (window.salesChartInstance && data.salesTrend) {
        window.salesChartInstance.data.datasets[0].data = data.salesTrend.map(s => s.value);
        window.salesChartInstance.update();
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    fetchDashboardStats();
    // Refresh every 5 minutes
    setInterval(fetchDashboardStats, 300000);
});
