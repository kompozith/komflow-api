package com.kompozith.komflow.features.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Levée lorsqu'une organisation a atteint la limite de son plan.
 * Traduit en HTTP 402 Payment Required.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PlanLimitExceededException extends RuntimeException {

  private final String metric;
  private final long   used;
  private final long   limit;

  public PlanLimitExceededException(String metric, long used, long limit) {
    super("Plan limit exceeded for metric [%s]: used=%d / limit=%d".formatted(metric, used, limit));
    this.metric = metric;
    this.used   = used;
    this.limit  = limit;
  }

  public String getMetric() { return metric; }
  public long   getUsed()   { return used;   }
  public long   getLimit()  { return limit;  }
}
