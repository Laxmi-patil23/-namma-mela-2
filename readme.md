
Markdown

# 🎭 NAMMA-MELA - Drama Show Booking App

## Complete Standard Operating Procedure (SOP) for Development

---

## 📋 TABLE OF CONTENTS

1. [Project Overview](#-project-overview)
2. [App Architecture](#-app-architecture)
3. [User Flows](#-user-flows)
4. [Screen Specifications](#-screen-specifications)
5. [Database Schema](#-database-schema)
6. [API Endpoints](#-api-endpoints)
7. [Technical Stack](#-technical-stack)
8. [Design System](#-design-system)
9. [Features Breakdown](#-features-breakdown)
10. [Development Roadmap](#-development-roadmap)
11. [Testing Strategy](#-testing-strategy)
12. [Deployment Guide](#-deployment-guide)

---

## 🎯 PROJECT OVERVIEW

### About Namma-Mela

**Namma-Mela** (Our Drama) is a mobile application designed to digitize traditional Indian drama show bookings. It connects drama enthusiasts with local drama companies, enabling easy ticket booking, seat selection, and community engagement.

### Problem Statement

Traditional drama companies in rural India lack digital infrastructure for:
- Online ticket booking
- Show promotion
- Audience engagement
- Revenue management

### Solution

A bilingual (English/Kannada) Android app providing:
- **For Customers:** Browse shows, book seats visually, rate performances
- **For Managers:** Add/edit shows, manage bookings, track revenue

### Key Highlights

- 🌐 **Bilingual Support:** English & Kannada
- 📱 **Dual Interface:** Customer & Manager modes
- 🎫 **Visual Seat Selection:** Interactive seat map
- 🔐 **Secure Authentication:** Phone OTP & Email login
- 💬 **Social Features:** Reviews, ratings, fan wall
- 📊 **Analytics Dashboard:** For managers
- 🎭 **Cultural Design:** Traditional Indian motifs

---

## 🏗️ APP ARCHITECTURE

### High-Level Architecture
┌─────────────────────────────────────────────────┐
│ PRESENTATION LAYER │
│ (Activities, Fragments, ViewModels, UI) │
└─────────────────┬───────────────────────────────┘
│
┌─────────────────▼───────────────────────────────┐
│ DOMAIN LAYER │
│ (Use Cases, Business Logic) │
└─────────────────┬───────────────────────────────┘
│
┌─────────────────▼───────────────────────────────┐
│ DATA LAYER │
│ ┌──────────────┐ ┌──────────────┐ │
│ │ Local (Room) │ │ Remote (API) │ │
│ └──────────────┘ └──────────────┘ │
└─────────────────────────────────────────────────┘

text


### Design Pattern

**MVVM (Model-View-ViewModel)** with **Repository Pattern**

- **Model:** Data classes, entities, DTOs
- **View:** Activities, Fragments, XML layouts
- **ViewModel:** Business logic, LiveData/StateFlow
- **Repository:** Single source of truth for data

---

## 🔄 USER FLOWS

### Customer User Journey
App Launch
↓
Language Selection (English/Kannada)
↓
Role Selection → Customer
↓
Phone Number Entry
↓
OTP Verification
↓
Home Screen (Tonight's Shows)
↓
├─→ Browse Shows
├─→ View Show Details
│ ↓
│ View Cast
│ ↓
│ Select Seats
│ ↓
│ Confirm Booking
│ ↓
│ QR Code Ticket
│
├─→ My Bookings
├─→ Fan Wall
└─→ Profile

text


### Manager User Journey
App Launch
↓
Language Selection
↓
Role Selection → Manager
↓
Email Login
↓
Manager Dashboard
↓
├─→ Add New Show
├─→ Edit Existing Show
├─→ Manage Cast
├─→ View Bookings & Revenue
└─→ Delete Show

text


---

## 📱 SCREEN SPECIFICATIONS

### Screen 1: Splash + Language + Role Selection

**Purpose:** Initial entry point with language selection and role choice

**UI Components:**
- Language toggle button (top-right): 🌐 English | ಕನ್ನಡ
- App logo (center): Drama mask 🎭
- App name: "NAMMA-MELA"
- Tagline: "Bringing Drama to Life"
- Two action buttons:
  - "I'm a Customer" (👤)
  - "I'm a Manager" (⚙️)

**Functionality:**
- Store language preference in DataStore
- Navigate based on role selection

**API:** None (local storage only)

---

### Screen 2A: Customer Login (Phone + OTP)

**Purpose:** Authenticate customers via phone number

**Flow:**

**Step 1: Phone Entry**
- Input: 10-digit phone number
- Country code: +91 (fixed)
- Button: "Send OTP"

**Step 2: OTP Verification**
- Input: 6-digit OTP (auto-fill via SMS Retriever)
- Timer: "Resend OTP in 30s"
- Button: "Verify & Continue"

**API Endpoints:**

```http
POST /auth/send-otp
{
  "phone": "+919876543210"
}

POST /auth/verify-otp
{
  "phone": "+919876543210",
  "otp": "123456"
}
Response:

JSON

{
  "success": true,
  "userId": "user_123",
  "token": "jwt_token",
  "isNewUser": false
}
Screen 2B: Manager Login (Email)
Purpose: Authenticate managers via email/password

UI Components:

Email input field
Password input field (with show/hide toggle)
"Login" button
"Forgot Password?" link
API Endpoint:

http

POST /auth/manager-login
{
  "email": "manager@example.com",
  "password": "password123"
}
Response:

JSON

{
  "success": true,
  "managerId": "mgr_123",
  "token": "jwt_token"
}
Screen 3: Home (Tonight's Shows)
Purpose: Display all shows scheduled for today

UI Components:

Top Bar:

Hamburger menu
App name
Notification icon
Profile icon
Show Cards (Vertical List):

Poster image (160dp × 240dp)
Show name
Time & duration (⏰ 7:00 PM | 3 hrs)
Venue (📍 Village Square)
Availability (🟢 45 seats available)
Price range (₹20 - ₹100)
Rating (⭐ 4.8)
"BOOK NOW" button
Bottom Navigation:

Home 🏠
Plays 🎭
My Bookings 🎟️
Fan Wall 💬
Profile 👤
API Endpoint:

http

GET /shows/today
Authorization: Bearer {token}
Response:

JSON

{
  "shows": [
    {
      "id": "show_123",
      "name": "Ramayana Drama",
      "nameKannada": "ರಾಮಾಯಣ ನಾಟಕ",
      "posterUrl": "https://...",
      "startTime": "19:00",
      "duration": 180,
      "venue": "Village Square",
      "seatsAvailable": 45,
      "totalSeats": 80,
      "priceRange": { "min": 20, "max": 100 },
      "rating": 4.8,
      "reviewCount": 125
    }
  ]
}
Screen 4: Play Detail
Purpose: Show complete information about a specific show

UI Sections (Scrollable):

Header

Back button
Favorite icon ❤️
Poster

Large banner image
Title & Rating

Show name (English/Kannada)
Star rating (⭐⭐⭐⭐⭐ 4.8)
Quick Info

Date: 📅 Today
Time: ⏰ 7:00 PM - 10:00 PM
Duration: ⌛ 3 hours
Venue: 📍 Village Square
Pricing

Front Row: ₹100
Middle Row: ₹50
Back Row: ₹20
Description

Full text (expandable)
Cast Preview

"View Full Cast →" button
Top 3 cast photos
Reviews Preview

Average rating breakdown
"See all reviews →" button
Top 2 reviews
Bottom CTA

"🎟️ BOOK SEATS" button
API Endpoint:

http

GET /shows/{showId}
Authorization: Bearer {token}
Screen 5: Cast Display
Purpose: Show all cast members categorized by role

UI Structure:

Categories:

🌟 LEAD ARTISTS (Grid: 2 columns)

Circular photo (80dp)
Name
Role badge (HERO/HEROINE)
Character name
🎤 SINGERS

😂 COMEDIANS

🎭 SUPPORTING CAST

API Endpoint:

http

GET /shows/{showId}/cast
Response:

JSON

{
  "cast": [
    {
      "id": "cast_1",
      "name": "Ravi Kumar",
      "nameKannada": "ರವಿ ಕುಮಾರ್",
      "role": "HERO",
      "character": "Rama",
      "photoUrl": "https://...",
      "category": "LEAD"
    }
  ]
}
Screen 6: Seat Selection ⭐ CRITICAL
Purpose: Interactive seat map for booking

Layout:

text

┌─────────────────────────┐
│    🎭 STAGE 🎭         │
└─────────────────────────┘

FRONT ROW (₹100)
  1  2  3  4  5  6  7  8
A 🟢 🟢 🔴 🔴 🟢 🟢 🟡 🟢
B 🟢 🔴 🔴 🟡 🟢 🟢 🟢 🟢

MIDDLE ROW (₹50)
C 🟢 🟢 🟢 🔴 🔴 🟢 🟢 🟢
D 🟢 🟢 🟢 🟢 🟢 🟢 🟢 🟢
E 🟢 🟢 🔴 🟢 🟢 🟢 🟢 🟢

BACK ROW (₹20)
F 🟢 🟢 🟢 🟢 🟢 🟢 🟢 🟢
G 🟢 🟢 🟢 🟢 🟢 🟢 🟢 🟢
H 🟢 🟢 🟢 🟢 🟢 🟢 🟢 🟢

Legend:
🟢 Available  🔴 Booked  🟡 Selected

Selected: A7, B4
Total: ₹200

[ CONFIRM BOOKING ]
Interaction:

Tap available seat → Select (turns yellow)
Tap selected seat → Deselect
Tap booked seat → Show toast "Already booked"
Max 10 seats per booking
Calculate price dynamically
API Endpoints:

http

GET /shows/{showId}/seats

POST /bookings/create
{
  "showId": "show_123",
  "seats": ["seat_A7", "seat_B4"]
}
Response:

JSON

{
  "success": true,
  "bookingId": "booking_123",
  "bookingCode": "NM12345",
  "qrCode": "data:image/png;base64,..."
}
Screen 7: Booking Confirmation
Purpose: Show successful booking with digital ticket

UI Components:

Success Animation

Green checkmark ✓
"Booking Successful!" message
Ticket Card

QR code (for entry)
Show name
Date & time
Seat numbers
Venue
Total amount
Booking ID (e.g., NM12345)
Action Buttons

📥 Download Ticket
📤 Share Ticket
⭐ Rate This Show
Navigation

"BACK TO HOME" button
Functionality:

Generate QR code using ZXing
Save ticket as PNG
Share via WhatsApp/SMS
Store in booking history
Screen 8: Fan Wall
Purpose: Social feed for show discussions

UI Components:

Post Input

Text area: "Share your experience..."
POST button
Feed (Infinite Scroll)

User avatar & name
Timestamp (e.g., "2 hrs ago")
Comment text
❤️ Like button (count)
💬 Reply button
Filter Tabs

All Shows | Show-specific
API Endpoints:

http

GET /comments?showId={showId}&page=1&limit=20

POST /comments/create
{
  "showId": "show_123",
  "message": "Loved the show!"
}

POST /comments/{commentId}/like
Screen 9: Manager Dashboard
Purpose: Control panel for drama company managers

UI Components:

Stats Cards

🎟️ Today's Bookings: 47/80
💰 Revenue: ₹3,250
🎭 Active Shows: 5
Quick Actions

➕ Add New Play
✏️ Edit Existing Play
👥 Manage Cast
📊 View Bookings
🔄 Reset Seats
API Endpoint:

http

GET /manager/dashboard
Authorization: Bearer {manager_token}
Response:

JSON

{
  "todayBookings": 47,
  "totalSeats": 80,
  "revenue": 3250,
  "activeShows": 5,
  "upcomingShows": [...]
}
Screen 10: Add/Edit Play (Manager)
Purpose: Form to create or modify shows

Form Fields:

Basic Info

Play Name * (required)
Description (multiline)
Duration (number picker: 1-5 hrs)
Show Time (time picker)
Show Date (date picker)
Venue (text/dropdown)
Poster Upload

Image picker
Preview
Upload to Firebase Storage
Pricing

Front Row: ₹ [input]
Middle Row: ₹ [input]
Back Row: ₹ [input]
Cast

Add Cast Member button
List with remove option
Actions

💾 SAVE PLAY
Cancel
API Endpoints:

http

POST /shows/create
PUT /shows/{showId}/update

POST /upload/image (multipart/form-data)
🗄️ DATABASE SCHEMA
Collections/Tables
1. users
JSON

{
  "_id": "user_123",
  "phone": "+919876543210",
  "name": "Rajesh Kumar",
  "email": "rajesh@example.com",
  "role": "CUSTOMER",
  "favoriteShows": ["show_123"],
  "languagePreference": "kannada",
  "createdAt": "2024-01-15T10:30:00Z"
}
2. managers
JSON

{
  "_id": "mgr_123",
  "email": "manager@dramacompany.com",
  "passwordHash": "$2b$10$...",
  "companyName": "Sri Krishna Drama Company",
  "phone": "+919876543210",
  "createdAt": "2024-01-01T00:00:00Z"
}
3. shows
JSON

{
  "_id": "show_123",
  "name": "Ramayana Drama",
  "nameKannada": "ರಾಮಾಯಣ ನಾಟಕ",
  "description": "Epic tale...",
  "descriptionKannada": "...",
  "posterUrl": "https://...",
  "managerId": "mgr_123",
  "showDate": "2024-01-20",
  "startTime": "19:00",
  "duration": 180,
  "venue": "Village Square",
  "pricing": {
    "front": 100,
    "middle": 50,
    "back": 20
  },
  "totalSeats": 80,
  "seatsBooked": 35,
  "rating": 4.8,
  "reviewCount": 125,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:00:00Z"
}
4. cast_members
JSON

{
  "_id": "cast_1",
  "showId": "show_123",
  "name": "Ravi Kumar",
  "nameKannada": "ರವಿ ಕುಮಾರ್",
  "role": "HERO",
  "character": "Rama",
  "photoUrl": "https://...",
  "category": "LEAD",
  "order": 1
}
5. seats
JSON

{
  "_id": "seat_A1",
  "showId": "show_123",
  "row": "A",
  "column": 1,
  "category": "FRONT",
  "price": 100,
  "isBooked": false,
  "bookedBy": null
}
6. bookings
JSON

{
  "_id": "booking_123",
  "bookingId": "NM12345",
  "userId": "user_123",
  "showId": "show_123",
  "showName": "Ramayana Drama",
  "showDate": "2024-01-20",
  "showTime": "19:00",
  "seats": ["seat_A7", "seat_B4"],
  "seatNumbers": ["A7", "B4"],
  "totalPrice": 200,
  "paymentStatus": "COMPLETED",
  "qrCode": "data:image/png;base64,...",
  "status": "CONFIRMED",
  "bookingDate": "2024-01-20T14:30:00Z"
}
7. reviews
JSON

{
  "_id": "review_1",
  "showId": "show_123",
  "userId": "user_123",
  "userName": "Rajesh K.",
  "rating": 5,
  "comment": "Amazing performance!",
  "likes": 24,
  "createdAt": "2024-01-21T22:00:00Z"
}
8. comments
JSON

{
  "_id": "comment_1",
  "showId": "show_123",
  "userId": "user_123",
  "userName": "Priya S.",
  "userAvatar": "https://...",
  "message": "The singers were superb!",
  "likes": 18,
  "likedBy": ["user_456"],
  "createdAt": "2024-01-20T20:15:00Z"
}
🌐 API ENDPOINTS
Base URL
text

https://api.nammamela.com/v1
Authentication
All authenticated endpoints require:

text

Authorization: Bearer {jwt_token}
Auth Endpoints
Send OTP
http

POST /auth/send-otp
Content-Type: application/json

{
  "phone": "+919876543210"
}

Response:
{
  "success": true,
  "message": "OTP sent successfully"
}
Verify OTP
http

POST /auth/verify-otp

{
  "phone": "+919876543210",
  "otp": "123456"
}

Response:
{
  "success": true,
  "userId": "user_123",
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "isNewUser": false
}
Manager Login
http

POST /auth/manager-login

{
  "email": "manager@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "managerId": "mgr_123",
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
Show Endpoints
Get Today's Shows
http

GET /shows/today
Authorization: Bearer {token}

Response:
{
  "shows": [
    {
      "id": "show_123",
      "name": "Ramayana Drama",
      "posterUrl": "https://...",
      "startTime": "19:00",
      "seatsAvailable": 45,
      "rating": 4.8
    }
  ]
}
Get Show Details
http

GET /shows/{showId}
Authorization: Bearer {token}
Create Show (Manager)
http

POST /shows/create
Authorization: Bearer {manager_token}

{
  "name": "Ramayana Drama",
  "description": "...",
  "posterUrl": "https://...",
  "showDate": "2024-01-20",
  "startTime": "19:00",
  "duration": 180,
  "venue": "Village Square",
  "pricing": {
    "front": 100,
    "middle": 50,
    "back": 20
  }
}

Response:
{
  "success": true,
  "showId": "show_123"
}
Update Show
http

PUT /shows/{showId}/update
Authorization: Bearer {manager_token}
Delete Show
http

DELETE /shows/{showId}
Authorization: Bearer {manager_token}
Booking Endpoints
Get Seats
http

GET /shows/{showId}/seats
Authorization: Bearer {token}

Response:
{
  "seats": [
    {
      "id": "seat_A1",
      "row": "A",
      "column": 1,
      "category": "FRONT",
      "price": 100,
      "isBooked": false
    }
  ]
}
Create Booking
http

POST /bookings/create
Authorization: Bearer {token}

{
  "showId": "show_123",
  "seats": ["seat_A7", "seat_B4"]
}

Response:
{
  "success": true,
  "bookingId": "booking_123",
  "bookingCode": "NM12345",
  "qrCode": "data:image/png;base64,..."
}
Get User Bookings
http

GET /bookings/user
Authorization: Bearer {token}

Response:
{
  "bookings": [
    {
      "id": "booking_123",
      "showName": "Ramayana Drama",
      "showDate": "2024-01-20",
      "seats": ["A7", "B4"],
      "totalPrice": 200,
      "qrCode": "..."
    }
  ]
}
Review & Comment Endpoints
Get Reviews
http

GET /reviews?showId={showId}
Authorization: Bearer {token}
Post Review
http

POST /reviews/create
Authorization: Bearer {token}

{
  "showId": "show_123",
  "rating": 5,
  "comment": "Amazing performance!"
}
Get Comments
http

GET /comments?showId={showId}&page=1
Authorization: Bearer {token}
Post Comment
http

POST /comments/create
Authorization: Bearer {token}

{
  "showId": "show_123",
  "message": "Loved the show!"
}
Like Comment
http

POST /comments/{commentId}/like
Authorization: Bearer {token}
Manager Endpoints
Dashboard Stats
http

GET /manager/dashboard
Authorization: Bearer {manager_token}

Response:
{
  "todayBookings": 47,
  "totalSeats": 80,
  "revenue": 3250,
  "activeShows": 5
}
Upload Endpoint
Upload Image
http

POST /upload/image
Authorization: Bearer {token}
Content-Type: multipart/form-data

FormData:
  - image: File

Response:
{
  "success": true,
  "imageUrl": "https://storage.../image.jpg"
}
💻 TECHNICAL STACK
Android App
Language: Kotlin
Min SDK: 24 (Android 7.0)
Target SDK: 34 (Android 14)
Architecture: MVVM + Repository Pattern

Core Dependencies
gradle

dependencies {
    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.20"
    
    // AndroidX
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "com.google.android.material:material:1.11.0"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"
    
    // Navigation
    implementation "androidx.navigation:navigation-fragment-ktx:2.7.6"
    implementation "androidx.navigation:navigation-ui-ktx:2.7.6"
    
    // Lifecycle & ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Retrofit (Networking)
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
    implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
    
    // Glide (Image Loading)
    implementation "com.github.bumptech.glide:glide:4.16.0"
    kapt "com.github.bumptech.glide:compiler:4.16.0"
    
    // Room (Database)
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // Firebase
    implementation platform("com.google.firebase:firebase-bom:32.7.0")
    implementation "com.google.firebase:firebase-auth-ktx"
    implementation "com.google.firebase:firebase-messaging-ktx"
    implementation "com.google.firebase:firebase-storage-ktx"
    
    // ZXing (QR Code)
    implementation "com.google.zxing:core:3.5.2"
    implementation "com.journeyapps:zxing-android-embedded:4.3.0"
    
    // Hilt (Dependency Injection)
    implementation "com.google.dagger:hilt-android:2.50"
    kapt "com.google.dagger:hilt-compiler:2.50"
    
    // DataStore
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}
Backend Options
Option 1: Node.js + Express (Recommended)
text

- Runtime: Node.js 18+
- Framework: Express.js
- Database: MongoDB + Mongoose
- Auth: jsonwebtoken (JWT)
- File Upload: Multer
- Storage: Firebase Storage
- SMS: Firebase Auth / Twilio
Option 2: Firebase (Serverless)
text

- Firestore (Database)
- Cloud Functions (API)
- Firebase Auth
- Cloud Storage
- Cloud Messaging
🎨 DESIGN SYSTEM
Color Palette
CSS

/* Primary Colors */
--maroon: #8B0000;      /* Main brand color */
--gold: #FFD700;        /* Accents */
--cream: #FFF8DC;       /* Backgrounds */

/* Functional Colors */
--green: #4CAF50;       /* Available, Success */
--red: #F44336;         /* Booked, Error */
--yellow: #FFC107;      /* Selected, Warning */
--blue: #2196F3;        /* Info, Links */

/* Text Colors */
--text-primary: #212121;
--text-secondary: #757575;
--text-on-dark: #FFFFFF;
Typography
text

Font Family: Roboto (English), Noto Sans Kannada (Kannada)

Heading 1:    24sp, Bold
Heading 2:    20sp, Medium
Heading 3:    18sp, Medium
Body:         14sp, Regular
Caption:      12sp, Light
Button:       16sp, Medium, UPPERCASE
Spacing
text

Micro:    4dp
Small:    8dp
Medium:   16dp
Large:    24dp
XLarge:   32dp
Component Sizes
text

Button Height:        48dp
Icon Size:            24dp × 24dp
Poster Card:          160dp × 240dp
Cast Avatar:          80dp (circular)
Seat Button:          32dp × 32dp
Bottom Nav:           56dp
App Bar:              56dp
Input Field:          56dp
Design Principles
Cultural Authenticity

Traditional Indian motifs
Drama-themed imagery (🎭)
Warm theatrical colors
Rural-Friendly

Large touch targets (48dp min)
Clear visual indicators
Minimal text input
Offline capability
Accessibility

High contrast (4.5:1 for text)
Icon + text labels
Support for large fonts
Bilingual

English & Kannada throughout
Easy language switching
✨ FEATURES BREAKDOWN
Customer Features
✅ Must-Have (MVP)
Phone OTP authentication
View tonight's shows
View show details
Visual seat selection
Book tickets
View booking confirmation with QR
Language toggle (English/Kannada)
View cast information
Rate and review shows
Fan wall comments
🎯 Should-Have (Phase 2)
View booking history
Download ticket as image
Share ticket
Push notifications
Favorite shows
Filter/search shows
Edit profile
💡 Could-Have (Future)
UPI payment integration
Seat recommendations
Video trailers
Live updates
Chat with cast
Loyalty points
Group booking
Voice search
Manager Features
✅ Must-Have (MVP)
Email login
Add new shows
Edit show details
Delete shows
Upload posters
Add/manage cast
View booking statistics
View revenue
🎯 Should-Have (Phase 2)
Analytics dashboard
Export reports
Manage multiple venues
Bulk seat reset
Send notifications
💡 Could-Have (Future)
Advanced analytics
Revenue forecasting
Marketing campaigns
Multi-manager support
Role-based permissions
🗓️ DEVELOPMENT ROADMAP
Phase 1: Foundation (Week 1-2)
 Project setup (Android Studio, Git)
 Add dependencies
 Set up navigation
 Create data models
 Set up Room database
 Configure Retrofit
 Set up Firebase
Deliverable: App skeleton

Phase 2: Authentication (Week 2-3)
 Language selection screen
 Role selection screen
 Firebase Phone Auth integration
 OTP verification screen
 Manager email login
 JWT token management
 Session persistence
Deliverable: Working auth flow

Phase 3: Customer - Browsing (Week 3-4)
 Home screen with show list
 Show detail screen
 Cast display screen
 API integration for shows
 Image loading with Glide
 Pull-to-refresh
Deliverable: Browse shows feature

Phase 4: Customer - Booking (Week 4-5)
 Seat selection UI
 Seat availability API
 Booking creation API
 QR code generation
 Booking confirmation screen
 Download/share ticket
Deliverable: Complete booking flow

Phase 5: Manager Features (Week 5-6)
 Manager dashboard
 Add show screen
 Edit show screen
 Cast management
 Image upload to Firebase
 Delete show
 Booking stats
Deliverable: Manager panel

Phase 6: Social Features (Week 6-7)
 Review/rating system
 Fan wall
 Comment posting
 Like functionality
 User profile
 Booking history
Deliverable: Social features

Phase 7: Polish (Week 7-8)
 Bilingual support
 Loading states
 Error handling
 Offline mode
 Push notifications
 Performance optimization
 UI/UX refinements
 Animations
Deliverable: Production-ready app

Phase 8: Testing & Deployment (Week 8)
 Unit tests
 Integration tests
 UI tests
 Manual testing
 Beta testing
 Bug fixes
 Generate signed APK
 Deployment
Deliverable: Released app

🧪 TESTING STRATEGY
Unit Tests
Test Coverage:

ViewModels (business logic)
Repositories (data operations)
Utility functions
Validators
Example:

Kotlin

@Test
fun `calculate total price correctly`() {
    val seats = listOf(
        Seat("A", 1, "FRONT", 100),
        Seat("B", 4, "FRONT", 100)
    )
    val total = bookingViewModel.calculateTotal(seats)
    assertEquals(200, total)
}
Integration Tests
Test Coverage:

API calls
Database operations
Authentication flow
UI Tests (Espresso)
Critical Flows:

Login flow
Booking flow (end-to-end)
Add show flow (manager)
Example:

Kotlin

@Test
fun testBookingFlow() {
    onView(withId(R.id.show_card)).perform(click())
    onView(withId(R.id.btn_book_seats)).perform(click())
    onView(withId(R.id.seat_A7)).perform(click())
    onView(withId(R.id.btn_confirm)).perform(click())
    onView(withId(R.id.tv_success)).check(matches(isDisplayed()))
}
Manual Testing Checklist
 Test on Android 7.0 to 14
 Test on different screen sizes
 Test with slow internet
 Test offline mode
 Test both languages
 Test edge cases (no shows, sold out, etc.)
 Security testing
 Performance testing
🚀 DEPLOYMENT GUIDE
Prerequisites
Firebase Project

Create project at console.firebase.google.com
Enable Authentication, Storage, Messaging
Download google-services.json
Backend Deployment

Deploy to Heroku/Railway/DigitalOcean
Set environment variables
Configure CORS
Keystore for Signing

Bash

keytool -genkey -v -keystore namma-mela.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias namma-mela
Build Steps
Update build.gradle

gradle

android {
    signingConfigs {
        release {
            storeFile file("namma-mela.jks")
            storePassword "your_password"
            keyAlias "namma-mela"
            keyPassword "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
Build Release APK

Bash

./gradlew assembleRelease
Locate APK

text

app/build/outputs/apk/release/app-release.apk
Play Store Submission (Optional)
Create developer account ($25 one-time)
Create app listing
Add screenshots (phone, tablet)
Write description
Upload APK/AAB
Submit for review
📦 DELIVERABLES
Client Handover Package
✅ APK Files

Release APK (signed)
Debug APK
✅ Source Code

Complete Android project
Backend source code
Git repository access
✅ Documentation

This README.md
API documentation (Postman collection)
Database schema
Setup instructions
✅ User Guides

Customer app guide (PDF)
Manager app guide (PDF)
Screenshots & walkthroughs
✅ Demo Materials

Demo video (3-5 minutes)
Presentation slides
Sample data (JSON)
✅ Credentials

Firebase project access
Backend admin credentials
Database access
Keystore file + passwords
🔐 SECURITY BEST PRACTICES
Authentication
JWT tokens expire in 7 days
Use EncryptedSharedPreferences for token storage
Validate tokens on every request
Network
HTTPS only
Certificate pinning (optional)
Disable cleartext traffic
Data
Encrypt sensitive data
Input validation
SQL injection prevention (Room handles this)
XSS protection
Code
ProGuard/R8 obfuscation
No hardcoded secrets
Use BuildConfig for API keys
📞 SUPPORT & CONTACTS
Project Name: Namma-Mela
Client: [Your Name]
Development Team: Anti Gravity
Timeline: 8 weeks
Status: In Development

📚 RESOURCES
Learning
Android Developers
Kotlin Documentation
Firebase Documentation
Design
Material Design
Figma Community
Tools
Postman - API testing
DB Browser for SQLite - Database inspection
Scrcpy - Screen mirroring
🎭 PROJECT VISION
Namma-Mela aims to bridge the gap between traditional Indian drama and modern technology, preserving cultural heritage while making it accessible to new generations.

Impact Goals
📱 Digitize 100+ drama companies
🎟️ Facilitate 10,000+ ticket bookings
🌟 Support local artists & traditions
🇮🇳 Promote cultural events across Karnataka
📝 CHANGELOG
Version 1.0.0 (Initial Release)
Phone OTP authentication
Show browsing & details
Visual seat selection
Ticket booking with QR code
Manager panel for CRUD operations
Bilingual support (English/Kannada)
Review & rating system
Fan wall comments
📄 LICENSE
Proprietary - All rights reserved
© 2024 Namma-Mela

✅ FINAL CHECKLIST
Pre-Development
 Review complete SOP
 Set up Firebase project
 Choose backend stack
 Set up Git repository
 Install Android Studio
During Development
 Follow MVVM architecture
 Write tests alongside code
 Commit regularly with clear messages
 Document API changes
 Handle errors gracefully
Pre-Release
 All features implemented
 All tests passing
 Manual testing complete
 Beta testing done
 Performance optimized
 Generate signed APK
 Prepare documentation
 Create demo video
🎉 CONCLUSION
This SOP provides a complete blueprint for building the Namma-Mela app. Follow it systematically, and you'll deliver a production-ready application that digitizes traditional drama shows while honoring cultural heritage.

Key Reminders:

Prioritize simplicity for rural users
Test on real devices frequently
Maintain bilingual consistency
Focus on performance and reliability
Keep cultural authenticity at the core
Ready to build something amazing? Let's go! 🚀🎭

Document Version: 1.0
Last Updated: January 2024
Format: README.md

END OF DOCUMENT

text


This is your complete, production-ready README.md file! 🎉

You can now:
1. **Copy this entire content** and save it as `README.md`
2. **Add it to your Git repository**
3. **Share it directly with Anti Gravity team**
4. **Convert to PDF** using any markdown-to-PDF converter

The document is comprehensive and includes everything needed to build the Namma-Mela app from scratch! 📱✨