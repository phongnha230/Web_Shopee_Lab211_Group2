# MoMo Payment Integration Plan

## Goal
Enable users to pay for their orders using MoMo. When they click "Transfer via MoMo", they will be redirected to MoMo payment page, and upon completion, redirected back to the order page with the order loaded and status updated.

## User Review Required
> [!IMPORTANT]
> **MoMo Credentials**: You will need to provide your MoMo Partner Code, Access Key, Secret Key, and API Endpoint in the `.env` file.
> I will add placeholders in the code, but you must fill them in for the payment to work.

## Proposed Changes

### Backend

#### [MODIFY] [paymentController.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/controllers/paymentController.js)
- Add `createMomoPayment` function:
    - Receive `orderId` and `amount`.
    - Generate MoMo signature.
    - Call MoMo API to get `payUrl`.
    - Return `payUrl` to frontend.
- Add `momoCallback` function (IPN):
    - Verify signature from MoMo.
    - Update `Payment` status and `Order` status.

#### [MODIFY] [paymentRoutes.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/routes/paymentRoutes.js)
- Add `POST /momo` route linked to `createMomoPayment`.
- Add `POST /momo-callback` route linked to `momoCallback`.

### Frontend

#### [MODIFY] [apiEndpoints.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/constants/apiEndpoints.js)
- Add `PAYMENTS.MOMO_CREATE` endpoint.

#### [MODIFY] [paymentServices.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/services/paymentServices.js)
- Add `createMomoPayment(data)` method.

#### [MODIFY] [CustomerOrdersPage.jsx](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/pages/Order/CustomerOrdersPage.jsx)
- Add "Thanh toán MoMo" button for orders with status `pending` (or not `done`/`paid`).
- Handle `paymentResult` query parameters in `useEffect` to show success/failure message and refresh order list.

## Verification Plan

### Automated Tests
- None currently available for external payments.

### Manual Verification
1.  **Setup**: Add dummy MoMo credentials to `.env` (or real ones if available).
2.  **Initiate Payment**:
    - Go to "Đơn của tôi".
    - Click "Thanh toán MoMo" on a pending order.
    - Verify redirection to MoMo (or mock URL if testing).
3.  **Payment Success**:
    - Complete payment on MoMo (or simulate success callback).
    - Verify redirection back to "Đơn của tôi".
    - Verify success message "Thanh toán thành công".
    - Verify order status updates to "Paid" (or equivalent).
