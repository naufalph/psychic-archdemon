package com.rumantra.client.domain;

public enum ProjectStatus {
  PENDING_APPROVAL, // Just created, awaiting superuser validation
  REJECTED, // Superuser rejected/invalidated the project
  OPEN, // Validated by superuser, accepting bids
  BIDDING_CLOSED, // No more bids accepted
  NEGOTIATION, // Bid accepted, awaiting client confirmation before work starts
  IN_PROGRESS, // Terms confirmed, work started
  COMPLETED, // Project done
  CANCELLED // Project cancelled by client
}
