// ==================== ADMIN USER MANAGEMENT API ====================
// Base URL của backend API
const API_BASE_URL = window.location.hostname === '127.0.0.1'
    ? 'http://127.0.0.1:8080/api/admin/users'
    : 'http://localhost:8080/api/admin/users';

// State management
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let totalElements = 0;
let currentSearch = '';
let currentRole = '';

// Lấy JWT token từ localStorage (giả sử đã login)
function getAuthToken() {
    return localStorage.getItem('accessToken') || '';
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
            if (response.status === 401 || response.status === 403) {
                window.location.href = '../login.html';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // Update state
        totalPages = data.totalPages;
        totalElements = data.totalElements;
        currentPage = page;

        return data;
    } catch (error) {
        console.error('Error fetching users:', error);
        throw error;
    }
}

async function getUserById(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}`, {
            method: 'GET',
            headers: getHeaders()
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error('Error fetching user:', error);
        throw error;
    }
}

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
        return true;
    } catch (error) {
        console.error('Error deleting user:', error);
        throw error;
    }
}

async function toggleUserStatus(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${userId}/status`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error('Error toggling user status:', error);
        throw error;
    }
}

// ==================== UI HANDLERS ====================

async function loadUsers(page = currentPage) {
    try {
        const data = await getAllUsers(page, pageSize, currentSearch, currentRole);
        if (data) {
            renderUsers(data.users);
            renderPagination();
        }
    } catch (error) {
        console.error('Failed to load users:', error);
    }
}

function openModal(user = null) {
    const modal = document.getElementById('userModal');
    const form = document.getElementById('userForm');
    const title = document.getElementById('modalTitle');
    const passwordField = document.getElementById('passwordField');

    form.reset();
    document.getElementById('userId').value = '';

    if (user) {
        title.innerText = 'Edit User';
        document.getElementById('userId').value = user.id;
        document.getElementById('fullName').value = user.fullName;
        document.getElementById('email').value = user.email;
        document.getElementById('phone').value = user.phone || '';
        document.getElementById('active').checked = user.active;

        // Handle multiple roles
        const roleSelect = document.getElementById('roles');
        Array.from(roleSelect.options).forEach(option => {
            option.selected = user.roles.includes(option.value);
        });

        passwordField.classList.add('hidden');
        document.getElementById('password').required = false;
    } else {
        title.innerText = 'Add New User';
        passwordField.classList.remove('hidden');
        document.getElementById('password').required = true;
    }

    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    const modal = document.getElementById('userModal');
    modal.classList.add('hidden');
    document.body.style.overflow = 'auto';
}

async function handleEditUser(userId) {
    try {
        const user = await getUserById(userId);
        openModal(user);
    } catch (error) {
        alert('Failed to fetch user details: ' + error.message);
    }
}

async function handleDeleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) return;

    try {
        await deleteUser(userId);
        loadUsers();
    } catch (error) {
        alert('Failed to delete user: ' + error.message);
    }
}

async function handleToggleStatus(userId) {
    try {
        await toggleUserStatus(userId);
        loadUsers();
    } catch (error) {
        alert('Failed to update status: ' + error.message);
    }
}

// ==================== RENDERING ====================

function renderUsers(users) {
    const tbody = document.getElementById('userTableBody');
    if (!tbody) return;

    if (users.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="px-8 py-10 text-center text-slate-400 font-medium">
                    No users found matching your criteria.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = users.map(user => `
        <tr class="group hover:bg-slate-50/50 transition-all border-b border-slate-50 last:border-none">
            <td class="px-8 py-5">
                <div class="flex items-center gap-4">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=f1f5f9&color=64748b" 
                         class="w-11 h-11 rounded-2xl border-2 border-white shadow-sm object-cover">
                    <div>
                        <p class="font-bold text-slate-900 line-clamp-1">${user.fullName}</p>
                        <p class="text-[11px] text-slate-400 font-semibold uppercase tracking-wider line-clamp-1">${user.email}</p>
                    </div>
                </div>
            </td>
            <td class="px-8 py-5">
                <div class="flex flex-wrap gap-1">
                    ${user.roles.map(role => `
                        <span class="inline-flex items-center px-2 py-0.5 rounded-lg text-[9px] font-black uppercase tracking-widest 
                            ${role === 'ROLE_ADMIN' ? 'bg-indigo-50 text-indigo-600' :
            role === 'ROLE_SELLER' ? 'bg-orange-50 text-orange-600' :
                'bg-blue-50 text-blue-600'}">
                            ${role.replace(/ROLE_/g, '')}
                        </span>
                    `).join('')}
                </div>
            </td>
            <td class="px-8 py-5">
                <span class="text-sm font-bold text-slate-500">${new Date(user.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
            </td>
            <td class="px-8 py-5 text-center">
                <div class="flex justify-center">
                    <div class="relative inline-block w-10 h-5 align-middle select-none transition duration-200 ease-in">
                        <input type="checkbox" 
                               id="toggle-${user.id}" 
                               class="toggle-checkbox absolute block w-5 h-5 rounded-full bg-white border-2 appearance-none cursor-pointer transition-all duration-300
                                      ${user.active ? 'right-0 border-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.3)]' : 'left-0 border-slate-300 shadow-sm'}" 
                               ${user.active ? 'checked' : ''} 
                               onchange="handleToggleStatus('${user.id}')"/>
                        <label for="toggle-${user.id}" 
                               class="toggle-label block overflow-hidden h-5 rounded-full cursor-pointer transition-colors duration-300
                                      ${user.active ? 'bg-emerald-500' : 'bg-slate-200'}"></label>
                    </div>
                </div>
            </td>
            <td class="px-8 py-5 text-center">
                <div class="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button class="w-9 h-9 flex items-center justify-center rounded-xl bg-blue-50 text-blue-600 hover:bg-blue-100 transition-colors" 
                            title="Edit" 
                            onclick="handleEditUser('${user.id}')">
                        <i class="fas fa-edit text-xs"></i>
                    </button>
                    <button class="w-9 h-9 flex items-center justify-center rounded-xl bg-rose-50 text-rose-600 hover:bg-rose-100 transition-colors" 
                            title="Delete" 
                            onclick="handleDeleteUser('${user.id}')">
                        <i class="fas fa-trash text-xs"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderPagination() {
    const info = document.getElementById('paginationInfo');
    const controls = document.getElementById('paginationControls');

    const start = currentPage * pageSize + 1;
    const end = Math.min((currentPage + 1) * pageSize, totalElements);

    info.innerText = `Showing ${totalElements > 0 ? start : 0} to ${end} of ${totalElements} entries`;

    let html = `
        <button onclick="changePage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}
            class="w-9 h-9 flex items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
            <i class="fas fa-chevron-left text-xs"></i>
        </button>
    `;

    // Simple page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            html += `
                <button onclick="changePage(${i})"
                    class="w-9 h-9 flex items-center justify-center rounded-xl font-bold text-sm transition-all
                    ${currentPage === i ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/20' : 'border border-slate-200 bg-white text-slate-500 hover:bg-slate-50'}">
                    ${i + 1}
                </button>
            `;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            html += `<span class="text-slate-400">...</span>`;
        }
    }

    html += `
        <button onclick="changePage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}
            class="w-9 h-9 flex items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
            <i class="fas fa-chevron-right text-xs"></i>
        </button>
    `;

    controls.innerHTML = html;
}

function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    loadUsers(page);
}

// ==================== INITIALIZATION ====================

document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    if (!getAuthToken()) {
        window.location.href = '../login.html';
        return;
    }

    loadUsers();
    updateAdminProfile();

    // Add User Button
    document.getElementById('addUserBtn').addEventListener('click', () => openModal());

    // User Form Submission
    document.getElementById('userForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const userId = document.getElementById('userId').value;
        const userData = {
            fullName: document.getElementById('fullName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            roles: Array.from(document.getElementById('roles').selectedOptions).map(opt => opt.value),
            active: document.getElementById('active').checked
        };

        if (!userId) {
            userData.password = document.getElementById('password').value;
        }

        try {
            if (userId) {
                await updateUser(userId, userData);
            } else {
                await createUser(userData);
            }
            closeModal();
            loadUsers();
        } catch (error) {
            alert('Operation failed: ' + error.message);
        }
    });

    // Search Input
    let searchTimeout;
    document.getElementById('searchInput').addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            currentSearch = e.target.value;
            loadUsers(0);
        }, 500);
    });

    // Role Filter
    document.getElementById('roleFilter').addEventListener('change', (e) => {
        currentRole = e.target.value;
        loadUsers(0);
    });
});

function logout() {
    if (confirm('Are you sure you want to sign out?')) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userName');
        window.location.href = '../login.html';
    }
}

function updateAdminProfile() {
    const userName = localStorage.getItem('userName') || 'Administrator';
    const profileName = document.querySelector('aside p.text-slate-900');
    const profileImg = document.querySelector('aside img');

    if (profileName) profileName.textContent = userName;
    if (profileImg) {
        profileImg.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(userName)}&background=4f46e5&color=fff`;
    }
}
