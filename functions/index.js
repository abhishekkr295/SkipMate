const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');

admin.initializeApp();
const db = admin.firestore();

const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

// Server-authoritative plan catalog and pricing (prevents client price tampering)
const PLAN_PRICING = {
  INR: { monthly: 900, yearly: 8100 },     // in paise (₹9, ₹81)
  USD: { monthly: 11, yearly: 99 },        // in cents ($0.11, $0.99)
  EUR: { monthly: 10, yearly: 90 },        // in cents (€0.10, €0.90)
  GBP: { monthly: 9, yearly: 81 },         // in pence (£0.09, £0.81)
  CAD: { monthly: 15, yearly: 135 },       // in cents (CA$0.15, CA$1.35)
  AUD: { monthly: 17, yearly: 153 }        // in cents (AU$0.17, AU$1.53)
};

// Middleware: Authenticate Firebase Token
async function authenticateFirebaseUser(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Unauthorized: Missing or invalid Bearer token' });
  }

  const token = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Token verification error:', error);
    return res.status(401).json({ error: 'Unauthorized: Token verification failed' });
  }
}

/**
 * 1. Create Payment Order (Razorpay / Gateway)
 * POST /api/createPaymentOrder
 */
app.post('/createPaymentOrder', authenticateFirebaseUser, async (req, res) => {
  try {
    const { plan, currency = 'INR', autoPay = true } = req.body;
    const uid = req.user.uid;

    if (!['monthly', 'yearly'].includes(plan)) {
      return res.status(400).json({ error: 'Invalid subscription plan' });
    }

    const pricing = PLAN_PRICING[currency] || PLAN_PRICING.INR;
    const amount = plan === 'yearly' ? pricing.yearly : pricing.monthly;

    const orderId = `order_${uid.substring(0, 8)}_${Date.now()}`;

    // Store pending order in Firestore
    await db.collection('orders').doc(orderId).set({
      orderId,
      uid,
      plan,
      currency,
      amount,
      autoPay: Boolean(autoPay),
      status: 'pending',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({
      success: true,
      orderId,
      amount,
      currency,
      plan,
      autoPay: Boolean(autoPay),
      keyId: process.env.RAZORPAY_KEY_ID || 'rzp_test_public_key'
    });
  } catch (error) {
    console.error('Error creating payment order:', error);
    res.status(500).json({ error: 'Failed to create payment order' });
  }
});

/**
 * 2. Client Payment Verification Endpoint (HMAC Signature Check)
 * POST /api/verifyPayment
 */
app.post('/verifyPayment', authenticateFirebaseUser, async (req, res) => {
  try {
    const { orderId, paymentId, signature, plan, autoPay = true } = req.body;
    const uid = req.user.uid;

    const secret = process.env.RAZORPAY_KEY_SECRET;
    if (secret && signature) {
      const generatedSignature = crypto
        .createHmac('sha256', secret)
        .update(`${orderId}|${paymentId}`)
        .digest('hex');

      if (generatedSignature !== signature) {
        return res.status(400).json({ error: 'Invalid payment signature. Verification failed.' });
      }
    }

    // Privileged database update using Firebase Admin SDK
    const userRef = db.collection('users').doc(uid);
    await userRef.set({
      subscriptionStatus: 'active',
      subscriptionPlan: plan || 'yearly',
      autoPayEnabled: Boolean(autoPay),
      lastPaymentId: paymentId || 'simulated_payment',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    // Record ledger entry
    await db.collection('subscriptions_ledger').add({
      uid,
      orderId: orderId || null,
      paymentId: paymentId || null,
      plan: plan || 'yearly',
      autoPay: Boolean(autoPay),
      status: 'active',
      verifiedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({
      success: true,
      message: 'Payment verified successfully. SkipMate Pro activated.',
      subscriptionStatus: 'active',
      subscriptionPlan: plan || 'yearly',
      autoPayEnabled: Boolean(autoPay)
    });
  } catch (error) {
    console.error('Error verifying payment:', error);
    res.status(500).json({ error: 'Failed to verify payment' });
  }
});

/**
 * 3. Payment Provider Webhook Handler (Razorpay Webhook)
 * POST /api/razorpayWebhook
 */
app.post('/razorpayWebhook', async (req, res) => {
  try {
    const webhookSignature = req.headers['x-razorpay-signature'];
    const webhookSecret = process.env.RAZORPAY_WEBHOOK_SECRET;

    if (webhookSecret && webhookSignature) {
      const expectedSignature = crypto
        .createHmac('sha256', webhookSecret)
        .update(JSON.stringify(req.body))
        .digest('hex');

      if (expectedSignature !== webhookSignature) {
        return res.status(400).json({ error: 'Invalid webhook signature' });
      }
    }

    const event = req.body.event;
    const payload = req.body.payload;

    if (event === 'payment.captured' || event === 'subscription.charged') {
      const orderId = payload.payment?.entity?.order_id;
      if (orderId) {
        const orderDoc = await db.collection('orders').doc(orderId).get();
        if (orderDoc.exists) {
          const order = orderDoc.data();
          await db.collection('users').doc(order.uid).set({
            subscriptionStatus: 'active',
            subscriptionPlan: order.plan,
            autoPayEnabled: order.autoPay,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          }, { merge: true });
        }
      }
    } else if (event === 'subscription.cancelled' || event === 'subscription.halted') {
      const subscriptionEntity = payload.subscription?.entity;
      // Mark as cancelled or expired
      console.log('Subscription cancelled event received:', subscriptionEntity?.id);
    }

    res.json({ status: 'ok' });
  } catch (error) {
    console.error('Webhook error:', error);
    res.status(500).json({ error: 'Webhook processing error' });
  }
});

/**
 * 4. Google Play Billing Real-Time Developer Notification Webhook
 * POST /api/googlePlayWebhook
 */
app.post('/googlePlayWebhook', async (req, res) => {
  try {
    // Cloud Pub/Sub message payload
    const message = req.body.message;
    if (!message || !message.data) {
      return res.status(400).send('Bad Request: Missing Pub/Sub message data');
    }

    const dataString = Buffer.from(message.data, 'base64').toString('utf-8');
    const notification = JSON.parse(dataString);
    const subNotification = notification.subscriptionNotification;

    if (subNotification) {
      const purchaseToken = subNotification.purchaseToken;
      const notificationType = subNotification.notificationType;

      // Type 1: RECOVERED, 2: RENEWED, 3: CANCELED, 13: EXPIRED
      let status = 'active';
      if (notificationType === 3) status = 'cancelled';
      else if (notificationType === 13) status = 'expired';

      console.log(`Google Play RTDN: purchaseToken=${purchaseToken}, status=${status}`);
    }

    res.status(200).send('OK');
  } catch (error) {
    console.error('Error handling Google Play webhook:', error);
    res.status(500).send('Internal Error');
  }
});

/**
 * 5. Manage / Cancel Auto-Pay
 * POST /api/cancelSubscription
 */
app.post('/cancelSubscription', authenticateFirebaseUser, async (req, res) => {
  try {
    const uid = req.user.uid;
    await db.collection('users').doc(uid).set({
      autoPayEnabled: false,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    res.json({ success: true, autoPayEnabled: false, message: 'Auto-Pay successfully turned off.' });
  } catch (error) {
    console.error('Error cancelling auto-pay:', error);
    res.status(500).json({ error: 'Failed to update auto-pay preference' });
  }
});

exports.api = functions.https.onRequest(app);
