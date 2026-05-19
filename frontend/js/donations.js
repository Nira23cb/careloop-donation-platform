/**
 * Shared donation card rendering and utilities.
 */

const STATUS_LABELS = {
  PENDING: 'Pending',
  VERIFIED: 'Verified',
  ASSIGNED: 'Assigned',
  OUT_FOR_DELIVERY: 'Out for Delivery',
  DELIVERED: 'Delivered',
  EXPIRED: 'Expired',
  CANCELLED: 'Cancelled'
};

/** Render a list of donation cards into a container */
function renderDonations(container, donations, actionBuilder) {
  if (!donations || donations.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="icon">📦</div>
        <p>No donations to show yet.</p>
      </div>`;
    return;
  }

  container.innerHTML = donations.map(d => buildDonationCard(d, actionBuilder)).join('');
}

/** Build HTML for one donation card */
function buildDonationCard(d, actionBuilder) {
  const statusClass = (d.status || '').toLowerCase().replace(/_/g, '_');
  const urgentClass = d.urgent ? ' urgent' : '';
  const cardClass = `donation-card status-${statusClass}${urgentClass}`;

  let badges = `<span class="badge badge-${statusClass}">${STATUS_LABELS[d.status] || d.status}</span>`;
  if (d.verified) {
    badges += '<span class="badge badge-verified-check">✓ Verified</span>';
  }
  if (d.urgent && d.timeLeft) {
    badges += '<span class="badge badge-urgent">⚠ Urgent</span>';
  }

  const vegLabel = d.vegType && d.vegType !== 'NA' ? d.vegType.replace('_', '-') : '';

  const actions = actionBuilder ? actionBuilder(d) : '';

  return `
    <article class="${cardClass}" data-id="${d.id}">
      <div class="card-header">
        <span class="card-type">${formatType(d.type)} · Qty ${d.quantity}</span>
        <div class="card-badges">${badges}</div>
      </div>
      <div class="card-meta">
        <span>📍 <strong>${escapeHtml(d.location)}</strong></span>
        ${d.donorName ? `<span>👤 ${escapeHtml(d.donorName)}</span>` : ''}
        ${d.timeLeft ? `<span>⏱ <strong class="${d.urgent ? 'text-urgent' : ''}">${d.timeLeft}</strong></span>` : ''}
        ${vegLabel ? `<span>🍽 ${vegLabel}</span>` : ''}
        ${d.volunteerName ? `<span>🚴 ${escapeHtml(d.volunteerName)}</span>` : ''}
      </div>
      ${actions ? `<div class="card-actions">${actions}</div>` : ''}
    </article>`;
}

function formatType(type) {
  if (!type) return '';
  return type.charAt(0) + type.slice(1).toLowerCase();
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/** Show cancel modal and call API */
function showCancelModal(donationId, onSuccess) {
  const overlay = document.getElementById('cancelModal');
  const form = document.getElementById('cancelForm');
  const idInput = document.getElementById('cancelDonationId');

  idInput.value = donationId;
  overlay.classList.add('show');

  form.onsubmit = async (e) => {
    e.preventDefault();
    const reason = document.getElementById('cancelReason').value.trim();
    if (!reason) return;

    try {
      await DonationAPI.cancel(donationId, reason);
      overlay.classList.remove('show');
      form.reset();
      if (onSuccess) onSuccess();
    } catch (err) {
      alert(err.message);
    }
  };
}

function closeCancelModal() {
  document.getElementById('cancelModal').classList.remove('show');
}

/** Build dashboard navbar HTML snippet */
function buildDashboardNav(user) {
  return `
    <nav class="navbar">
      <a href="index.html" class="logo">
        <span class="logo-icon">♻</span> CareLoop
      </a>
      <div class="nav-links">
        <span>Hi, <strong>${escapeHtml(user.name)}</strong></span>
        <div class="notif-wrapper">
          <button class="notif-bell" id="notifBell" aria-label="Notifications">🔔
            <span class="notif-count hidden" id="notifCount">0</span>
          </button>
          <div class="notif-dropdown" id="notifDropdown"></div>
        </div>
        <button class="btn btn-outline btn-sm" onclick="logout()">Logout</button>
      </div>
    </nav>`;
}

/** Load trust score into dashboard */
async function loadTrustScore() {
  try {
    const profile = await AuthAPI.me();
    document.getElementById('trustValue').textContent = profile.reliabilityScore + '%';
    document.getElementById('cancelValue').textContent = profile.cancelCount;
    document.getElementById('ratingValue').textContent = profile.rating;

    const tag = document.getElementById('unreliableTag');
    if (profile.unreliable) {
      tag.classList.remove('hidden');
    } else {
      tag.classList.add('hidden');
    }

    // Update local storage
    const user = getUser();
    user.reliabilityScore = profile.reliabilityScore;
    user.unreliable = profile.unreliable;
    localStorage.setItem('careloop_user', JSON.stringify(user));
  } catch (e) {
    console.warn('Trust score load failed', e);
  }
}
