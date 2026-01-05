# Order Management System

A complete full-stack order management solution with Android mobile app and web-based admin panel.

![Android](https://img.shields.io/badge/Android-Kotlin-green)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![PHP](https://img.shields.io/badge/Backend-PHP-purple)
![MySQL](https://img.shields.io/badge/Database-MySQL-orange)

## Features

### Mobile App (Android)
- Product browsing with categories
- Search and filter products
- Shopping cart management
- Discount pricing support
- Order placement and tracking
- User authentication (Login/Register)
- Featured products
- Order history
- Modern Material Design 3 UI
- Responsive design

### Admin Web Panel
- Real-time dashboard with statistics
- Order management (view, update status, track)
- Product management (CRUD operations)
- Discount pricing management
- Category management with images
- User management
- Live image previews
- Search and filter functionality
- Responsive design (mobile, tablet, desktop)

### Backend API
- Secure authentication
- RESTful API architecture
- MySQL database
- Input sanitization & validation
- CORS enabled
- Optimized queries

## Tech Stack

### Mobile App
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit + OkHttp
- **JSON Parsing:** Gson
- **Design:** Material Design 3
- **Navigation:** Compose Navigation

### Backend
- **Language:** PHP 7.4+
- **Database:** MySQL 5.7+ / MariaDB 10.2+
- **API:** RESTful
- **Server:** Apache/Nginx

### Admin Panel
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla)
- **Design:** Responsive, Modern UI
- **API Integration:** Fetch API

## Prerequisites

### For Mobile App
- Android Studio (latest version)
- Android SDK (API 24+)
- Kotlin plugin

### For Backend
- PHP 7.4 or higher
- MySQL 5.7+ or MariaDB 10.2+
- Apache/Nginx web server
- PHP extensions: mysqli, json

## Installation & Setup

### 1. Backend Setup

#### Step 1: Upload Backend Files
Upload the entire `backend` folder to your PHP server:
```
your-server.com/
└── backend/
    ├── admin/
    ├── api/
    ├── assets/
    ├── config.php
    └── setup_database.php
```

#### Step 2: Configure Database
Edit `backend/config.php` with your database credentials:
```php
$host = "your-database-host";      // Usually "localhost"
$user = "your-database-username";
$password = "your-database-password";
$dbname = "your-database-name";
```

#### Step 3: Create Database
Create a new MySQL database:
```sql
CREATE DATABASE your_database_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Step 4: Run Database Setup
Access the setup script in your browser:
```
http://your-server.com/backend/setup_database.php
```

Or run via command line:
```bash
cd backend
php setup_database.php
```

This will:
- Create all necessary tables (users, categories, products, orders, order_items)
- Insert sample categories with images
- Insert sample products with discounts

#### Step 5: Verify Installation
Test the API:
```
http://your-server.com/backend/api/products.php?action=list
```

You should see a JSON response with sample products.

### 2. Mobile App Setup

#### Step 1: Clone/Download Project
```bash
git clone <your-repository-url>
cd BuiltOrderManagement
```

#### Step 2: Configure Server URL
Open `app/src/main/java/com/king/builtordermanagement/data/api/RetrofitClient.kt`

**Update the BASE_URL with your server URL:**
```kotlin
private const val BASE_URL = "http://your-server.com/backend/"
```

**Examples:**
```kotlin
// For live server
private const val BASE_URL = "https://yourdomain.com/backend/"

// For local testing (use your computer's IP, not localhost)
private const val BASE_URL = "http://192.168.1.100/backend/"

// For shared hosting
private const val BASE_URL = "https://yoursite.com/backend/"
```

⚠️ **Important Notes:**
- Always end the URL with `/backend/`
- Use `https://` for production (secure)
- For local testing, use your computer's IP address (not `localhost` or `127.0.0.1`)
- Make sure your server is accessible from your Android device

#### Step 3: Open in Android Studio
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project folder
4. Wait for Gradle sync to complete

#### Step 4: Build and Run
1. Connect your Android device or start an emulator
2. Click "Run" (green play button) or press `Shift + F10`
3. Select your device
4. Wait for the app to install and launch

### 3. Admin Panel Setup

#### Access Admin Panel
Open in your browser:
```
http://your-server.com/backend/admin/login.html
```

#### Default Login Credentials
```
Username: admin
Password: admin123
```

**Security Warning:** Change the default password before going to production!

## Mobile App Usage

### First Time Setup
1. Launch the app
2. Register a new account or login
3. Browse products by category
4. Add products to cart
5. Proceed to checkout
6. Track your orders

### Features Guide
- **Home:** Browse featured products and categories
- **Categories:** Filter products by category
- **Search:** Find products quickly
- **Cart:** Review items before checkout
- **Orders:** Track order status and history
- **Profile:** Manage account settings

## Admin Panel Usage

### Dashboard
- View total orders, pending orders, products, and revenue
- See recent orders at a glance

### Managing Orders
1. Click "Orders" in sidebar
2. Filter by status or search
3. Click "View" to see order details
4. Update order status (pending → processing → shipped → delivered)

### Managing Products
1. Click "Products" in sidebar
2. Click "+ Add Product" to create new
3. Fill in details:
   - Name, description
   - Price (required)
   - Discount price (optional - for sales)
   - Stock quantity
   - Category
   - Image URL
   - Featured status
4. Click "Save"

**Adding Discounts:**
- Enter regular price: `$99.99`
- Enter discount price: `$79.99`
- Product will show with "SALE" badge

### Managing Categories
1. Click "Categories" in sidebar
2. Click "+ Add Category"
3. Enter name, description, and image URL
4. Images will preview as you type
5. Click "Save"

### Managing Users
- View all registered users
- See order count per user
- Track registration dates

## Configuration

### Server Requirements
- PHP 7.4+
- MySQL 5.7+ or MariaDB 10.2+
- Apache with mod_rewrite OR Nginx
- PHP extensions: mysqli, json
- Memory limit: 128MB minimum
- Upload max filesize: 10MB minimum

### Android Requirements
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Compile SDK: API 34

### Network Configuration
Ensure your server allows:
- CORS requests (already configured in `config.php`)
- POST, GET, PUT, DELETE methods
- JSON content type

## Project Structure

```
BuiltOrderManagement/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/king/builtordermanagement/
│           │   ├── data/
│           │   │   ├── api/          # Retrofit API interfaces
│           │   │   ├── models/       # Data models
│           │   │   └── repository/   # Data repositories
│           │   ├── ui/
│           │   │   ├── components/   # Reusable UI components
│           │   │   ├── screens/      # App screens
│           │   │   └── theme/        # Material Design theme
│           │   ├── viewmodel/        # ViewModels
│           │   └── MainActivity.kt
│           └── res/                  # Resources
│
└── backend/
    ├── admin/
    │   ├── index.html               # Admin dashboard
    │   └── login.html               # Admin login
    ├── api/
    │   ├── admin.php                # Admin operations
    │   ├── auth.php                 # Authentication
    │   ├── categories.php           # Categories API
    │   ├── orders.php               # Orders API
    │   └── products.php             # Products API
    ├── assets/
    │   ├── css/
    │   │   └── styles.css           # Admin panel styles
    │   └── js/
    │       └── script.js            # Admin panel logic
    ├── config.php                   # Database configuration
    ├── setup_database.php           # Database setup script
    └── README.md                    # Backend documentation
```

## API Endpoints

### Authentication
- `POST /api/auth.php?action=register` - Register new user
- `POST /api/auth.php?action=login` - User login

### Products
- `GET /api/products.php?action=list` - Get all products
- `GET /api/products.php?action=featured` - Get featured products
- `GET /api/products.php?action=category&category_id={id}` - Get products by category
- `GET /api/products.php?action=search&q={query}` - Search products
- `GET /api/products.php?action=detail&id={id}` - Get product details

### Categories
- `GET /api/categories.php?action=list` - Get all categories

### Orders
- `POST /api/orders.php?action=create` - Create new order
- `GET /api/orders.php?action=list&user_id={id}` - Get user orders
- `GET /api/orders.php?action=detail&order_id={id}` - Get order details
- `POST /api/orders.php?action=cancel` - Cancel order

### Admin (Authentication Required)
- `GET /api/admin.php?action=stats` - Dashboard statistics
- `GET /api/admin.php?action=all_orders` - All orders
- `POST /api/admin.php?action=update_order_status` - Update order status
- `POST /api/admin.php?action=create_product` - Create product
- `POST /api/admin.php?action=update_product` - Update product
- `POST /api/admin.php?action=delete_product` - Delete product
- `POST /api/admin.php?action=create_category` - Create category
- `POST /api/admin.php?action=update_category` - Update category
- `POST /api/admin.php?action=delete_category` - Delete category
- `GET /api/admin.php?action=users` - Get all users

## Security

### Before Going to Production

1. **Change Admin Password**
   - Implement proper authentication system
   - Use password hashing (bcrypt)

2. **Enable HTTPS**
   - Get SSL certificate (Let's Encrypt is free)
   - Force HTTPS in `.htaccess`

3. **Update Database Credentials**
   - Use strong passwords
   - Restrict database user permissions

4. **Secure Files**
   - Set proper file permissions (644 for files, 755 for directories)
   - Protect sensitive files via `.htaccess`

5. **Update CORS Settings**
   - In `config.php`, change `Access-Control-Allow-Origin` from `*` to your domain

6. **Environment Variables**
   - Move sensitive data to environment variables
   - Don't commit credentials to version control

## Troubleshooting

### Mobile App Issues

**"Unable to connect to server"**
- Check if BASE_URL is correct in `RetrofitClient.kt`
- Ensure server is accessible from your device
- Use IP address instead of localhost for local testing
- Check if server is running

**"Network error"**
- Verify internet connection
- Check if API endpoints are working (test in browser)
- Ensure CORS is enabled on server

**App crashes on launch**
- Check Android Studio Logcat for errors
- Verify all dependencies are installed
- Clean and rebuild project

### Backend Issues

**"Database connection failed"**
- Verify credentials in `config.php`
- Check if MySQL service is running
- Ensure database exists

**"404 Not Found" on API calls**
- Check if `.htaccess` is enabled
- Verify file paths are correct
- Check server configuration

**CORS errors**
- Verify CORS headers in `config.php`
- Check if server allows cross-origin requests

### Admin Panel Issues

**Can't login**
- Use default credentials: admin / admin123
- Clear browser cache
- Check browser console for errors

**Images not loading**
- Verify image URLs are accessible
- Check internet connection
- Ensure URLs use HTTPS if site uses HTTPS

## Database Schema

### users
- id, name, email, phone, password_hash, address, created_at

### categories
- id, name, image_url, description, created_at

### products
- id, category_id, name, description, price, discount_price, image_url, stock, rating, is_featured, created_at

### orders
- id, user_id, total_amount, status, shipping_address, payment_method, notes, created_at

### order_items
- id, order_id, product_id, quantity, price

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the [GNU Affero General Public License v3.0](https://github.com/KushxKalsi/BuiltOrderManagement/blob/main_branch/LICENSE.md).

## 👨‍💻 Author

Built with ❤️ by Kush Kalsi

## Support

For issues and questions:
- Open an issue on GitHub
- Email: me@kushkalsi.in

## Acknowledgments

- Material Design 3 for UI components
- Unsplash for sample product images
- Android Jetpack libraries
- PHP community

---

**⭐ If you find this project helpful, please give it a star!**

## 📸 Screenshots

### Mobile App
<img src="https://github.com/KushxKalsi/BuiltOrderManagement/blob/main_branch/apk/screenshots/Screenshot_20251212-112925.png" width="49%" height="300px"/>

<img src="https://github.com/KushxKalsi/BuiltOrderManagement/blob/main_branch/apk/screenshots/Screenshot_20251212-113013.png" width="49%" height="300px"/>

<img src="https://github.com/KushxKalsi/BuiltOrderManagement/blob/main_branch/apk/screenshots/Screenshot_20251212-113025.png" width="49%" height="300px"/>

### Admin Panel
<img src="https://github.com/KushxKalsi/BuiltOrderManagement/blob/main_branch/apk/screenshots/image.png" width="49%" height="300px"/>

**Happy Coding! 🚀**
