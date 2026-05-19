/**
 * CareLoop API helper - all backend calls go through here.
 * Base URL points to Spring Boot on port 8080.
 */
const API_BASE = 'http://localhost:8080/api';

/** Get stored JWT token */
function getToken() {
  return localStorage.getItem('careloop_token');
}

/** Get logged-in user from localStorage */
function getUser() {
  const raw = localStorage.getItem('careloop_user');
  return raw ? JSON.parse(raw) : null;
}

/** Save auth data after login/register */
function saveAuth(response) {
  localStorage.setItem('careloop_token', response.token);
  localStorage.setItem('careloop_user', JSON.stringify({
    id: response.id,
    name: response.name,
    email: response.email,
    role: response.role,
    reliabilityScore: response.reliabilityScore,
    unreliable: response.unreliable
  }));
}

/** Clear auth and redirect to login */
function logout() {
  localStorage.removeItem('careloop_token');
  localStorage.removeItem('careloop_user');
  window.location.href = 'login.html';
}

/** Generic fetch wrapper with auth header */
async function apiFetch(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const token = getToken();
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }

  const response = await fetch(API_BASE + endpoint, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.error || 'Request failed');
  }

  return data;
}

/** Auth APIs */
const AuthAPI = {
  register: (body) => apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  me: () => apiFetch('/auth/me')
};

/** Donation APIs */
const DonationAPI = {
  create: (body) => apiFetch('/donations', { method: 'POST', body: JSON.stringify(body) }),
  list: (type, location) => {
    let q = '';
    if (type) q += '?type=' + type;
    if (location) q += (q ? '&' : '?') + 'location=' + encodeURIComponent(location);
    return apiFetch('/donations' + q);
  },
  verify: (id) => apiFetch('/donations/' + id + '/verify', { method: 'PUT' }),
  assign: (id) => apiFetch('/donations/' + id + '/assign', { method: 'PUT' }),
  outForDelivery: (id) => apiFetch('/donations/' + id + '/out-for-delivery', { method: 'PUT' }),
  deliver: (id) => apiFetch('/donations/' + id + '/deliver', { method: 'PUT' }),
  cancel: (id, reason) => apiFetch('/donations/' + id + '/cancel', {
    method: 'PUT',
    body: JSON.stringify({ reason })
  }),
  myDeliveries: () => apiFetch('/donations/my-deliveries'),
  impact: () => apiFetch('/donations/impact')
};

/** Notification APIs */
const NotificationAPI = {
  list: () => apiFetch('/notifications'),
  unreadCount: () => apiFetch('/notifications/unread-count'),
  markAllRead: () => apiFetch('/notifications/read-all', { method: 'PUT' })
};

/** Redirect to correct dashboard by role */
function redirectToDashboard(role) {
  const map = {
    DONOR: 'donor-dashboard.html',
    NGO: 'ngo-dashboard.html',
    VOLUNTEER: 'volunteer-dashboard.html'
  };
  window.location.href = map[role] || 'login.html';
}

/** Protect dashboard pages - require login */
function requireAuth(expectedRole) {
  const user = getUser();
  if (!user || !getToken()) {
    window.location.href = 'login.html';
    return null;
  }
  if (expectedRole && user.role !== expectedRole) {
    redirectToDashboard(user.role);
    return null;
  }
  return user;
}
