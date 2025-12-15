<?php require_once '../config/version.php'; ?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .login-container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            width: 100%;
            max-width: 400px;
        }

        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }

        .login-header h1 {
            color: #2c3e50;
            margin-bottom: 10px;
        }

        .login-header p {
            color: #7f8c8d;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #2c3e50;
            font-weight: 600;
        }

        .form-group input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn-login {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }

        .btn-login:hover {
            transform: translateY(-2px);
        }

        .error-message {
            background: #fee;
            color: #c33;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            display: none;
        }

        .error-message.show {
            display: block;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Admin Panel</h1>
            <p>Login to manage your store</p>
        </div>

        <div id="error-message" class="error-message"></div>

        <form id="login-form" onsubmit="handleLogin(event)">
            <div class="form-group">
                <label>Username</label>
                <input type="text" id="username" required placeholder="Enter username">
            </div>

            <div class="form-group">
                <label>Password</label>
                <input type="password" id="password" required placeholder="Enter password">
            </div>

            <button type="submit" class="btn-login">Login</button>
        </form>

        <p style="margin-top: 20px; text-align: center; color: #7f8c8d; font-size: 12px;">
            Default credentials: admin / admin123
        </p>
    </div>

    <script>
        function handleLogin(event) {
            event.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            // Simple authentication (replace with proper backend authentication)
            if (username === 'admin' && password === 'admin123') {
                // Store session (in production, use proper session management)
                localStorage.setItem('admin_logged_in', 'true');
                window.location.href = 'index.php';
            } else {
                showError('Invalid username or password');
            }
        }

        function showError(message) {
            const errorDiv = document.getElementById('error-message');
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
            
            setTimeout(() => {
                errorDiv.classList.remove('show');
            }, 3000);
        }

        // Check if already logged in
        if (localStorage.getItem('admin_logged_in') === 'true') {
            window.location.href = 'index.php';
        }
    </script>
</body>
</html>
