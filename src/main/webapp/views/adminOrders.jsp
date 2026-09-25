<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Orders | Admin</title><link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"></head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-dark"><a class="navbar-brand" href="/admin/">Harvest &amp; Home Admin</a><div class="navbar-nav"><a class="nav-link" href="/admin/">Dashboard</a><a class="nav-link" href="/admin/logout">Logout</a></div></nav>
<main class="container-fluid py-4"><h1 class="mb-4">Customer orders</h1><c:choose><c:when test="${not empty orders}"><div class="table-responsive"><table class="table table-striped table-bordered bg-white"><thead class="thead-dark"><tr><th>Order</th><th>Customer</th><th>Total</th><th>Payment</th><th>Status</th><th>Address</th><th>Created</th></tr></thead><tbody><c:forEach var="order" items="${orders}"><tr><td>#${order.id}</td><td>${order.customer.username}</td><td>$${order.totalAmount}</td><td>${order.paymentMethod}</td><td>${order.status}</td><td>${order.shippingAddress}</td><td>${order.createdAt}</td></tr></c:forEach></tbody></table></div></c:when><c:otherwise><p class="alert alert-info">No orders have been placed yet.</p></c:otherwise></c:choose></main>
</body>
</html>
