# MoMo Payment Integration Walkthrough

I have successfully integrated MoMo payment into your Cafe App. Here is a summary of the changes and how to test it.

## Changes Implemented

### Backend
- **`controllers/paymentController.js`**: Added `createMomoPayment` to generate payment URL and `momoCallback` to handle IPN from MoMo.
- **`routes/paymentRoutes.js`**: Added routes `/momo` and `/momo-callback`.

### Frontend
- **`constants/apiEndpoints.js`**: Added `MOMO_CREATE` endpoint.
- **`services/paymentServices.js`**: Added `createMomoPayment` service.
- **`pages/Order/CustomerOrdersPage.jsx`**:
    - Added "Thanh toán MoMo" button for pending orders.
    - Added logic to handle redirect back from MoMo (display success/error message and refresh order list).

## Configuration Required

> [!IMPORTANT]
> You **MUST** add the following variables to your backend `.env` file for the payment to work:

```env
MOMO_PARTNER_CODE=MOMO
MOMO_ACCESS_KEY=F8BBA842ECF85
MOMO_SECRET_KEY=K951B6PE1waDMi640xX08PD3vg6EkVlz
MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
MOMO_IPN_URL=https://your-public-domain.com/api/payments/momo-callback
MOMO_REDIRECT_URL=http://localhost:3000/orders
```

*Note: For local testing of IPN (Callback), you need a public URL (e.g., using ngrok) for `MOMO_IPN_URL`. If you just want to test the redirect flow, `MOMO_REDIRECT_URL` is enough, but the order status won't update automatically on the backend unless the IPN is received or you implement a query status check.*

## How to Test

1.  Start your backend and frontend.
2.  Login as a customer.
3.  Place an order (or use an existing pending order).
4.  Go to **Đơn của tôi**.
5.  Click the **Thanh toán MoMo** button.
6.  You will be redirected to MoMo's test payment page.
7.  Scan the QR code with the MoMo app (in Test mode) or use the provided test credentials on the page.
8.  After payment, you will be redirected back to the Order page.
9.  A success message should appear.
