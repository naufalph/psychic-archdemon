package com.rumantra.client.domain;

public enum ProjectStatus {
  DRAFT, // Being filled out by the client, not yet submitted
  PENDING_APPROVAL, // Just created, awaiting superuser validation
  REJECTED, // Superuser rejected/invalidated the project
  OPEN, // Validated by superuser, accepting bids
  BIDDING_CLOSED, // No more bids accepted
  NEGOTIATION, // Bid accepted, awaiting client confirmation before work starts
  NEGOTIATION_EXPIRED, // Negotiation window (7 days) lapsed without both parties confirming;
  // awaiting superuser fault review
  IN_PROGRESS, // Terms confirmed, work started
  COMPLETED, // Project done
  CANCELLED, // Project cancelled by client
  DELETED // Removed by the client; retained because its status ledger cannot be erased
}
