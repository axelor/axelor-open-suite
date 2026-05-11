# Axelor Maintenance

Computerized Maintenance Management System (CMMS) module for Axelor Open Suite.
Provides equipment maintenance, preventive maintenance batch generation, and
maintenance request lifecycle management.

## Anticipation horizon on preventive maintenance batch

The preventive maintenance batch (`ProductionBatch.actionSelect = 4`) supports
an **anticipation horizon** (`mtnAnticipationDays`, integer, default 0) so that
maintenance requests can be created in advance of their scheduled date. This
gives planners lead time to order spare parts, schedule technicians, and plan
machine downtime.

### Behavior

When `mtnAnticipationDays = N` and an equipment has `nextMtnDate` set, the
batch creates the preventive `MaintenanceRequest` as soon as:

```
today >= nextMtnDate - N
```

The `expectedDate` on the created request **always equals `nextMtnDate`** — the
request is created early, but it is still scheduled for the correct target
date. A value of `0` (default) reproduces the legacy behavior (trigger on the
due date). The horizon is **not** applied to the day-based history path nor to
the operating-hours criterion.

### Configuration

On the `ProductionBatch` configuration form, when *Action* is set to
**Generate preventive maintenance requests**, fill the *Anticipation horizon
(days)* field. Existing batches keep `0` and behave exactly as before.

### Example

| `nextMtnDate` | `mtnAnticipationDays` | Run date     | Behavior                |
|---------------|-----------------------|--------------|-------------------------|
| 2026-08-01    | 14                    | 2026-07-15   | No request (outside horizon) |
| 2026-08-01    | 14                    | 2026-07-20   | Request created with `expectedDate = 2026-08-01` |
| 2026-08-01    | 0                     | 2026-07-31   | No request (legacy behavior) |
| 2026-08-01    | 0                     | 2026-08-01   | Request created with `expectedDate = 2026-08-01` |

Duplicate protection still applies: if a planned or in-progress preventive
request already exists for the equipment, no new request is created.
