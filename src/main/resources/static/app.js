// Wrap the whole file so variables do not become global.
(function () {
    'use strict';

    // Create the AngularJS application and attach one controller for the store page.
    angular.module('byteBazaarApp', [])
        .controller('StoreController', function ($http, $timeout) {
            // vm means "view model"; HTML uses it as "store".
            var vm = this;

            // Default page shown when the app opens.
            vm.view = 'catalog';

            // Data used by the catalog, cart, and orders screens.
            vm.products = [];
            vm.categories = [];
            vm.cart = {items: [], total: 0};
            vm.orders = [];

            // Form values connected with ng-model in index.html.
            vm.loginForm = {email: 'customer@bytebazaar.dev', password: 'password'};
            vm.registerForm = {};
            vm.shippingAddress = '';

            // Read saved login details from browser storage if the user logged in earlier.
            vm.user = JSON.parse(localStorage.getItem('byteBazaarUser') || 'null');
            vm.token = localStorage.getItem('byteBazaarToken');

            // If token exists, send it with every API request.
            if (vm.token) {
                $http.defaults.headers.common.Authorization = 'Bearer ' + vm.token;
            }

            // Change visible screen and load data for cart/orders when needed.
            vm.show = function (view) {
                // Cart and orders should be opened only after login.
                if ((view === 'cart' || view === 'orders') && !vm.user) {
                    vm.view = 'account';
                    vm.setError('Please sign in to continue.');
                    return;
                }

                // Update current screen name.
                vm.view = view;

                // Refresh cart when cart screen is opened.
                if (view === 'cart') {
                    vm.loadCart();
                }

                // Refresh orders when orders screen is opened.
                if (view === 'orders') {
                    vm.loadOrders();
                }
            };

            // Used by Angular filter to search products and filter by category.
            vm.productFilter = function (product) {
                // Convert search text to lowercase so search is case-insensitive.
                var query = (vm.search || '').toLowerCase();

                // Check search text against product fields.
                var matchesQuery = !query || [product.name, product.brand, product.category, product.description]
                    .join(' ').toLowerCase().indexOf(query) !== -1;

                // Check selected category. Empty category means all products.
                var matchesCategory = !vm.category || product.category === vm.category;

                // Product is shown only if both conditions match.
                return matchesQuery && matchesCategory;
            };

            // Load all products from backend.
            vm.loadProducts = function () {
                return $http.get('/api/products').then(function (response) {
                    // Save products returned by API.
                    vm.products = response.data;

                    // Build category dropdown from product categories.
                    vm.categories = Array.from(new Set(vm.products.map(function (product) {
                        return product.category;
                    })));
                }, vm.handleError);
            };

            // Send login form data to backend.
            vm.login = function () {
                return $http.post('/api/auth/login', vm.loginForm).then(vm.acceptAuth, vm.handleError);
            };

            // Send registration form data to backend.
            vm.register = function () {
                return $http.post('/api/auth/register', vm.registerForm).then(vm.acceptAuth, vm.handleError);
            };

            // Common code after successful login or registration.
            vm.acceptAuth = function (response) {
                // Store token and user from API response.
                vm.token = response.data.token;
                vm.user = response.data.user;

                // Save login data in browser storage so page refresh keeps user logged in.
                localStorage.setItem('byteBazaarToken', vm.token);
                localStorage.setItem('byteBazaarUser', JSON.stringify(vm.user));

                // Add token to future API requests.
                $http.defaults.headers.common.Authorization = 'Bearer ' + vm.token;

                // Show success message and return to catalog.
                vm.setMessage('Signed in successfully.');
                vm.show('catalog');

                // Load cart for logged-in user.
                vm.loadCart();
            };

            // Logout user from backend and clear local login data.
            vm.logout = function () {
                $http.post('/api/auth/logout').finally(function () {
                    // Clear controller state.
                    vm.token = null;
                    vm.user = null;
                    vm.cart = {items: [], total: 0};

                    // Stop sending Authorization header.
                    delete $http.defaults.headers.common.Authorization;

                    // Remove saved login data from browser.
                    localStorage.removeItem('byteBazaarToken');
                    localStorage.removeItem('byteBazaarUser');

                    // Show message and go back to catalog.
                    vm.setMessage('Signed out.');
                    vm.view = 'catalog';
                });
            };

            // Load cart items for the logged-in user.
            vm.loadCart = function () {
                // No API call is needed if user is not logged in.
                if (!vm.user) {
                    return;
                }

                // Get cart from backend and save it for display.
                return $http.get('/api/cart').then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            // Add one quantity of selected product to cart.
            vm.addToCart = function (product) {
                // Ask user to login before adding item.
                if (!vm.user) {
                    vm.show('account');
                    return;
                }

                // Send product id and quantity to backend.
                return $http.post('/api/cart', {productId: product.id, quantity: 1}).then(function (response) {
                    vm.cart = response.data;
                    vm.setMessage(product.name + ' added to cart.');
                }, vm.handleError);
            };

            // Update quantity of a cart item.
            vm.updateCartItem = function (item) {
                // Keep quantity minimum as 1.
                if (!item.quantity || item.quantity < 1) {
                    item.quantity = 1;
                }

                // Send updated quantity to backend.
                return $http.patch('/api/cart/' + item.id, {quantity: item.quantity}).then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            // Remove one item from cart.
            vm.removeCartItem = function (item) {
                return $http.delete('/api/cart/' + item.id).then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            // Place order using current cart and shipping address.
            vm.checkout = function () {
                return $http.post('/api/orders', {shippingAddress: vm.shippingAddress}).then(function () {
                    // Clear local cart form after order is placed.
                    vm.shippingAddress = '';
                    vm.cart = {items: [], total: 0};

                    // Show success message and orders screen.
                    vm.setMessage('Order placed successfully.');
                    vm.show('orders');

                    // Reload products because stock changes after checkout.
                    vm.loadProducts();
                }, vm.handleError);
            };

            // Load all orders of logged-in user.
            vm.loadOrders = function () {
                // Orders are available only after login.
                if (!vm.user) {
                    return;
                }

                // Get orders from backend and save them for display.
                return $http.get('/api/orders').then(function (response) {
                    vm.orders = response.data;
                }, vm.handleError);
            };

            // Show success message for a few seconds.
            vm.setMessage = function (message) {
                vm.message = message;
                vm.error = '';
                $timeout(function () {
                    vm.message = '';
                }, 3500);
            };

            // Show error message for a few seconds.
            vm.setError = function (message) {
                vm.error = message;
                vm.message = '';
                $timeout(function () {
                    vm.error = '';
                }, 4500);
            };

            // Common error handler for failed API calls.
            vm.handleError = function (response) {
                // Use backend error message when available, otherwise show a simple fallback.
                var message = response.data && response.data.message ? response.data.message : 'Request failed.';
                vm.setError(message);
            };

            // Initial API calls when page loads.
            vm.loadProducts();
            vm.loadCart();
        });
})();
