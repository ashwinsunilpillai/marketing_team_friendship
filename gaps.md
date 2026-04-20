# Marketing PDF vs `schema.sql` Gaps

This file summarizes the differences between `marketing.pdf` and the current `schema.sql` so the schema can be aligned with the document.

## Summary

The PDF describes a broader canonical marketing data model than the current SQL file. Your schema currently defines only three tables: `campaigns`, `customers`, and `segments`. The PDF expects six additional objects and also uses different column names and relationships for the objects that do exist.

## Objects Present In The PDF But Missing From `schema.sql`

| PDF object | PDF role | Status in `schema.sql` |
| --- | --- | --- |
| `customer_segments` | Table | Missing |
| `segment_members` | Table | Missing |
| `message_templates` | Table | Missing |
| `campaign_messages` | Table | Missing |
| `campaign_metrics` | Table | Missing |
| `consent_preferences` | Table | Missing |

## Objects In Both, But Not Aligned

### `campaigns`

The PDF describes `campaigns` with these business columns:

`campaign_id`, `campaign_title`, `campaign_type`, `target_vehicle_segment`, `campaign_budget`, `target_leads`, `start_date`, `end_date`, `campaign_roi`, `campaign_results`, `created_at`, `updated_at`.

The current schema instead defines:

`campaign_id`, `campaign_name`, `start_date`, `end_date`, `budget`, `status`, `segment_id`, `description`, `impressions`, `clicks`, `conversions`, `lead_target`, `leads_generated`, `campaign_type`, `created_at`, `updated_at`.

Main gaps:

- `campaign_name` does not match `campaign_title`.
- `budget` does not match `campaign_budget` and the PDF uses a wider numeric precision.
- `segment_id`, `description`, `impressions`, `clicks`, `conversions`, `lead_target`, and `leads_generated` are not part of the PDF model.
- The PDF includes `target_vehicle_segment`, `target_leads`, `campaign_roi`, and `campaign_results`, which are missing.

### `customers`

The PDF describes `customers` with these business columns:

`customer_id`, `name`, `email`, `phone`, `segment`, `region`, `interested_car_model`, `purchased_vin`, `vehicle_model_year`, `lifetime_value`, `status`, `last_contact_date`, `created_at`, `updated_at`.

The current schema instead defines:

`customer_id`, `first_name`, `last_name`, `email`, `phone`, `city`, `age`, `interest`, `status`, `created_at`, `updated_at`.

Main gaps:

- `name` is missing and is split into `first_name` and `last_name` instead.
- `segment`, `region`, `interested_car_model`, `purchased_vin`, `vehicle_model_year`, `lifetime_value`, and `last_contact_date` are missing.
- `city`, `age`, and `interest` are not part of the PDF model.

### `segments`

The PDF treats `segments` as a view, not a base table.

PDF expectation:

- `segments` is a compatibility view.
- It is derived from `customer_segments`.
- It exposes `segment_id`, `name`, and `criteria`.

Current schema:

- `segments` is created as a standalone table.
- It contains `segment_name`, `segment_type`, `criteria`, `customer_count`, `description`, `created_at`, and `updated_at`.

Main gaps:

- The object type is different: view in the PDF vs table in `schema.sql`.
- The PDF expects a base table called `customer_segments`, which is missing.
- The column names do not match the PDF view projection.

## Additional PDF Details Not Represented In The Schema

The PDF also documents relationship and access details that are not reflected in `schema.sql`:

- `segment_members` has foreign key links to `customer_segments.segment_id` and likely to a customer row, but no such table exists in the schema.
- `campaign_messages` links campaigns, customers, and message templates, but none of those relational tables exist in the current SQL.
- `campaign_metrics` includes `metric_date` and `revenue_generated`, which are not present in the current `campaigns` table.
- `consent_preferences` tracks `channel`, `is_opted_in`, and `consent_timestamp`, none of which exist in the schema.

## Bottom Line

The current schema is a simplified marketing schema, while the PDF describes a richer canonical model. If the goal is to comply with the PDF, the SQL file needs:

1. New tables for the missing objects.
2. Column renames and type adjustments for `campaigns` and `customers`.
3. A redesign of `segments` from table to view backed by `customer_segments`.
4. Relationship tables for campaign messaging, metrics, and consent tracking.
