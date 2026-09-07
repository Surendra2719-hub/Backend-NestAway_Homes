/* NestAway Homes - Main Application Controller (Vanilla JS + Spring Boot REST APIs) */

const BASE_URL = '/api';

// Global App State
let currentUser = JSON.parse(localStorage.getItem('nestaway_user')) || null;
let authToken = localStorage.getItem('nestaway_token') || null;
let currentProperties = [];
let wishlist = JSON.parse(localStorage.getItem('nestaway_wishlist')) || [];
let activeView = 'home';
let authMode = 'login';
let selectedProperty = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
  updateUserUI();
  fetchApprovedProperties();
  setupInitialDates();
});

function getHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }
  return headers;
}

// Show Toast Notification
function showToast(message) {
  const existing = document.querySelector('.toast-msg');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = 'toast-msg';
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3500);
}

// Navigation & View Routing
function showView(viewId) {
  activeView = viewId;
  document.querySelectorAll('.view-section').forEach(el => el.style.display = 'none');
  
  const target = document.getElementById(`${viewId}-view`);
  if (target) {
    target.style.display = 'block';
  }

  // Update active navbar links
  document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active'));
  const activeNav = document.getElementById(`nav-${viewId}`);
  if (activeNav) activeNav.classList.add('active');

  window.scrollTo({ top: 0, behavior: 'smooth' });

  // Refresh view specific data
  if (viewId === 'host') loadHostDashboard();
  if (viewId === 'admin') loadAdminDashboard();
  if (viewId === 'bookings') loadGuestBookings();
  if (viewId === 'profile') loadProfileView();
}

// Update Header User State
function updateUserUI() {
  const authWrapper = document.getElementById('auth-buttons-wrapper');
  const userWrapper = document.getElementById('user-menu-wrapper');
  const roleWrapper = document.getElementById('role-action-wrapper');
  const mobileRoleWrapper = document.getElementById('mobile-role-btn-wrapper');
  const mobileAuthWrapper = document.getElementById('mobile-auth-actions');

  if (currentUser) {
    authWrapper.style.display = 'none';
    userWrapper.style.display = 'block';

    document.getElementById('user-display-name').textContent = currentUser.name.split(' ')[0];
    document.getElementById('user-avatar-initial').textContent = currentUser.name[0].toUpperCase();
    document.getElementById('dropdown-name').textContent = currentUser.name;
    document.getElementById('dropdown-role').textContent = currentUser.role;

    // Role-Based Button Mapping
    if (currentUser.role === 'ADMIN') {
      roleWrapper.innerHTML = `<button class="btn-primary" onclick="showView('admin')">🛡️ Admin Panel</button>`;
      mobileRoleWrapper.innerHTML = `<button class="btn-primary" style="width:100%" onclick="showView('admin'); toggleMobileDrawer();">🛡️ Admin Panel</button>`;
    } else if (currentUser.role === 'HOST') {
      roleWrapper.innerHTML = `<button class="btn-secondary" onclick="showView('host')">➕ Host Dashboard</button>`;
      mobileRoleWrapper.innerHTML = `<button class="btn-secondary" style="width:100%" onclick="showView('host'); toggleMobileDrawer();">➕ Host Dashboard</button>`;
    } else {
      roleWrapper.innerHTML = `<button class="host-link-btn" onclick="handleRoleClick()">Become a Host</button>`;
      mobileRoleWrapper.innerHTML = `<button class="btn-primary" style="width:100%" onclick="handleRoleClick(); toggleMobileDrawer();">Become a Host</button>`;
    }

    mobileAuthWrapper.innerHTML = `<button class="dropdown-item logout-red" onclick="logout(); toggleMobileDrawer();">🚪 Logout</button>`;
  } else {
    authWrapper.style.display = 'flex';
    userWrapper.style.display = 'none';
    roleWrapper.innerHTML = `<button class="host-link-btn" onclick="handleRoleClick()">Become a Host</button>`;

    mobileRoleWrapper.innerHTML = `<button class="btn-primary" style="width:100%" onclick="handleRoleClick(); toggleMobileDrawer();">Become a Host</button>`;
    mobileAuthWrapper.innerHTML = `
      <button class="btn-secondary" style="width:100%; margin-bottom:0.5rem;" onclick="openAuthModal('login'); toggleMobileDrawer();">Log in</button>
      <button class="btn-primary" style="width:100%;" onclick="openAuthModal('signup'); toggleMobileDrawer();">Sign up</button>
    `;
  }

  document.getElementById('wishlist-count').textContent = wishlist.length;
}

function toggleUserDropdown() {
  const dropdown = document.getElementById('user-dropdown');
  dropdown.classList.toggle('show');
}

function toggleMobileDrawer() {
  const drawer = document.getElementById('mobile-drawer');
  drawer.classList.toggle('show');
}

function handleRoleClick() {
  if (!currentUser) {
    openAuthModal('signup');
  } else if (currentUser.role === 'GUEST') {
    showView('profile');
  } else if (currentUser.role === 'HOST') {
    showView('host');
  } else if (currentUser.role === 'ADMIN') {
    showView('admin');
  }
}

// ----------------------------------------------------
// AUTH & USER APIs
// ----------------------------------------------------
// Password Visibility Toggle & Register Role Switcher
function togglePasswordVisibility(inputId, btn) {
  const input = document.getElementById(inputId);
  if (!input) return;
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = '🙈';
  } else {
    input.type = 'password';
    btn.textContent = '👁️';
  }
}

function selectRegisterRole(role) {
  document.getElementById('auth-register-role').value = role;
  document.querySelectorAll('.role-card').forEach(card => card.classList.remove('active'));
  if (role === 'GUEST') {
    const guestCard = document.getElementById('role-card-guest');
    if (guestCard) guestCard.classList.add('active');
  } else {
    const hostCard = document.getElementById('role-card-host');
    if (hostCard) hostCard.classList.add('active');
  }
}

function setAuthMode(mode) {
  authMode = (mode === 'signup' || mode === 'register') ? 'register' : 'login';
  
  const nameGroup = document.getElementById('auth-name-group');
  const phoneGroup = document.getElementById('auth-phone-group');
  const roleGroup = document.getElementById('auth-role-select-group');
  const confirmPassGroup = document.getElementById('auth-confirm-password-group');
  
  const submitBtn = document.getElementById('auth-submit-btn');
  const toggleBtn = document.getElementById('auth-toggle-btn');
  const subtitle = document.getElementById('auth-subtitle');

  const tabLogin = document.getElementById('auth-tab-login');
  const tabRegister = document.getElementById('auth-tab-register');

  const nameInput = document.getElementById('auth-name');
  const confirmPassInput = document.getElementById('auth-confirm-password');

  if (authMode === 'login') {
    if (subtitle) subtitle.textContent = 'Welcome back to NestAway';
    if (nameGroup) nameGroup.style.display = 'none';
    if (phoneGroup) phoneGroup.style.display = 'none';
    if (roleGroup) roleGroup.style.display = 'none';
    if (confirmPassGroup) confirmPassGroup.style.display = 'none';

    if (nameInput) nameInput.required = false;
    if (confirmPassInput) confirmPassInput.required = false;

    if (submitBtn) submitBtn.textContent = 'Log in';
    if (toggleBtn) toggleBtn.textContent = "Don't have an account? Sign up";

    if (tabLogin) tabLogin.classList.add('active');
    if (tabRegister) tabRegister.classList.remove('active');
  } else {
    if (subtitle) subtitle.textContent = 'Create your NestAway account';
    if (nameGroup) nameGroup.style.display = 'block';
    if (phoneGroup) phoneGroup.style.display = 'block';
    if (roleGroup) roleGroup.style.display = 'block';
    if (confirmPassGroup) confirmPassGroup.style.display = 'block';

    if (nameInput) nameInput.required = true;
    if (confirmPassInput) confirmPassInput.required = true;

    if (submitBtn) submitBtn.textContent = 'Create Account';
    if (toggleBtn) toggleBtn.textContent = 'Already have an account? Log in';

    if (tabRegister) tabRegister.classList.add('active');
    if (tabLogin) tabLogin.classList.remove('active');
  }
}

function openAuthModal(mode = 'login') {
  setAuthMode(mode);
  const modal = document.getElementById('auth-modal');
  if (modal) modal.classList.add('show');
}

function closeAuthModal() {
  const modal = document.getElementById('auth-modal');
  if (modal) modal.classList.remove('show');
}

function toggleAuthMode() {
  setAuthMode(authMode === 'login' ? 'register' : 'login');
}

async function handleAuthSubmit(e) {
  e.preventDefault();
  const email = document.getElementById('auth-email').value.trim();
  const password = document.getElementById('auth-password').value;

  if (authMode === 'login') {
    try {
      const res = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ email, password })
      });

      if (res.ok) {
        const data = await res.json();
        currentUser = { id: data.id, name: data.name, email: data.email, role: data.role };
        authToken = data.token;
        localStorage.setItem('nestaway_user', JSON.stringify(currentUser));
        localStorage.setItem('nestaway_token', authToken);

        closeAuthModal();
        updateUserUI();
        showToast(`Welcome back, ${currentUser.name}!`);
        showView('home');
      } else {
        alert('⚠️ Invalid Email ID or Password');
      }
    } catch (err) {
      alert('⚠️ Network error connecting to backend service');
    }
  } else {
    // Signup / Register Mode - Validate Passwords Match & Name
    const name = document.getElementById('auth-name').value.trim();
    if (!name) {
      alert('⚠️ Full Name is required to register!');
      return;
    }

    const confirmPassword = document.getElementById('auth-confirm-password').value;
    if (password !== confirmPassword) {
      alert('⚠️ Passwords do not match! Please make sure both password fields match exactly.');
      return;
    }

    const phone = document.getElementById('auth-phone').value.trim();
    const roleSelect = document.getElementById('auth-register-role').value;

    const endpoint = roleSelect === 'HOST' ? `${BASE_URL}/users/register-host` : `${BASE_URL}/users/register`;

    try {
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ name, email, password, phone })
      });

      if (res.ok) {
        showToast('Registration successful! Please log in with your credentials.');
        openAuthModal('login');
      } else {
        let errorMsg = `Server returned status ${res.status}`;
        try {
          const text = await res.text();
          try {
            const errData = JSON.parse(text);
            if (typeof errData === 'string') {
              errorMsg = errData;
            } else if (errData) {
              errorMsg = errData.message || errData.error || errData.email || errData.name || errData.password || JSON.stringify(errData);
            }
          } catch(e) {
            if (text && text.trim().length > 0) errorMsg = text;
          }
        } catch (err){}
        alert(`⚠️ Registration Failed (Status ${res.status}): ${errorMsg}`);
      }
    } catch (err) {
      alert('⚠️ Network error registering user');
    }
  }
}

function logout() {
  currentUser = null;
  authToken = null;
  localStorage.removeItem('nestaway_user');
  localStorage.removeItem('nestaway_token');
  updateUserUI();
  showToast('Logged out successfully');
  showView('home');
}

// ----------------------------------------------------
// PROPERTY APIs & RENDERING
// ----------------------------------------------------
const DEMO_PROPERTIES = [
  {
    id: 101,
    title: "Heritage Royal Villa with Private Pool",
    city: "Jaipur",
    state: "Rajasthan",
    pricePerNight: 5500,
    bedrooms: 4, beds: 4, bathrooms: 4,
    description: "Royal Rajasthani heritage villa featuring handcrafted wooden furniture, courtyard garden, plunge pool, and Fort views.",
    coverImage: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80"
  },
  {
    id: 102,
    title: "Modern Tech-Hub Studio",
    city: "Bengaluru",
    state: "Karnataka",
    pricePerNight: 2800,
    bedrooms: 1, beds: 1, bathrooms: 1,
    description: "Ultra-modern studio equipped with high-speed fiber Wi-Fi, workstation setup, espresso machine, and walking access to metro.",
    coverImage: "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80"
  },
  {
    id: 103,
    title: "Scenic Beachfront Cottage",
    city: "Goa",
    state: "Goa",
    pricePerNight: 4200,
    bedrooms: 2, beds: 2, bathrooms: 2,
    description: "Charming independent coastal cottage just 2 minutes walk from Calangute beach shore.",
    coverImage: "https://images.unsplash.com/photo-1507089947368-19c1da9775ae?auto=format&fit=crop&w=1200&q=80"
  },
  {
    id: 104,
    title: "Executive 2BHK Flat",
    city: "Hyderabad",
    state: "Telangana",
    pricePerNight: 2900,
    bedrooms: 2, beds: 2, bathrooms: 2,
    description: "Premium executive apartment for IT professionals and families visiting Hyderabad.",
    coverImage: "https://images.unsplash.com/photo-1554995207-c18c203602cb?auto=format&fit=crop&w=1200&q=80"
  }
];

async function fetchApprovedProperties() {
  try {
    const res = await fetch(`${BASE_URL}/properties`);
    if (res.ok) {
      const data = await res.json();
      currentProperties = data.length > 0 ? data : DEMO_PROPERTIES;
    } else {
      currentProperties = DEMO_PROPERTIES;
    }
  } catch (err) {
    currentProperties = DEMO_PROPERTIES;
  }
  renderProperties();
}

function renderProperties() {
  const homeGrid = document.getElementById('home-recommended-grid');
  const exploreGrid = document.getElementById('explore-grid');

  if (!homeGrid || !exploreGrid) return;

  if (currentProperties.length === 0) {
    const emptyHtml = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: var(--bg-subtle); border-radius: var(--radius-xl);">
        <h3>No Live Properties Listed Yet</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">Be the first host to list a home on NestAway Homes!</p>
      </div>
    `;
    homeGrid.innerHTML = emptyHtml;
    exploreGrid.innerHTML = emptyHtml;
    return;
  }

  const cardsHtml = currentProperties.map(p => createPropertyCardHtml(p)).join('');
  homeGrid.innerHTML = currentProperties.slice(0, 3).map(p => createPropertyCardHtml(p)).join('');
  exploreGrid.innerHTML = cardsHtml;
}

function createPropertyCardHtml(p) {
  const coverImg = p.coverImage || 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80';
  const isWishlisted = wishlist.includes(p.id);

  return `
    <div class="property-card" onclick="openPropertyDetailModal(${p.id})">
      <div class="card-img-wrapper">
        <img src="${coverImg}" alt="${p.title}" class="card-img">
        <button class="card-wishlist-btn" onclick="event.stopPropagation(); toggleWishlist(${p.id})">
          ${isWishlisted ? '❤️' : '🤍'}
        </button>
      </div>
      <div class="card-body">
        <div class="card-header-row">
          <h3 class="card-title">${p.title}</h3>
          <div class="card-rating">★ 4.8</div>
        </div>
        <p class="card-location">${p.city}, ${p.state || 'India'}</p>
        <div class="card-price-row">
          <span class="price-amount">₹ ${p.pricePerNight}</span>
          <span class="price-unit">/ night</span>
        </div>
      </div>
    </div>
  `;
}

function toggleWishlist(propId) {
  if (wishlist.includes(propId)) {
    wishlist = wishlist.filter(id => id !== propId);
  } else {
    wishlist.push(propId);
    showToast('Saved to your wishlist ❤️');
  }
  localStorage.setItem('nestaway_wishlist', JSON.stringify(wishlist));
  updateUserUI();
  renderProperties();
}

function filterCategory(category) {
  document.querySelectorAll('.category-pill').forEach(btn => btn.classList.remove('active'));
  event.currentTarget.classList.add('active');

  const homeGrid = document.getElementById('home-recommended-grid');
  if (!homeGrid) return;

  let filtered = [...currentProperties];
  if (category === 'villas') filtered = filtered.filter(p => p.propertyType === 'VILLA' || p.title?.toLowerCase().includes('villa'));
  if (category === 'pool') filtered = filtered.filter(p => p.description?.toLowerCase().includes('pool') || p.title?.toLowerCase().includes('pool'));
  if (category === 'beach') filtered = filtered.filter(p => p.city?.toLowerCase() === 'goa' || p.title?.toLowerCase().includes('beach'));
  if (category === 'studios') filtered = filtered.filter(p => p.title?.toLowerCase().includes('studio') || p.propertyType === 'APARTMENT');
  if (category === 'flats') filtered = filtered.filter(p => p.propertyType === 'APARTMENT' || p.title?.toLowerCase().includes('flat'));

  if (filtered.length === 0) filtered = currentProperties;
  homeGrid.innerHTML = filtered.slice(0, 3).map(p => createPropertyCardHtml(p)).join('');
}

// Explore Filters
function handleHeroSearch(e) {
  e.preventDefault();
  const city = document.getElementById('hero-city-select').value;
  showView('explore');
  if (city) {
    document.getElementById('filter-city').value = city;
    applyExploreFilters();
  }
}

function applyExploreFilters() {
  const city = document.getElementById('filter-city').value;
  const priceFilter = document.getElementById('filter-price').value;

  let filtered = [...currentProperties];
  if (city) {
    filtered = filtered.filter(p => p.city?.toLowerCase() === city.toLowerCase());
  }
  if (priceFilter === 'under-2500') {
    filtered = filtered.filter(p => p.pricePerNight <= 2500);
  } else if (priceFilter === '2500-5000') {
    filtered = filtered.filter(p => p.pricePerNight > 2500 && p.pricePerNight <= 5000);
  } else if (priceFilter === 'above-5000') {
    filtered = filtered.filter(p => p.pricePerNight > 5000);
  }

  const exploreGrid = document.getElementById('explore-grid');
  if (filtered.length === 0) {
    exploreGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: var(--bg-subtle); border-radius: var(--radius-xl);">
        <h3>No Properties Found Matching Your Filter</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">Try resetting your filters to explore all available stays.</p>
      </div>
    `;
  } else {
    exploreGrid.innerHTML = filtered.map(p => createPropertyCardHtml(p)).join('');
  }
}

function resetExploreFilters() {
  document.getElementById('filter-city').value = '';
  document.getElementById('filter-price').value = '';
  renderProperties();
}

/// Property Detail & Full-Screen View & Payment Gateway Modal
function openPropertyDetailModal(propId) {
  openPropertyDetail(propId);
}

function openPropertyDetail(propId) {
  selectedProperty = currentProperties.find(p => p.id === propId);
  if (!selectedProperty) return;

  document.getElementById('detail-title').textContent = selectedProperty.title;
  document.getElementById('detail-location').textContent = `📍 ${selectedProperty.address || ''}, ${selectedProperty.city}, ${selectedProperty.state || 'India'}`;
  document.getElementById('detail-price').textContent = `₹ ${selectedProperty.pricePerNight}`;
  document.getElementById('detail-description').textContent = selectedProperty.description || 'Enjoy a luxurious and comfortable stay at this beautifully maintained home, featuring modern amenities, cozy interiors, and 24/7 support.';
  document.getElementById('detail-main-img').src = selectedProperty.coverImage || 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80';

  if (currentUser) {
    document.getElementById('page-book-name').value = currentUser.name || '';
    document.getElementById('page-book-phone').value = currentUser.phone || '9876543210';
  }

  setupPageDates();
  updatePriceBreakup();
  showView('property-detail');
}

function setupPageDates() {
  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];

  const checkIn = document.getElementById('page-book-checkin');
  const checkOut = document.getElementById('page-book-checkout');
  if (checkIn && checkOut) {
    checkIn.value = today;
    checkOut.value = tomorrow;

    checkIn.onchange = updatePriceBreakup;
    checkOut.onchange = updatePriceBreakup;
  }
}

function updatePriceBreakup() {
  if (!selectedProperty) return;

  const checkInStr = document.getElementById('page-book-checkin').value;
  const checkOutStr = document.getElementById('page-book-checkout').value;

  let nights = 1;
  if (checkInStr && checkOutStr) {
    const d1 = new Date(checkInStr);
    const d2 = new Date(checkOutStr);
    const diffTime = d2 - d1;
    if (diffTime > 0) {
      nights = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    }
  }

  const rate = selectedProperty.pricePerNight || 0;
  const cleaningFee = 250;
  const total = (rate * nights) + cleaningFee;

  document.getElementById('summary-nightly-rate').textContent = `₹ ${rate}`;
  document.getElementById('summary-nights-count').textContent = `${nights} ${nights === 1 ? 'Night' : 'Nights'}`;
  document.getElementById('summary-total-price').textContent = `₹ ${total}`;
}

let currentPaymentDetails = null;

function handleProceedToPayment(e) {
  e.preventDefault();
  if (!currentUser) {
    openAuthModal('login');
    showToast('Please log in to proceed with booking');
    return;
  }

  const checkInDate = document.getElementById('page-book-checkin').value;
  const checkOutDate = document.getElementById('page-book-checkout').value;
  const occupantName = document.getElementById('page-book-name').value;
  const occupantPhone = document.getElementById('page-book-phone').value;

  const totalText = document.getElementById('summary-total-price').textContent;

  currentPaymentDetails = {
    checkInDate,
    checkOutDate,
    occupantName,
    occupantPhone,
    totalAmount: totalText
  };

  document.getElementById('pay-property-title').textContent = selectedProperty.title;
  document.getElementById('pay-stay-dates').textContent = `${checkInDate} to ${checkOutDate}`;
  document.getElementById('pay-total-amount').textContent = totalText;

  document.getElementById('payment-modal').classList.add('show');
}

function closePaymentModal() {
  document.getElementById('payment-modal').classList.remove('show');
}

function switchPayTab(method) {
  document.querySelectorAll('#payment-modal .tab-btn').forEach(btn => btn.classList.remove('active'));
  const activeBtn = document.getElementById(`pay-tab-${method}`);
  if (activeBtn) activeBtn.classList.add('active');

  document.getElementById('pay-upi-view').style.display = method === 'upi' ? 'block' : 'none';
  document.getElementById('pay-card-view').style.display = method === 'card' ? 'block' : 'none';
  document.getElementById('pay-cash-view').style.display = method === 'cash' ? 'block' : 'none';
}

async function confirmFinalPayment() {
  if (!selectedProperty || !currentUser || !currentPaymentDetails) return;

  try {
    const res = await fetch(`${BASE_URL}/bookings/property/${selectedProperty.id}/guest/${currentUser.id}`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({
        checkInDate: currentPaymentDetails.checkInDate,
        checkOutDate: currentPaymentDetails.checkOutDate,
        totalGuests: 2,
        occupantName: currentPaymentDetails.occupantName,
        occupantPhone: currentPaymentDetails.occupantPhone
      })
    });

    if (res.ok) {
      closePaymentModal();
      showToast('🎉 Payment Received & Stay Booked Successfully!');
      showView('bookings');
    } else {
      let errorMsg = `Server error (${res.status})`;
      try {
        const text = await res.text();
        try {
          const errData = JSON.parse(text);
          if (typeof errData === 'string') {
            errorMsg = errData;
          } else if (errData) {
            errorMsg = errData.message || errData.error || JSON.stringify(errData);
          }
        } catch(e) {
          if (text && text.trim().length > 0) errorMsg = text;
        }
      } catch (err){}
      alert(`⚠️ Booking Failed (Status ${res.status}): ${errorMsg}`);
    }
  } catch (err) {
    alert('⚠️ Network error connecting to booking service');
  }
}

function setupInitialDates() {
  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];

  const checkIn = document.getElementById('book-checkin');
  const checkOut = document.getElementById('book-checkout');
  if (checkIn && checkOut) {
    checkIn.value = today;
    checkOut.value = tomorrow;
  }
}

// ----------------------------------------------------
// HOST DASHBOARD APIs
// ----------------------------------------------------
async function loadHostDashboard() {
  if (!currentUser || currentUser.role !== 'HOST') return;

  // Load Host Properties
  try {
    const res = await fetch(`${BASE_URL}/properties/host/${currentUser.id}`, { headers: getHeaders() });
    if (res.ok) {
      const properties = await res.json();
      const grid = document.getElementById('host-properties-grid');
      if (properties.length === 0) {
        grid.innerHTML = `<p style="grid-column: 1/-1; color: var(--text-muted);">You have not listed any properties yet. Click 'Add New Listing' to post your first home!</p>`;
      } else {
        grid.innerHTML = properties.map(p => createPropertyCardHtml(p)).join('');
      }
    }
  } catch (err) { console.warn(err); }

  // Load Received Bookings
  try {
    const res = await fetch(`${BASE_URL}/bookings/host/${currentUser.id}`, { headers: getHeaders() });
    if (res.ok) {
      const bookings = await res.json();
      const tbody = document.getElementById('host-bookings-tbody');
      if (bookings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--text-muted);">No bookings received yet.</td></tr>`;
      } else {
        tbody.innerHTML = bookings.map(b => `
          <tr>
            <td>#${b.id}</td>
            <td><strong>${b.propertyTitle}</strong></td>
            <td>${b.guestName}</td>
            <td>${b.checkInDate} to ${b.checkOutDate}</td>
            <td>${b.totalGuests} Guests</td>
            <td>₹ ${b.totalPrice}</td>
            <td><span class="badge badge-${b.status?.toLowerCase()}">${b.status}</span></td>
            <td>
              ${b.status === 'PENDING' ? `<button class="btn-primary" style="padding:0.35rem 0.75rem; font-size:0.8rem;" onclick="confirmHostBooking(${b.id})">Confirm</button>` : 'Confirmed'}
            </td>
          </tr>
        `).join('');
      }
    }
  } catch (err) { console.warn(err); }
}

function openAddPropertyModal() {
  document.getElementById('add-property-modal').classList.add('show');
}
function closeAddPropertyModal() {
  document.getElementById('add-property-modal').classList.remove('show');
}

async function handleAddPropertySubmit(e) {
  e.preventDefault();
  const title = document.getElementById('prop-title').value;
  const propertyType = document.getElementById('prop-type').value;
  const city = document.getElementById('prop-city').value;
  const pricePerNight = Number(document.getElementById('prop-price').value);
  const bedrooms = Number(document.getElementById('prop-bedrooms').value);
  const beds = Number(document.getElementById('prop-beds').value);
  const bathrooms = Number(document.getElementById('prop-bathrooms').value);
  const address = document.getElementById('prop-address').value;
  const description = document.getElementById('prop-desc').value;

  try {
    const res = await fetch(`${BASE_URL}/properties/host/${currentUser.id}`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({
        title, propertyType, city, state: 'State', country: 'India',
        pricePerNight, maxGuests: 4, bedrooms, beds, bathrooms, address, description
      })
    });

    if (res.ok) {
      closeAddPropertyModal();
      showToast('Property listing submitted! Pending Admin Approval.');
      loadHostDashboard();
    } else {
      alert('Failed to add property listing.');
    }
  } catch (err) { alert('Network error adding property'); }
}

async function confirmHostBooking(bookingId) {
  try {
    const res = await fetch(`${BASE_URL}/bookings/${bookingId}/host/${currentUser.id}/confirm`, {
      method: 'PUT',
      headers: getHeaders()
    });
    if (res.ok) {
      showToast('Booking confirmed!');
      loadHostDashboard();
    }
  } catch (err) { console.warn(err); }
}

// ----------------------------------------------------
// ADMIN DASHBOARD APIs
// ----------------------------------------------------
async function loadAdminDashboard() {
  if (!currentUser || currentUser.role !== 'ADMIN') return;
  switchAdminTab('pending');
}

function switchAdminTab(tabName) {
  document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
  document.getElementById(`tab-btn-${tabName}`).classList.add('active');

  ['pending', 'approved', 'hosts', 'guests'].forEach(t => {
    document.getElementById(`admin-${t}-tab`).style.display = t === tabName ? 'block' : 'none';
  });

  if (tabName === 'pending') fetchAdminPending();
  if (tabName === 'approved') fetchAdminApproved();
  if (tabName === 'hosts') fetchAdminHosts();
  if (tabName === 'guests') fetchAdminGuests();
}

async function fetchAdminPending() {
  try {
    const res = await fetch(`${BASE_URL}/properties/admin/${currentUser.id}/pending`, { headers: getHeaders() });
    if (res.ok) {
      const list = await res.json();
      const grid = document.getElementById('admin-pending-grid');
      if (list.length === 0) {
        grid.innerHTML = `<p style="grid-column: 1/-1; color: var(--text-muted);">No pending property submissions queue.</p>`;
      } else {
        grid.innerHTML = list.map(p => `
          <div class="property-card">
            <div class="card-body">
              <h3>${p.title}</h3>
              <p>${p.city} | ₹${p.pricePerNight}/night</p>
              <div style="display:flex; gap:0.5rem; margin-top:1rem;">
                <button class="btn-primary" style="flex:1;" onclick="approveAdminProperty(${p.id})">Approve</button>
                <button class="btn-secondary" style="flex:1;" onclick="rejectAdminProperty(${p.id})">Reject</button>
              </div>
            </div>
          </div>
        `).join('');
      }
    }
  } catch (err) { console.warn(err); }
}

async function approveAdminProperty(propId) {
  try {
    const res = await fetch(`${BASE_URL}/properties/${propId}/admin/${currentUser.id}/approve`, { method: 'PUT', headers: getHeaders() });
    if (res.ok) { showToast('Property approved live!'); fetchAdminPending(); }
  } catch (err) { console.warn(err); }
}

async function rejectAdminProperty(propId) {
  try {
    const res = await fetch(`${BASE_URL}/properties/${propId}/admin/${currentUser.id}/reject`, { method: 'PUT', headers: getHeaders() });
    if (res.ok) { showToast('Property rejected.'); fetchAdminPending(); }
  } catch (err) { console.warn(err); }
}

async function fetchAdminApproved() {
  const grid = document.getElementById('admin-approved-grid');
  grid.innerHTML = currentProperties.map(p => createPropertyCardHtml(p)).join('');
}

async function fetchAdminHosts() {
  try {
    const res = await fetch(`${BASE_URL}/admin/hosts`, { headers: getHeaders() });
    if (res.ok) {
      const hosts = await res.json();
      const tbody = document.getElementById('admin-hosts-tbody');
      tbody.innerHTML = hosts.map(h => `
        <tr>
          <td>#${h.id}</td>
          <td><strong>${h.name}</strong></td>
          <td>${h.email}</td>
          <td>${h.phone || 'N/A'}</td>
          <td><span class="badge badge-approved">${h.propertyCount || 0} Homes</span></td>
          <td>${h.createdAt ? new Date(h.createdAt).toLocaleDateString() : 'N/A'}</td>
          <td><button class="btn-secondary" style="padding:0.35rem 0.65rem; font-size:0.8rem;" onclick="showToast('Viewing Host Properties...')">View Properties</button></td>
        </tr>
      `).join('');
    }
  } catch (err) { console.warn(err); }
}

async function fetchAdminGuests() {
  try {
    const res = await fetch(`${BASE_URL}/admin/guests`, { headers: getHeaders() });
    if (res.ok) {
      const guests = await res.json();
      const tbody = document.getElementById('admin-guests-tbody');
      tbody.innerHTML = guests.map(g => `
        <tr>
          <td>#${g.id}</td>
          <td><strong>${g.name}</strong></td>
          <td>${g.email}</td>
          <td>${g.phone || 'N/A'}</td>
          <td><span class="badge badge-approved">${g.bookingCount || 0} Bookings</span></td>
          <td>${g.createdAt ? new Date(g.createdAt).toLocaleDateString() : 'N/A'}</td>
          <td><button class="btn-secondary" style="padding:0.35rem 0.65rem; font-size:0.8rem;" onclick="showToast('Viewing Guest Bookings...')">View Stays</button></td>
        </tr>
      `).join('');
    }
  } catch (err) { console.warn(err); }
}

// ----------------------------------------------------
// GUEST BOOKINGS & PROFILE
// ----------------------------------------------------
async function loadGuestBookings() {
  if (!currentUser) return;
  try {
    const res = await fetch(`${BASE_URL}/bookings/guest/${currentUser.id}`, { headers: getHeaders() });
    if (res.ok) {
      const bookings = await res.json();
      const tbody = document.getElementById('guest-bookings-tbody');
      if (bookings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--text-muted);">You have no booked stays yet.</td></tr>`;
      } else {
        tbody.innerHTML = bookings.map(b => `
          <tr>
            <td>#${b.id}</td>
            <td><strong>${b.propertyTitle}</strong></td>
            <td>${b.checkInDate}</td>
            <td>${b.checkOutDate}</td>
            <td>${b.occupantName}</td>
            <td>₹ ${b.totalPrice}</td>
            <td><span class="badge badge-${b.status?.toLowerCase()}">${b.status}</span></td>
            <td>
              ${b.status !== 'CANCELLED' ? `<button class="btn-secondary" style="padding:0.35rem 0.65rem; font-size:0.8rem; color:#DC2626;" onclick="cancelGuestBooking(${b.id})">Cancel</button>` : 'Cancelled'}
            </td>
          </tr>
        `).join('');
      }
    }
  } catch (err) { console.warn(err); }
}

async function cancelGuestBooking(bookingId) {
  try {
    const res = await fetch(`${BASE_URL}/bookings/${bookingId}/guest/${currentUser.id}/cancel`, { method: 'PUT', headers: getHeaders() });
    if (res.ok) {
      showToast('Booking cancelled');
      loadGuestBookings();
    }
  } catch (err) { console.warn(err); }
}

function loadProfileView() {
  if (!currentUser) return;
  document.getElementById('profile-name').value = currentUser.name;
  document.getElementById('profile-email').value = currentUser.email;
  document.getElementById('profile-phone').value = currentUser.phone || 'N/A';
  document.getElementById('profile-role').value = currentUser.role;

  const upgradeWrapper = document.getElementById('upgrade-host-wrapper');
  if (currentUser.role === 'GUEST') {
    upgradeWrapper.style.display = 'block';
  } else {
    upgradeWrapper.style.display = 'none';
  }
}

async function upgradeToHost() {
  try {
    const res = await fetch(`${BASE_URL}/users/${currentUser.id}/upgrade-to-host`, { method: 'PUT', headers: getHeaders() });
    if (res.ok) {
      currentUser.role = 'HOST';
      localStorage.setItem('nestaway_user', JSON.stringify(currentUser));
      updateUserUI();
      showToast('🎉 Account upgraded to Host!');
      showView('host');
    }
  } catch (err) { alert('Failed to upgrade account'); }
}
