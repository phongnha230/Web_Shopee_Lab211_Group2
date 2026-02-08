// Use existing API_BASE_URL from page scope to avoid duplicate declaration
const NOTIFICATION_API = (typeof API_BASE_URL !== 'undefined' ? API_BASE_URL : 'http://localhost:8080/api');

document.addEventListener('DOMContentLoaded', () => {
    loadNotifications();
    setupNotificationUI();
});

// Setup UI interactions
function setupNotificationUI() {
    const btn = document.getElementById('notificationBtn');
    const dropdown = document.getElementById('notificationDropdown');

    if (!btn || !dropdown) return;

    // Toggle dropdown
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('hidden');
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!btn.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('hidden');
        }
    });

    // Mark all read button
    const markAllBtn = document.getElementById('markAllReadBtn');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', markAllAsRead);
    }
}

// Fetch notifications
async function loadNotifications() {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return;

    try {
        const response = await fetch(`${NOTIFICATION_API}/notifications`, {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (response.ok) {
            let notifications = await response.json();

            // 🔥 Filter out SECURITY notifications if NOT in Admin Dashboard
            // Check for a specific element that only exists in Admin Dashboard (e.g., 'adminAvatar')
            const isAdminDashboard = document.getElementById('adminAvatar');

            if (!isAdminDashboard) {
                notifications = notifications.filter(n => n.type !== 'SECURITY');
            }
            renderNotifications(notifications);
            updateBadge(notifications);
        }
    } catch (error) {
        console.error('Failed to load notifications:', error);
    }
}

// Update Badge Count
function updateBadge(notifications) {
    const unreadCount = notifications.filter(n => !n.read).length;
    const badge = document.getElementById('notificationBadge');

    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }
    }
}

// Render List
function renderNotifications(notifications) {
    const listContainer = document.getElementById('notificationList');
    if (!listContainer) return;

    if (notifications.length === 0) {
        listContainer.innerHTML = '<div class="p-4 text-center text-gray-500 text-sm">No notifications</div>';
        return;
    }

    listContainer.innerHTML = notifications.map(n => `
        <div class="p-3 border-b border-gray-100 hover:bg-gray-50 cursor-pointer ${n.read ? 'opacity-60' : 'bg-blue-50/50'}" 
             onclick="markAsRead('${n.id}', this)">
            <div class="flex items-start gap-3">
                <div class="text-xl ${getIconColor(n.type)}">
                    <i class="${getIconClass(n.type)}"></i>
                </div>
                <div class="flex-1">
                    <h4 class="text-sm font-semibold text-gray-800">${n.title}</h4>
                    <p class="text-xs text-gray-600 mt-1 line-clamp-2">${n.message}</p>
                    <span class="text-[10px] text-gray-400 mt-1 block">${formatDate(n.createdAt)}</span>
                </div>
                ${!n.read ? '<span class="w-2 h-2 bg-blue-500 rounded-full mt-1"></span>' : ''}
            </div>
        </div>
    `).join('');
}

// Mark single as read
async function markAsRead(id, element) {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return;

    try {
        await fetch(`${NOTIFICATION_API}/notifications/${id}/read`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        // UI Update
        element.classList.remove('bg-blue-50/50');
        element.classList.add('opacity-60');
        const dot = element.querySelector('.bg-blue-500');
        if (dot) dot.remove();

        // Update badge locally
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            let count = parseInt(badge.textContent) || 0;
            if (count > 0) {
                count--;
                badge.textContent = count > 0 ? count : '';
                if (count === 0) badge.classList.add('hidden');
            }
        }
    } catch (error) {
        console.error('Error marking as read:', error);
    }
}

// Mark all as read
async function markAllAsRead() {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return;

    try {
        await fetch(`${NOTIFICATION_API}/notifications/read-all`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
        loadNotifications(); // Reload to refresh UI
    } catch (error) {
        console.error('Error marking all as read:', error);
    }
}

// Helpers
function getIconClass(type) {
    switch (type) {
        case 'SECURITY': return 'fas fa-shield-alt';
        case 'ORDER': return 'fas fa-box';
        case 'SYSTEM': return 'fas fa-info-circle';
        default: return 'fas fa-bell';
    }
}

function getIconColor(type) {
    switch (type) {
        case 'SECURITY': return 'text-red-500';
        case 'ORDER': return 'text-blue-500';
        case 'SYSTEM': return 'text-orange-500';
        default: return 'text-gray-500';
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}


