# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

All commands run from `src/Backend/`:

```bash
# Start development server (kills existing process on port 8080 first)
.\run-dev.bat
# or directly:
.\mvnw.cmd spring-boot:run

# Run all tests
.\mvnw.cmd test

# Build jar
.\mvnw.cmd clean package
```

Single test class:
```bash
.\mvnw.cmd test -Dtest=CartServiceImplTest
```

## Environment Variables

Create `src/Backend/.env` (see `.env.example`). Required variables:

| Variable | Purpose |
|---|---|
| `JWT_SECRET` | 32+ char HMAC-SHA256 signing key |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP (App Password, not account password) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Image uploads |

MongoDB URI is hardcoded in `application.properties` pointing to Atlas; override with `SPRING_DATA_MONGODB_URI`.

## Architecture

**Stack**: Spring Boot 3.2.3, Java 21, MongoDB Atlas, stateless JWT auth.

**Package layout** under `com.shoppeclone.backend`:

```
auth/          JWT + Google OAuth2, OTP email verification
user/          User profile, addresses, CSV bulk import
shop/          Shop registration, seller analytics, approval flow
product/       Catalog, variants, images, categories (hierarchical)
cart/          Cart with promotion price calculations
order/         Order lifecycle, return handling, delivery failure
payment/       MoMo + VNPAY webhooks
shipping/      Provider webhooks, delivery tracking
promotion/
  voucher/     Platform and shop-scoped vouchers
  flashsale/   Flash sale campaigns (see note below)
review/        Product reviews + seller replies
refund/        Refund requests with image evidence
dispute/       Dispute resolution
chat/          Buyer-seller messaging
admin/         Dashboard stats, user/shop management, maintenance
search/        Search suggestion service (recently added)
common/        SecurityConfig, CorsConfig, CloudinaryConfig,
               GlobalExceptionHandler, DataInitializer, EmailService
```

**Layered pattern**: `Controller → Service interface → ServiceImpl → MongoRepository`.
All constructors use Lombok `@RequiredArgsConstructor`. DTOs live in `dto/request/` and `dto/response/` per module.

## Key Architectural Notes

**Security** (`common/config/SecurityConfig.java`): Stateless JWT. `JwtAuthFilter` runs before `UsernamePasswordAuthenticationFilter`. Public routes include `/api/auth/**`, `/api/webhooks/**`, and all `GET /api/products/**`. Method-level security via `@EnableMethodSecurity`. Three roles: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`.

**Startup seeding** (`common/config/DataInitializer.java`, `Order=1`): On every startup, roles, categories, payment methods, and shipping providers are seeded. Vouchers are **cleared and recreated** on each startup — avoid this in production.

**Flash sale module** (`promotion/flashsale/`): The most complex module. Handles concurrent inventory with safeguards, a scheduler for campaign transitions, and runtime metrics tracking. Tomcat is configured with 500 max threads / 1000 accept-count specifically for flash sale load. A standalone load-testing simulator lives in `tools/FlashSaleSimulator/`.

**Product aggregate fields**: `minPrice`, `maxPrice`, `totalStock`, `sold`, `rating` on the `Product` document are **denormalized** — they are recalculated and saved whenever variants change. Do not treat them as source of truth for individual variant data.

**Flash sale sold count** (`mapToResponse` in `ProductServiceImpl`): The real-time sold count is fetched live from `FlashSaleItem` records for any active flash sale, not from the `flashSaleSold` field on the product/variant document.

**Search suggestions** (`search/`): `GET /api/search/suggestions?keyword=&limit=10` returns PRODUCT, CATEGORY, KEYWORD, and ATTRIBUTE (color/size) suggestion items. All user input is regex-escaped before hitting MongoDB.

## MongoDB

Database name: `web_shoppe`. Collections follow snake_case (e.g., `flash_sales`, `product_categories`). Compound indexes exist on `ProductCategory` (productId + categoryId, unique). Timezone is set to `Asia/Ho_Chi_Minh` at startup via `TimezoneConfig`.
