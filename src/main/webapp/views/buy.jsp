<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout | Harvest &amp; Home</title>
    <link rel="stylesheet" href="/resources/storefront.css">
</head>
<body>
<header class="topbar"><a class="brand" href="/"><span class="brand-mark">+</span> Harvest &amp; Home</a><nav class="nav-links"><a href="/user/products">Shop</a><a href="/logout">Log out</a></nav></header>
<main class="page-width"><section style="padding:55px 0 30px"><span class="eyebrow">Checkout</span><h1 style="font-size:clamp(3rem,6vw,5rem);margin:15px 0 0">Complete your order</h1><p class="form-note">Your total is $${totalAmount}. Payment is cash on delivery.</p></section><c:if test="${not empty error}"><p class="alert">${error}</p></c:if><c:choose><c:when test="${not empty cartProducts}"><form action="/checkout" method="post" class="auth-form" style="max-width:620px;padding:0 0 55px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="field"><label for="shippingAddress">Shipping address</label><textarea id="shippingAddress" name="shippingAddress" rows="4" required placeholder="Where should we deliver your order?"></textarea></div><div class="field"><label for="paymentMethod">Payment method</label><select id="paymentMethod" name="paymentMethod" required><option value="CASH_ON_DELIVERY">Cash on delivery</option></select></div><button class="button" type="submit">Place order</button></form></c:when><c:otherwise><p class="form-note">Your cart is empty. <a href="/user/products">Browse the collection</a>.</p></c:otherwise></c:choose></main>
</body>
</html>