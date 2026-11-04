document.addEventListener('DOMContentLoaded', function () {
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const mobileBackdrop = document.getElementById('sidebarBackdrop');
    const body = document.body;

    // Load saved desktop sidebar state
    const savedState = localStorage.getItem('sportbooking_sidebar_collapsed');
    if (savedState === 'true' && window.innerWidth >= 992) {
        body.classList.add('sidebar-collapsed');
    }

    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', function () {
            if (window.innerWidth < 992) {
                // Mobile toggle
                body.classList.toggle('mobile-sidebar-open');
            } else {
                // Desktop toggle
                body.classList.toggle('sidebar-collapsed');
                localStorage.setItem('sportbooking_sidebar_collapsed', body.classList.contains('sidebar-collapsed'));
            }
        });
    }

    if (mobileBackdrop) {
        mobileBackdrop.addEventListener('click', function () {
            body.classList.remove('mobile-sidebar-open');
        });
    }

    // Auto close mobile sidebar when clicking a link
    const sidebarLinks = document.querySelectorAll('.admin-sidebar .sidebar-link');
    sidebarLinks.forEach(function (link) {
        link.addEventListener('click', function () {
            if (window.innerWidth < 992) {
                body.classList.remove('mobile-sidebar-open');
            }
        });
    });

    // Handle window resize
    window.addEventListener('resize', function () {
        if (window.innerWidth >= 992) {
            body.classList.remove('mobile-sidebar-open');
            const isCollapsed = localStorage.getItem('sportbooking_sidebar_collapsed') === 'true';
            if (isCollapsed) {
                body.classList.add('sidebar-collapsed');
            } else {
                body.classList.remove('sidebar-collapsed');
            }
        }
    });
});
