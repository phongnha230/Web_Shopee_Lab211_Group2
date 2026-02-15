$baseUrl = "http://localhost:8080/api"
$random = Get-Random
$email = "testuser_$random@example.com"
$password = "Password123"

Write-Host "1. Registering User: $email"
$registerBody = @{
    email = $email
    password = $password
    fullName = "Test User"
    phoneNumber = "0901234567"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
Start-Sleep -Seconds 2

# We need to manually verify OTP in DB or just force login if OTP is bypassed? 
# Wait, user registration sends OTP. We can't verify OTP easily without checking email or DB.
# BUT, is there a way to bypass? Or maybe the user is created but need verify?
# Let's check AuthServiceImpl.
# Ah, the user said "Verified OTP" in previous summary.
# I might need to verify OTP.
# Let's check if there is a way to hack it for testing or if I should just use the EXISTING user token the user has?

# The user has existing token, but they might have already created a shop ("You already have a shop registered!").
# So I likely need a NEW user.
# If I create a new user, I need to verify OTP.
# Does the system print OTP to console? Let's hope so.

# Alternative: Use "verify-otp" with a hardcoded value if I can find it in logs, or...
# Let's assume for now I can't easily register a new user without email access.

# PLAN B: Use the EXISTING user if they haven't created a shop yet.
# User previously said: "Tạo shop & được admin duyệt". So they HAVE a shop.
# I can't register another shop for the same user.

# PLAN C: I need to bypass OTP or find the OTP code.
# Let's check OtpServiceImpl.java to see if it logs the OTP.

Write-Host "Checking OTP..."
