<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Order confirmed | Harvest &amp; Home</title><link rel="stylesheet" href="/resources/storefront.css"></head>
<body>
<header class="topbar"><a class="brand" href="/"><span class="brand-mark">+</span> Harvest &amp; Home</a><nav class="nav-links"><a href="/user/products">Shop</a><a href="/logout">Log out</a></nav></header>
<main class="page-width"><section class="hero"><div><span class="eyebrow">Order confirmed</span><h1>Thank you for your order.</h1><p>Order #${order.id} is placed with cash on delivery. We will deliver it to ${order.shippingAddress}.</p><p class="price">Total: $${order.totalAmount}</p><a class="button" href="/user/products">Continue shopping</a></div><div class="hero-art" aria-hidden="true"></div></section></main>
<footer class="footer">Harvest &amp; Home &middot; Order status: ${order.status}</footer>
</body></html>
