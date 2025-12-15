# Order Management System - Backend

## Quick Start

1. **Setup Database**
   ```bash
   php setup_database.php
   ```

2. **Configure Database**
   Edit `config.php` with your credentials

3. **Access Admin Panel**
   ```
   http://your-domain.com/backend/admin/login.php
   ```
   Login: `admin` / `admin123`

4. **Asset Versioning**
   See [VERSIONING.md](VERSIONING.md) for cache busting system

## Structure

```
backend/
├── admin/              # Admin web interface (PHP)
├── api/                # REST API endpoints
├── assets/
│   ├── css/           # Stylesheets (versioned)
│   └── js/            # JavaScript files (versioned)
├── config/
│   └── version.php    # Asset version for cache busting
├── config.php         # Database configuration
└── setup_database.php # Database setup
```

## Features

- ✅ Order Management (view, update status)
- ✅ Product Management (CRUD with discount pricing)
- ✅ Category Management (with images)
- ✅ User Management
- ✅ Dashboard Analytics
- ✅ Responsive Design
- ✅ Asset Versioning (automatic cache busting)
- ✅ Currency: Indian Rupee (₹)

## API Endpoints

- `/api/auth.php` - Authentication
- `/api/products.php` - Products
- `/api/categories.php` - Categories
- `/api/orders.php` - Orders
- `/api/admin.php` - Admin operations

## New Features

### Product Discounts
- Set `discount_price` lower than `price` to show discount
- Admin panel displays original price (strikethrough) and sale price
- "SALE" badge appears on discounted products
- Mobile app automatically uses discount price

### Category Images
- Add/edit category images via Image URL field
- Images display in categories table
- Supports any image URL

## Security

⚠️ Before production:
- Change default admin password
- Enable HTTPS
- Update database credentials
- Restrict file access
