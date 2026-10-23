/**
 * SkipMate Official Landing Page — Client Interactive Logic
 * Mobile Navigation • APK Download Toast • Tab Switcher • FAQ Accordion • Modals
 */

document.addEventListener('DOMContentLoaded', () => {
  initNavbar();
  initDownloadTracker();
  initAppPreviewTabs();
  initFaqAccordion();
  initModals();
  renderQrCode();
});

/* ==========================================================================
   1. Navbar & Mobile Drawer
   ========================================================================== */
function initNavbar() {
  const navbar = document.getElementById('navbar');
  const hamburgerBtn = document.getElementById('hamburgerBtn');
  const mobileDrawer = document.getElementById('mobileDrawer');
  const mobileLinks = document.querySelectorAll('.mobile-nav-link, .mobile-drawer-cta a');

  // Sticky header scroll styling
  window.addEventListener('scroll', () => {
    if (window.scrollY > 20) {
      navbar?.classList.add('scrolled');
    } else {
      navbar?.classList.remove('scrolled');
    }
  }, { passive: true });

  // Toggle mobile drawer
  if (hamburgerBtn && mobileDrawer) {
    const mobileDrawerCloseBtn = document.getElementById('mobileDrawerCloseBtn');

    hamburgerBtn.addEventListener('click', () => {
      const isOpen = mobileDrawer.classList.contains('open');
      toggleDrawer(!isOpen);
    });

    if (mobileDrawerCloseBtn) {
      mobileDrawerCloseBtn.addEventListener('click', () => {
        toggleDrawer(false);
      });
    }

    // Close mobile drawer when link is clicked & navigate smoothly
    mobileLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        const targetHref = link.getAttribute('href');
        toggleDrawer(false);
        if (targetHref && targetHref.startsWith('#') && targetHref !== '#') {
          const targetEl = document.querySelector(targetHref);
          if (targetEl) {
            e.preventDefault();
            setTimeout(() => {
              targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 80);
          }
        }
      });
    });

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && mobileDrawer.classList.contains('open')) {
        toggleDrawer(false);
      }
    });
  }

  function toggleDrawer(open) {
    if (open) {
      hamburgerBtn.classList.add('active');
      hamburgerBtn.setAttribute('aria-expanded', 'true');
      mobileDrawer.classList.add('open');
      mobileDrawer.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
    } else {
      hamburgerBtn.classList.remove('active');
      hamburgerBtn.setAttribute('aria-expanded', 'false');
      mobileDrawer.classList.remove('open');
      mobileDrawer.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';
    }
  }
}

/* ==========================================================================
   2. Download Tracking & Floating Notification
   ========================================================================== */
function initDownloadTracker() {
  const downloadButtons = document.querySelectorAll('.btn-download-track');
  const downloadToast = document.getElementById('downloadToast');
  const closeToastBtn = document.getElementById('closeToastBtn');
  const toastGuideLink = document.getElementById('toastGuideLink');
  let toastTimer = null;

  downloadButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      showDownloadToast();
    });
  });

  if (closeToastBtn && downloadToast) {
    closeToastBtn.addEventListener('click', () => {
      hideToast();
    });
  }

  if (toastGuideLink) {
    toastGuideLink.addEventListener('click', () => {
      hideToast();
      highlightFirstStep();
    });
  }

  function showDownloadToast() {
    if (!downloadToast) return;
    
    downloadToast.classList.add('show');
    clearTimeout(toastTimer);

    // Auto-dismiss after 8 seconds
    toastTimer = setTimeout(() => {
      hideToast();
    }, 8000);
  }

  function hideToast() {
    if (!downloadToast) return;
    downloadToast.classList.remove('show');
    clearTimeout(toastTimer);
  }

  function highlightFirstStep() {
    const firstStep = document.querySelector('.guide-step-item');
    if (firstStep) {
      firstStep.style.borderColor = 'var(--sky-blue)';
      firstStep.style.boxShadow = '0 0 20px var(--sky-glow)';
      setTimeout(() => {
        firstStep.style.borderColor = '';
        firstStep.style.boxShadow = '';
      }, 2500);
    }
  }
}

/* ==========================================================================
   3. Interactive App Preview Tabs
   ========================================================================== */
function initAppPreviewTabs() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabPanels = document.querySelectorAll('.preview-content');

  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-tab');
      if (!targetId) return;

      // Update button active state
      tabButtons.forEach(b => {
        b.classList.remove('active');
        b.setAttribute('aria-selected', 'false');
      });
      btn.classList.add('active');
      btn.setAttribute('aria-selected', 'true');

      // Update panel visibility
      tabPanels.forEach(panel => {
        if (panel.id === targetId) {
          panel.classList.add('active');
          panel.removeAttribute('hidden');
        } else {
          panel.classList.remove('active');
          panel.setAttribute('hidden', 'true');
        }
      });
    });
  });
}

/* ==========================================================================
   4. Accessible FAQ Accordion
   ========================================================================== */
function initFaqAccordion() {
  const accordionHeaders = document.querySelectorAll('.accordion-header');

  accordionHeaders.forEach(header => {
    header.addEventListener('click', () => {
      const item = header.closest('.accordion-item');
      const body = item?.querySelector('.accordion-body');
      if (!item || !body) return;

      const isExpanded = header.getAttribute('aria-expanded') === 'true';

      // Toggle this item
      if (isExpanded) {
        header.setAttribute('aria-expanded', 'false');
        body.setAttribute('hidden', 'true');
        item.classList.remove('open');
      } else {
        header.setAttribute('aria-expanded', 'true');
        body.removeAttribute('hidden');
        item.classList.add('open');
      }
    });

    // Keyboard support (Enter or Space)
    header.addEventListener('keydown', (e) => {
      if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        header.click();
      }
    });
  });
}

/* ==========================================================================
   5. Modals (Privacy Policy & Terms of Use)
   ========================================================================== */
function initModals() {
  const privacyBtn = document.getElementById('openPrivacyModal');
  const termsBtn = document.getElementById('openTermsModal');
  const privacyModal = document.getElementById('privacyModal');
  const termsModal = document.getElementById('termsModal');
  const closeBtns = document.querySelectorAll('[data-close-modal]');

  privacyBtn?.addEventListener('click', () => openModal(privacyModal));
  termsBtn?.addEventListener('click', () => openModal(termsModal));

  closeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal-backdrop');
      if (modal) closeModal(modal);
    });
  });

  // Close when clicking modal backdrop
  [privacyModal, termsModal].forEach(modal => {
    if (!modal) return;
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeModal(modal);
      }
    });
  });

  // Close on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (privacyModal?.classList.contains('open')) closeModal(privacyModal);
      if (termsModal?.classList.contains('open')) closeModal(termsModal);
    }
  });

  function openModal(modal) {
    if (!modal) return;
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }

  function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }
}

/* ==========================================================================
   6. Dynamic Desktop QR Code Generator (Zero-dependency Canvas QR)
   ========================================================================== */
function renderQrCode() {
  const canvas = document.getElementById('qrCanvas');
  if (!canvas || !canvas.getContext) return;

  const ctx = canvas.getContext('2d');
  const size = canvas.width;
  
  // Clear canvas
  ctx.fillStyle = '#FFFFFF';
  ctx.fillRect(0, 0, size, size);

  // Draw high-contrast decorative QR pattern with standard corner targets
  const cellSize = size / 21; // 21x21 modules standard Version 1 QR
  ctx.fillStyle = '#0B0D17';

  // Helper to draw QR finder pattern
  function drawFinder(startX, startY) {
    // 7x7 outer square
    ctx.fillRect(startX * cellSize, startY * cellSize, 7 * cellSize, 7 * cellSize);
    // 5x5 inner white
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect((startX + 1) * cellSize, (startY + 1) * cellSize, 5 * cellSize, 5 * cellSize);
    // 3x3 inner black
    ctx.fillStyle = '#0B0D17';
    ctx.fillRect((startX + 2) * cellSize, (startY + 2) * cellSize, 3 * cellSize, 3 * cellSize);
  }

  // Draw 3 corner finder patterns
  drawFinder(0, 0); // Top-left
  drawFinder(14, 0); // Top-right
  drawFinder(0, 14); // Bottom-left

  // Timing lines
  for (let i = 8; i < 13; i += 2) {
    ctx.fillRect(i * cellSize, 6 * cellSize, cellSize, cellSize);
    ctx.fillRect(6 * cellSize, i * cellSize, cellSize, cellSize);
  }

  // Alignment pattern in bottom-right
  const matrix = [
    [1,0,1,1,0,1,1],
    [0,1,0,0,1,0,1],
    [1,1,0,1,0,1,0],
    [0,1,1,0,1,1,1],
    [1,0,0,1,0,0,1],
    [0,1,1,1,0,1,0],
    [1,0,1,0,1,0,1]
  ];

  for (let r = 0; r < matrix.length; r++) {
    for (let c = 0; c < matrix[r].length; c++) {
      if (matrix[r][c] === 1) {
        ctx.fillRect((10 + c) * cellSize, (10 + r) * cellSize, cellSize, cellSize);
      }
    }
  }

  // Additional data modules
  const dataModules = [
    [8, 2], [8, 4], [9, 1], [9, 3], [10, 0], [10, 5],
    [12, 1], [13, 3], [1, 9], [3, 9], [5, 10], [2, 12],
    [8, 14], [8, 16], [9, 18], [10, 20], [12, 18], [13, 20]
  ];

  dataModules.forEach(([x, y]) => {
    ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
  });
}
