// main.js: handle JWT auth, form submission, user list rendering

// Helper to get JWT and set Authorization header
function authFetch(url, options = {}) {
    const token = localStorage.getItem('jwt');
    if (!options.headers) options.headers = {};
    options.headers['Content-Type'] = 'application/json';
    options.headers['Authorization'] = 'Bearer ' + token;
    return fetch(url, options);
}

// Redirect to login if no token
if (!localStorage.getItem('jwt')) {
    window.location.href = '/login';
}

// Logout handler
const logoutBtn = document.getElementById('logoutBtn');
logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('jwt');
    window.location.href = '/login';
});

// Load existing subscription data into form
(async function loadProfile() {
    try {
        const res = await authFetch('/api/users/me', { method: 'GET' });
        if (!res.ok) throw new Error('Failed to load subscription');
        const user = await res.json();
        window.userId = user.id;
        document.getElementById('name').value = user.name || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phoneNumber').value = user.phoneNumber || '';
        document.getElementById('city').value = user.city || '';
    } catch (err) {
        console.error(err);
    }
})();

// Handle subscription update
const profileForm = document.getElementById('profileForm');
profileForm.addEventListener('submit', async e => {
  e.preventDefault();
  const payload = {
    id: window.userId,
    name: document.getElementById('name').value,
    email: document.getElementById('email').value,
    phoneNumber: document.getElementById('phoneNumber').value,
    city: document.getElementById('city').value
  };
  try {
    const res = await authFetch(`/users/${window.userId}`, {
      method: 'PUT', body: JSON.stringify(payload)
    });
    const msgEl = document.getElementById('profileMessage');
    if (res.ok) {
      msgEl.textContent = 'Subscription saved.';
      msgEl.style.color = 'green';
    } else {
      const err = await res.text();
      msgEl.textContent = 'Error: ' + err;
      msgEl.style.color = 'red';
    }
  } catch (err) {
    console.error(err);
  }
});

// Alerts fetch and render logic
const refreshBtn = document.getElementById('refreshBtn');
const alertsList = document.getElementById('alertsList');

async function loadAlerts() {
    try {
        const res = await authFetch('/alerts/history');
        if (!res.ok) throw new Error('Failed to fetch alerts');
        const alerts = await res.json();
        alertsList.innerHTML = '';
        alerts.forEach(alert => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.textContent = `[${alert.city}] ${alert.event} (${alert.startTime} - ${alert.endTime})`;
            alertsList.appendChild(li);
        });
    } catch (err) {
        console.error(err);
    }
}

refreshBtn.addEventListener('click', loadAlerts);
loadAlerts();
