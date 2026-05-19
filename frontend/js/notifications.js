/**
 * Notification bell dropdown for dashboard pages.
 */

async function initNotifications() {
  const bell = document.getElementById('notifBell');
  const dropdown = document.getElementById('notifDropdown');
  const countEl = document.getElementById('notifCount');

  if (!bell) return;

  try {
    const { count } = await NotificationAPI.unreadCount();
    if (count > 0) {
      countEl.textContent = count > 9 ? '9+' : count;
      countEl.classList.remove('hidden');
    } else {
      countEl.classList.add('hidden');
    }
  } catch (e) {
    console.warn('Could not load notifications', e);
  }

  bell.addEventListener('click', async (e) => {
    e.stopPropagation();
    dropdown.classList.toggle('show');

    if (dropdown.classList.contains('show')) {
      await loadNotifications(dropdown);
      try {
        await NotificationAPI.markAllRead();
        countEl.classList.add('hidden');
      } catch (e) { /* ignore */ }
    }
  });

  document.addEventListener('click', () => dropdown.classList.remove('show'));
  dropdown.addEventListener('click', (e) => e.stopPropagation());
}

async function loadNotifications(container) {
  try {
    const list = await NotificationAPI.list();
    if (!list.length) {
      container.innerHTML = '<div class="notif-empty">No notifications yet</div>';
      return;
    }

    container.innerHTML = list.map(n => `
      <div class="notif-item ${n.readFlag ? '' : 'unread'}">
        <div>${escapeHtml(n.message)}</div>
        <div class="time">${formatTime(n.createdAt)} · ${n.type}</div>
      </div>
    `).join('');
  } catch (e) {
    container.innerHTML = '<div class="notif-empty">Could not load notifications</div>';
  }
}

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleString();
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
