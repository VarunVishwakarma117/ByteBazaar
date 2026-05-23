(function () {
    'use strict';

    angular.module('byteBazaarApp', [])
        .controller('StoreController', function ($http, $timeout) {
            var vm = this;

            vm.view = 'catalog';
            vm.products = [];
            vm.categories = [];
            vm.cart = {items: [], total: 0};
            vm.orders = [];
            vm.loginForm = {email: 'customer@bytebazaar.dev', password: 'password'};
            vm.registerForm = {};
            vm.shippingAddress = '';
            vm.user = JSON.parse(localStorage.getItem('byteBazaarUser') || 'null');
            vm.token = localStorage.getItem('byteBazaarToken');

            if (vm.token) {
                $http.defaults.headers.common.Authorization = 'Bearer ' + vm.token;
            }

            vm.show = function (view) {
                if ((view === 'cart' || view === 'orders') && !vm.user) {
                    vm.view = 'account';
                    vm.setError('Please sign in to continue.');
                    return;
                }
                vm.view = view;
                if (view === 'cart') {
                    vm.loadCart();
                }
                if (view === 'orders') {
                    vm.loadOrders();
                }
            };

            vm.productFilter = function (product) {
                var query = (vm.search || '').toLowerCase();
                var matchesQuery = !query || [product.name, product.brand, product.category, product.description]
                    .join(' ').toLowerCase().indexOf(query) !== -1;
                var matchesCategory = !vm.category || product.category === vm.category;
                return matchesQuery && matchesCategory;
            };

            vm.loadProducts = function () {
                return $http.get('/api/products').then(function (response) {
                    vm.products = response.data;
                    vm.categories = Array.from(new Set(vm.products.map(function (product) {
                        return product.category;
                    })));
                }, vm.handleError);
            };

            vm.login = function () {
                return $http.post('/api/auth/login', vm.loginForm).then(vm.acceptAuth, vm.handleError);
            };

            vm.register = function () {
                return $http.post('/api/auth/register', vm.registerForm).then(vm.acceptAuth, vm.handleError);
            };

            vm.acceptAuth = function (response) {
                vm.token = response.data.token;
                vm.user = response.data.user;
                localStorage.setItem('byteBazaarToken', vm.token);
                localStorage.setItem('byteBazaarUser', JSON.stringify(vm.user));
                $http.defaults.headers.common.Authorization = 'Bearer ' + vm.token;
                vm.setMessage('Signed in successfully.');
                vm.show('catalog');
                vm.loadCart();
            };

            vm.logout = function () {
                $http.post('/api/auth/logout').finally(function () {
                    vm.token = null;
                    vm.user = null;
                    vm.cart = {items: [], total: 0};
                    delete $http.defaults.headers.common.Authorization;
                    localStorage.removeItem('byteBazaarToken');
                    localStorage.removeItem('byteBazaarUser');
                    vm.setMessage('Signed out.');
                    vm.view = 'catalog';
                });
            };

            vm.loadCart = function () {
                if (!vm.user) {
                    return;
                }
                return $http.get('/api/cart').then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            vm.addToCart = function (product) {
                if (!vm.user) {
                    vm.show('account');
                    return;
                }
                return $http.post('/api/cart', {productId: product.id, quantity: 1}).then(function (response) {
                    vm.cart = response.data;
                    vm.setMessage(product.name + ' added to cart.');
                }, vm.handleError);
            };

            vm.updateCartItem = function (item) {
                if (!item.quantity || item.quantity < 1) {
                    item.quantity = 1;
                }
                return $http.patch('/api/cart/' + item.id, {quantity: item.quantity}).then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            vm.removeCartItem = function (item) {
                return $http.delete('/api/cart/' + item.id).then(function (response) {
                    vm.cart = response.data;
                }, vm.handleError);
            };

            vm.checkout = function () {
                return $http.post('/api/orders', {shippingAddress: vm.shippingAddress}).then(function () {
                    vm.shippingAddress = '';
                    vm.cart = {items: [], total: 0};
                    vm.setMessage('Order placed successfully.');
                    vm.show('orders');
                    vm.loadProducts();
                }, vm.handleError);
            };

            vm.loadOrders = function () {
                if (!vm.user) {
                    return;
                }
                return $http.get('/api/orders').then(function (response) {
                    vm.orders = response.data;
                }, vm.handleError);
            };

            vm.setMessage = function (message) {
                vm.message = message;
                vm.error = '';
                $timeout(function () {
                    vm.message = '';
                }, 3500);
            };

            vm.setError = function (message) {
                vm.error = message;
                vm.message = '';
                $timeout(function () {
                    vm.error = '';
                }, 4500);
            };

            vm.handleError = function (response) {
                var message = response.data && response.data.message ? response.data.message : 'Request failed.';
                vm.setError(message);
            };

            vm.loadProducts();
            vm.loadCart();
        });
})();
