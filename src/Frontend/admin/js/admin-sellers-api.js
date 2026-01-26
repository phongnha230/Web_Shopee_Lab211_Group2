const SHOPS_API_URL = 'http://localhost:8080/api/admin/shops';

let currentShops = [];

async function fetchShops(status = null) {
    try {
        let url = SHOPS_API_URL;
        if (status) {
            url += `?status=${status}`;
        }
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch shops');
        currentShops = await response.json();
        renderShopsTable(status);
    } catch (error) {
        console.error('Error loading shops:', error);
    }
}

async function updateShopStatus(id, status) {
    try {
        const response = await fetch(`${SHOPS_API_URL}/${id}/status?status=${status}`, {
            method: 'PATCH'
        });
        if (!response.ok) throw new Error('Failed to update shop status');

        // Refresh the current view
        const activeTab = document.querySelector('.tab-btn.active').id;
        if (activeTab === 'tab-active-shops') {
            fetchShops('ACTIVE');
        } else {
            fetchShops('PENDING');
        }
    } catch (error) {
        console.error('Error updating shop status:', error);
        alert('Failed to update shop status. Please try again.');
    }
}

function renderShopsTable(filterStatus) {
    const activeTbody = document.getElementById('shopsTable');
    const pendingTbody = document.getElementById('requestsTable');
    const pendingCountBadge = document.getElementById('pending-count');

    if (filterStatus === 'ACTIVE' || !filterStatus) {
        const activeList = currentShops.filter(s => s.status === 'ACTIVE' || s.status === 'BANNED');
        activeTbody.innerHTML = activeList.map(shop => `
            <tr class="group hover:bg-slate-50/50 transition-all border-b border-slate-50 last:border-none">
                <td class="px-10 py-5">
                    <div class="flex items-center gap-4">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(shop.name)}&background=f1f5f9&color=64748b" class="w-11 h-11 rounded-2xl border-2 border-white shadow-sm object-cover">
                        <span class="font-bold text-slate-900">${shop.name}</span>
                    </div>
                </td>
                <td class="px-10 py-5">
                   <span class="text-sm font-bold text-slate-600">${shop.ownerName || 'Unknown'}</span>
                </td>
                <td class="px-10 py-5 text-center">
                    <div class="inline-flex items-center gap-1.5 bg-amber-50 text-amber-600 px-3 py-1 rounded-xl text-xs font-black">
                        <i class="fas fa-star text-[10px]"></i> ${shop.rating.toFixed(1)}
                    </div>
                </td>
                <td class="px-10 py-5 text-center font-black text-slate-900 text-sm">${shop.productCount}</td>
                <td class="px-10 py-5 text-center">
                    <span class="px-3 py-1 rounded-xl text-[10px] font-black uppercase tracking-widest ${shop.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}">
                        ${shop.status}
                    </span>
                </td>
                <td class="px-10 py-5 text-center">
                    <div class="flex items-center justify-center gap-2">
                         ${shop.status === 'ACTIVE'
                ? `<button onclick="updateShopStatus('${shop.id}', 'BANNED')" class="w-9 h-9 flex items-center justify-center rounded-xl bg-rose-50 text-rose-600 hover:bg-rose-100 transition-colors" title="Ban Shop"><i class="fas fa-ban text-xs"></i></button>`
                : `<button onclick="updateShopStatus('${shop.id}', 'ACTIVE')" class="w-9 h-9 flex items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 hover:bg-emerald-100 transition-colors" title="Unban Shop"><i class="fas fa-check text-xs"></i></button>`
            }
                        <button class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-100 text-slate-500 hover:bg-slate-200 transition-colors">
                            <i class="fas fa-external-link-alt text-[10px]"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    if (filterStatus === 'PENDING' || !filterStatus) {
        const pendingList = currentShops.filter(s => s.status === 'PENDING');
        pendingCountBadge.innerText = pendingList.length;

        if (pendingList.length === 0) {
            pendingTbody.innerHTML = `<tr><td colspan="4" class="px-10 py-12 text-center text-slate-400 font-bold">No pending requests found</td></tr>`;
        } else {
            pendingTbody.innerHTML = pendingList.map(req => `
                <tr class="group hover:bg-slate-50/50 transition-all border-b border-slate-50 last:border-none table-row">
                    <td class="px-10 py-6 font-black text-slate-900 table-cell">${req.name}</td>
                    <td class="px-10 py-6 text-slate-600 font-bold table-cell">${req.ownerName || 'Unknown'}</td>
                    <td class="px-10 py-6 text-slate-400 text-xs font-bold uppercase tracking-wider table-cell">
                        <i class="far fa-calendar-alt mr-2"></i> ${new Date(req.createdAt).toLocaleDateString()}
                    </td>
                    <td class="px-10 py-6 text-center table-cell">
                        <div class="flex items-center justify-center gap-3">
                            <button onclick="updateShopStatus('${req.id}', 'ACTIVE')" class="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-2xl text-xs font-black shadow-lg shadow-indigo-500/20 transition-all active:scale-95">Accept</button>
                            <button onclick="updateShopStatus('${req.id}', 'REJECTED')" class="bg-rose-100 text-rose-600 px-5 py-2.5 rounded-2xl text-xs font-black hover:bg-rose-200 transition-all active:scale-95">Decline</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        }
    }
}

// Override legacy switchTab to fetch data
const originalSwitchTab = window.switchTab;
window.switchTab = function (tabId) {
    if (tabId === 'active-shops') {
        fetchShops('ACTIVE');
    } else if (tabId === 'pending-requests') {
        fetchShops('PENDING');
    }

    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');
    document.getElementById(`tab-${tabId}`).classList.add('active');
}

// Initial load
document.addEventListener('DOMContentLoaded', () => {
    fetchShops('ACTIVE');
});
