// ==================== ADMIN USER MANAGEMENT API ====================
// Base URL của backend API
const API_BASE_URL = 'http://localhost:8080/api/admin/users';

// Lấy JWT token từ localStorage (giả sử đã login)
function getAuthToken() {
    return localStorage.getItem('adminToken') || '';
}

// Headers mặc định cho các request
function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
    };
}

// ==================== API FUNCTIONS ====================

/**
 * Lấy danh sách users với pagination và filter
 * @param {number} page - Trang hiện tại (0-indexed)
 * @param {number} size - Số lượng items mỗi trang
 * @param {string} search - Từ khóa tìm kiếm (email hoặc fullName)
 * @param {string} role - Filter theo role (ROLE_USER, ROLE_ADMIN, ROLE_SELLER)
 */
async function getAllUsers(page = 0, size = 10, search = '', role = '') {
    try {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });

        if (search) params.append('search', search);
        if (role) params.append('role', role);

        const response = await fetch(`${API_BASE_URL}?${params}`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error fetching users:', error);
        throw error;
    }
}

/**
 * Lấy chi tiết user theo ID
 * @param {string} userId - ID của user
 */
async function getUserById(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching user:', error);
        throw error;
    }
}

/**
 * Tạo user mới
 * @param {Object} userData - Thông tin user
 * @param {string} userData.email - Email
 * @param {string} userData.fullName - Họ tên
 * @param {string} userData.phone - Số điện thoại
 * @param {string} userData.password - Mật khẩu
 * @param {Array<string>} userData.roles - Danh sách roles (ví dụ: ["ROLE_USER"])
 * @param {boolean} userData.active - Trạng thái active
 */
async function createUser(userData) {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error creating user:', error);
        throw error;
    }
}

/**
 * Cập nhật thông tin user
 * @param {string} userId - ID của user
 * @param {Object} userData - Thông tin cần cập nhật
 */
async function updateUser(userId, userData) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error updating user:', error);
        throw error;
    }
}

/**
 * Xóa user
 * @param {string} userId - ID của user
 */
async function deleteUser(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error deleting user:', error);
        throw error;
    }
}

/**
 * Toggle trạng thái active/ban của user
 * @param {string} userId - ID của user
 */
async function toggleUserStatus(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}/status`, {
            method: 'PATCH',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error toggling user status:', error);
        throw error;
    }
}

/**
 * Cập nhật roles cho user
 * @param {string} userId - ID của user
 * @param {Array<string>} roles - Danh sách roles mới
 */
async function updateUserRoles(userId, roles) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}/roles`, {
            method: 'PATCH',
            headers: getHeaders(),
            body: JSON.stringify({ roles })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error updating user roles:', error);
        throw error;
    }
}

// ==================== USAGE EXAMPLES ====================

// Ví dụ 1: Lấy danh sách users
async function loadUsers() {
    try {
        const data = await getAllUsers(0, 10, '', '');
        console.log('Users:', data.users);
        console.log('Total:', data.totalElements);
        console.log('Pages:', data.totalPages);

        // Render users vào table
        renderUsers(data.users);
    } catch (error) {
        alert('Failed to load users: ' + error.message);
    }
}

// Ví dụ 2: Tìm kiếm users
async function searchUsers(searchTerm) {
    try {
        const data = await getAllUsers(0, 10, searchTerm, '');
        renderUsers(data.users);
    } catch (error) {
        alert('Search failed: ' + error.message);
    }
}

// Ví dụ 3: Tạo user mới
async function handleCreateUser() {
    const userData = {
        email: 'newuser@example.com',
        fullName: 'New User',
        phone: '0123456789',
        password: 'password123',
        roles: ['ROLE_USER'],
        active: true
    };

    try {
        const newUser = await createUser(userData);
        console.log('User created:', newUser);
        alert('User created successfully!');
        loadUsers(); // Reload danh sách
    } catch (error) {
        alert('Failed to create user: ' + error.message);
    }
}

// Ví dụ 4: Toggle status
async function handleToggleStatus(userId) {
    try {
        const updatedUser = await toggleUserStatus(userId);
        console.log('Status updated:', updatedUser);
        loadUsers(); // Reload danh sách
    } catch (error) {
        alert('Failed to toggle status: ' + error.message);
    }
}

// Ví dụ 5: Xóa user
async function handleDeleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    try {
        await deleteUser(userId);
        alert('User deleted successfully!');
        loadUsers(); // Reload danh sách
    } catch (error) {
        alert('Failed to delete user: ' + error.message);
    }
}

// Helper function để render users (tương tự như trong HTML hiện tại)
function renderUsers(users) {
    const tbody = document.getElementById('userTableBody');
    if (!tbody) return;

    tbody.innerHTML = users.map(user => `
        <tr class="hover:bg-slate-50 transition-colors">
            <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=random&color=fff" 
                         class="w-10 h-10 rounded-full border border-slate-200">
                    <div>
                        <p class="font-semibold text-slate-800">${user.fullName}</p>
                        <p class="text-xs text-slate-500">${user.email}</p>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold 
                    ${user.roles.includes('ROLE_ADMIN') ? 'bg-purple-100 text-purple-700' :
            user.roles.includes('ROLE_SELLER') ? 'bg-orange-100 text-orange-700' :
                'bg-blue-100 text-blue-700'}">
                    ${user.roles.join(', ').replace(/ROLE_/g, '')}
                </span>
            </td>
            <td class="px-6 py-4 text-sm text-slate-600">${new Date(user.createdAt).toLocaleDateString()}</td>
            <td class="px-6 py-4 text-center">
                <div class="relative inline-block w-10 mr-2 align-middle select-none transition duration-200 ease-in">
                    <input type="checkbox" 
                           id="toggle-${user.id}" 
                           class="toggle-checkbox absolute block w-5 h-5 rounded-full bg-white border-4 appearance-none cursor-pointer 
                                  ${user.active ? 'right-0 border-green-400' : 'left-0 border-gray-300'}" 
                           ${user.active ? 'checked' : ''} 
                           onclick="handleToggleStatus('${user.id}')"/>
                    <label for="toggle-${user.id}" 
                           class="toggle-label block overflow-hidden h-5 rounded-full cursor-pointer 
                                  ${user.active ? 'bg-green-400' : 'bg-gray-300'}"></label>
                </div>
            </td>
            <td class="px-6 py-4 text-center">
                <button class="text-slate-400 hover:text-blue-500 mx-1 transition-colors" 
                        title="Edit" 
                        onclick="handleEditUser('${user.id}')">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="text-slate-400 hover:text-red-500 mx-1 transition-colors" 
                        title="Delete" 
                        onclick="handleDeleteUser('${user.id}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Init: Load users khi trang load
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
});
