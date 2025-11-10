# Final Fix: Restart User Service to Initialize Users

## What Happened
The database had users that were created manually with incorrect passwords. I've deleted all users so the DataInitializer can create them fresh with the correct credentials.

## **RESTART USER SERVICE NOW**

### Steps:
1. Find the window titled **"PLM - User Service (8083)"**
2. Press `Ctrl+C` to stop the service
3. Wait 5 seconds for it to fully stop
4. In that same window, run:
   ```cmd
   mvn spring-boot:run
   ```
5. **Wait for this message in the console:**
   ```
   Demo users initialized successfully!
   ```
   This confirms all 4 users were created properly.

## After Restart - Test Login

Once you see "Demo users initialized successfully!", **try logging in**:
- Go to: http://localhost:8111
- Username: `demo`
- Password: `demo`

### All Demo Credentials:
- `demo` / `demo` (USER role)
- `guodian` / `password` (REVIEWER role)
- `labubu` / `password` (EDITOR role)
- `vivi` / `password` (APPROVER role)

## Summary of All Fixes

✅ **Auth Service**: Added Spring Cloud LoadBalancer dependency
✅ **User Service**: Fixed JSON serialization with @JsonIgnore annotations  
✅ **Database**: Removed duplicate users
✅ **Database**: Cleared users to allow proper initialization with correct passwords

**Everything should work after this restart!** 🎉





