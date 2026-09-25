<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Your cart | Harvest &amp; Home</title><link rel="stylesheet" href="/resources/storefront.css"></head>
<body>
<header class="topbar"><a class="brand" href="/"><span class="brand-mark">+</span> Harvest &amp; Home</a><nav class="nav-links"><a href="/user/products">Shop</a><a href="/profileDisplay">Profile</a><a href="/logout">Log out</a></nav></header>
<main class="page-width"><section style="padding:55px 0 30px"><span class="eyebrow">Your selection</span><h1 style="font-size:clamp(3rem,6vw,5rem);margin:15px 0 0">Cart</h1></section>
<c:choose><c:when test="${not empty cartProducts}"><div class="product-grid"><c:forEach var="product" items="${cartProducts}"><article class="product-card"><div class="product-image"><img src="${product.image}" alt="${product.name}"></div><div class="product-info"><h3>${product.name}</h3><p class="product-meta">${product.category.name}<br>${product.description}</p><span class="price">$${product.price}</span><form action="/cart/remove" method="post" style="margin-top:14px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="productId" value="${product.id}"><button class="button button-light" type="submit">Remove</button></form></div></article></c:forEach></div></c:when><c:otherwise><p class="form-note">Your cart is empty. <a href="/user/products">Browse the collection</a>.</p></c:otherwise></c:choose></main>
<footer class="footer">Harvest &amp; Home &middot; Items are saved for this session.</footer>
</body></html>
