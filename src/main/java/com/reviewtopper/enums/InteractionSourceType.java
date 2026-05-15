package com.reviewtopper.enums;

public enum InteractionSourceType {
    /** Public landing page load (SPA route; tracked when {@code GET /api/public/{slug}} is called). */
    LANDING_PAGE,
    QR_SCAN,
    REVIEW_REDIRECT,
    WHATSAPP_DEEP_LINK,
    FEEDBACK_FORM,
    OTHER
}
