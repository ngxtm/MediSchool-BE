# Email Setup Guide for Vaccine Event Notifications

## Gmail SMTP Configuration

### Step 1: Enable 2-Step Verification
1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Navigate to **Security** → **2-Step Verification**
3. Enable 2-Step Verification if not already enabled

### Step 2: Generate App Password
1. In Google Account Settings, go to **Security** → **2-Step Verification**
2. Click on **App passwords** (appears after enabling 2-Step Verification)
3. Select **Mail** as the app and **Other** as the device
4. Click **Generate**
5. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

### Step 3: Configure Application

#### Option A: Direct Configuration (for development)
Edit `src/main/resources/application.properties`:
```properties
spring.mail.username=your-actual-gmail@gmail.com
spring.mail.password=your-16-character-app-password
```

#### Option B: Environment Variables (recommended for production)
Set environment variables:
```bash
# Windows PowerShell
$env:GMAIL_USERNAME="your-actual-gmail@gmail.com"
$env:GMAIL_APP_PASSWORD="your-16-character-app-password"

# Windows Command Prompt
set GMAIL_USERNAME=your-actual-gmail@gmail.com
set GMAIL_APP_PASSWORD=your-16-character-app-password

# Linux/Mac
export GMAIL_USERNAME="your-actual-gmail@gmail.com"
export GMAIL_APP_PASSWORD="your-16-character-app-password"
```

### Step 4: Test Configuration
1. Restart your Spring Boot application
2. Test the email notification endpoint:
   ```
   POST /api/vaccine-events/{eventId}/send-email-notifications
   ```

## Troubleshooting

### Common Issues:

1. **Authentication Failed (535-5.7.8)**
   - Ensure you're using an App Password, not your regular Gmail password
   - Verify 2-Step Verification is enabled
   - Check that the email address is correct

2. **Connection Timeout**
   - Check your internet connection
   - Verify firewall settings allow SMTP traffic on port 587

3. **"Less secure app access" errors**
   - Use App Passwords instead of enabling less secure app access
   - App Passwords are more secure and recommended by Google

### Security Notes:
- Never commit real email credentials to version control
- Use environment variables in production
- App Passwords are specific to the application and can be revoked
- Each App Password is 16 characters long (remove spaces when copying)

## Testing the Email Feature

After configuration, you can test the bulk email notification:

1. Ensure you have a vaccine event with pending consents
2. Call the API endpoint with the event ID
3. Check the response for success/failure counts
4. Verify emails are received in the target inboxes

## API Endpoint

```
POST /api/vaccine-events/{eventId}/send-email-notifications
```

Response format:
```json
{
  "totalConsents": 5,
  "emailsSent": 3,
  "emailsFailed": 2,
  "failedEmails": ["email1@example.com", "email2@example.com"],
  "message": "Bulk email notification completed"
}
``` 