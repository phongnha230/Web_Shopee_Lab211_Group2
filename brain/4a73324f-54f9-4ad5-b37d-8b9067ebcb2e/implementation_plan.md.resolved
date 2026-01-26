# Product Detail Page Implementation Plan

## Goal
Create a new product detail page (`product-detail.html`) following Shopee's design with orange theme. The page will display product information, image gallery, specifications, and customer reviews.

## User Review Required

> [!IMPORTANT]
> This creates a **new standalone page** for product details. Products on the landing page will link to this page with URL parameters (e.g., `product-detail.html?id=1`).

## Proposed Changes

### Frontend File Structure

#### [NEW] [product-detail.html](file:///C:/Users/HPPAVILION/Downloads/Backend/src/Frontend/product-detail.html)

New HTML page with the following sections:

**1. Header** (reused from index.html)
- Orange gradient header with search and navigation
- Breadcrumb navigation (Home > Category > Product Name)

**2. Product Gallery Section**
- Main product image (large display)
- Thumbnail gallery (4-5 smaller images below)
- Image selection on click

**3. Product Info Panel**
- Product title  
- Star rating with review count
- Price display (original + discounted in orange)
- Variant selection (Color, Size buttons)
- Quantity selector (+/- buttons)
- "Add to Cart" button (orange)
- "Buy Now" button (orange filled)

**4. Product Specifications**
- Table format showing:
  - Category, Stock, Brand, Material, etc.
  - Clean grid layout

**5. Product Description**
- Expandable text section
- Bullet points for features

**6. Product Ratings & Reviews**
- Overall rating statistics (4.8 out of 5)
- Filter buttons (All, 5 Star, 4 Star, etc.)
- Individual review cards with:
  - User avatar & name
  - Star rating
  - Review text
  - Review images (if any)
  - Date posted
- Pagination

**7. Footer** (reused from index.html)

---

#### [MODIFY] [index.html](file:///C:/Users/HPPAVILION/Downloads/Backend/src/Frontend/index.html)

Update product cards to link to detail page:
- Wrap product card in `<a>` tag or add `onclick` handler
- Pass product ID via URL parameter
- Example: `product-detail.html?id=1&name=camera`

---

## Verification Plan

### Manual Testing

**Test 1: Page Navigation**
1. Open `http://localhost:3000/index.html` in browser
2. Click on any product card in Flash Sale or Daily Discover section
3. ✅ Should navigate to `product-detail.html` with product ID in URL
4. ✅ Breadcrumb should show correct navigation path

**Test 2: Product Gallery**
1. On product detail page, view the main product image
2. Click on thumbnail images below
3. ✅ Main image should update to show clicked thumbnail
4. ✅ Smooth transition between images

**Test 3: Add to Cart**
1. Select a color variant
2. Select a size variant  
3. Change quantity using +/- buttons
4. Click "Add to Cart"
5. ✅ Should show success message
6. ✅ Cart counter should update

**Test 4: Responsive Design**
1. Resize browser to mobile width (< 768px)
2. ✅ Layout should stack vertically
3. ✅ All buttons and text should be readable
4. ✅ Gallery should work on mobile

**Test 5: Orange Theme Consistency**
1. Check all buttons and accents
2. ✅ Should match Shopee orange theme (#F97316)
3. ✅ Hover states should be darker orange
